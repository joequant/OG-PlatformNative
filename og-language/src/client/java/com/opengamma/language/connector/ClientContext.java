/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.language.context.SessionContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents constant state held by most clients.
 */
public final class ClientContext {

  private static final Logger s_logger = LoggerFactory.getLogger(ClientContext.class);

  private final FudgeContext _fudgeContext;
  private final ScheduledExecutorService _housekeepingScheduler;
  private final ClientExecutor _executor;
  private final int _messageTimeout;
  private final int _heartbeatTimeout;
  private final int _terminationTimeout;
  private final FudgeMsgEnvelope _heartbeatMessage;
  private final UserMessagePayloadVisitor<UserMessagePayload, SessionContext> _messageHandler;
  private Set<Queue<FudgeMsgEnvelope>> _active = Sets.newIdentityHashSet();

  public ClientContext(final FudgeContext fudgeContext, final ScheduledExecutorService housekeepingScheduler, final ClientExecutor executor, final int messageTimeout,
      final int heartbeatTimeout, final int terminationTimeout, final UserMessagePayloadVisitor<UserMessagePayload, SessionContext> messageHandler) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(housekeepingScheduler, "housekeepingScheduler");
    ArgumentChecker.notNull(executor, "executor");
    ArgumentChecker.notNull(messageHandler, "messageHandler");
    _fudgeContext = fudgeContext;
    _housekeepingScheduler = housekeepingScheduler;
    _executor = executor;
    _messageTimeout = messageTimeout;
    _heartbeatTimeout = heartbeatTimeout;
    _terminationTimeout = terminationTimeout;
    _heartbeatMessage = new FudgeMsgEnvelope(new ConnectorMessage(ConnectorMessage.Operation.HEARTBEAT).toFudgeMsg(new FudgeSerializer(fudgeContext)), 0, MessageDirectives.CLIENT);
    _messageHandler = messageHandler;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public ScheduledExecutorService getHousekeepingScheduler() {
    return _housekeepingScheduler;
  }

  public ExecutorService createExecutor() {
    return _executor.createClientExecutor();
  }

  public int getMessageTimeout() {
    return _messageTimeout;
  }

  public int getHeartbeatTimeout() {
    return _heartbeatTimeout;
  }

  public int getTerminationTimeout() {
    return _terminationTimeout;
  }

  public FudgeMsgEnvelope getHeartbeatMessage() {
    return _heartbeatMessage;
  }

  public UserMessagePayloadVisitor<UserMessagePayload, SessionContext> getMessageHandler() {
    return _messageHandler;
  }

  public FudgeMsgEnvelope getShutdownMessage() {
    return new FudgeMsgEnvelope(new ConnectorMessage(ConnectorMessage.Operation.POISON).toFudgeMsg(new FudgeSerializer(getFudgeContext())), 0, MessageDirectives.CLIENT);
  }

  /**
   * Registers an output buffer to receive the shutdown message during a service stop.
   * 
   * @param queue the queue to post the message to, not null
   * @return true if the queue was registered, false if the shutdown has already begun
   */
  public synchronized boolean registerForShutdown(final Queue<FudgeMsgEnvelope> queue) {
    if (_active != null) {
      s_logger.debug("Connection registered for {}", this);
      _active.add(queue);
      return true;
    } else {
      s_logger.debug("ClientContext {} already shutdown at connection", this);
      return false;
    }
  }

  /**
   * Unregisters an output buffer, previously registered with {@link #registerForShutdown}, for notifications.
   * 
   * @param queue the queue that was previously registered, not null
   */
  public synchronized void unregisterForShutdown(final Queue<FudgeMsgEnvelope> queue) {
    if (_active != null) {
      s_logger.debug("Unregistered connection for {}", this);
      _active.remove(queue);
    }
  }

  /**
   * Sends the shutdown message (see {@link #getShutdownMessage}) to all registered buffers and prevents any new registrations.
   */
  public synchronized void shutdown() {
    final Collection<Queue<FudgeMsgEnvelope>> active = _active;
    _active = null;
    if ((active != null) && !active.isEmpty()) {
      final FudgeMsgEnvelope msg = getShutdownMessage();
      s_logger.info("Terminating {} active connection(s)", active.size());
      for (Queue<FudgeMsgEnvelope> connection : active) {
        // Graceful shutdown message
        connection.add(msg);
        // Local poison message
        connection.add(FudgeContext.EMPTY_MESSAGE_ENVELOPE);
      }
    } else {
      s_logger.debug("No active connections to terminate");
    }
  }
}
