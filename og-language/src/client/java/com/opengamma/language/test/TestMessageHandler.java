/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.test;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.Custom;
import com.opengamma.language.connector.Main;
import com.opengamma.language.connector.UserMessage;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.debug.UnhandledMessageHandler;
import com.opengamma.language.debug.UnhandledMessageHandlerAdapter;
import com.opengamma.language.test.Test.Operation;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Responds to the Test message to allow an unit test of the messaging infrastructures.
 */
public class TestMessageHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(TestMessageHandler.class);

  public static final class NonDeliverable extends Custom {

    private static final long serialVersionUID = 1L;

    private final int _nonce;

    public NonDeliverable(final int nonce) {
      _nonce = nonce;
    }

    @Override
    public void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg fudgeMsg) {
      fudgeMsg.add("nonce", _nonce);
    }

    public static Custom fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg fudgeMsg) {
      return new NonDeliverable(fudgeMsg.getInt("nonce"));
    }

  }

  public static class Unhandled extends UnhandledMessageHandlerAdapter<SessionContext> {

    protected Unhandled(final UnhandledMessageHandler<SessionContext> underlying) {
      super(underlying);
    }

    @Override
    public void unhandledMessage(final UserMessage message, final SessionContext context) throws AsynchronousExecution {
      if (message.getPayload() instanceof NonDeliverable) {
        s_logger.info("Non-deliverable message returned - sending ECHO_RESPONSE_A");
        context.getMessageSender().send(new Test(Test.Operation.ECHO_RESPONSE_A, ((NonDeliverable) message.getPayload())._nonce));
      } else {
        super.unhandledMessage(message, context);
      }
    }

  }

  /**
   * Returns the inline response to the message (or null for none), and sends asynchronous responses to the supplied sender.
   * 
   * @param message received test message
   * @param sender message sender for asynchronous messages
   */
  @SuppressWarnings("deprecation")
  public static UserMessagePayload testMessage(final Test message, final SessionContext context) {
    switch (message.getOperation()) {
      case CRASH_REQUEST: {
        s_logger.info("CRASH_REQUEST - calling system.exit");
        final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
        msg.add("foo", null, 42);
        context.getStashMessage().put(msg);
        System.exit(1);
        return null;
      }
      case ECHO_REQUEST:
        s_logger.info("ECHO_REQUEST - returning ECHO_RESPONSE");
        message.setOperation(Operation.ECHO_RESPONSE);
        return message;
      case ECHO_REQUEST_A:
        s_logger.info("ECHO_REQUEST_A - sending ECHO_RESPONSE_A asynchronously");
        message.setOperation(Operation.ECHO_RESPONSE_A);
        context.getMessageSender().send(message.clone());
        s_logger.info("ECHO_REQUEST_A - returning ECHO_RESPONSE");
        message.setOperation(Operation.ECHO_RESPONSE);
        return message;
      case ECHO_RESPONSE:
        throw new IllegalArgumentException("ECHO_RESPONSE should not have been sent by the server");
      case ECHO_RESPONSE_A:
        throw new IllegalArgumentException("ECHO_RESPONSE_A should not have been sent by the server");
      case NON_DELIVERY_REQUEST:
        s_logger.info("NON_DELIVERY_REQUEST - sending non-deliverable payload");
        context.getMessageSender().send(new NonDeliverable(message.getNonce()));
        return null;
      case PAUSE_REQUEST:
        s_logger.info("PAUSE_REQUEST - suspending threads");
        Main.notifyPause();
        final Thread[] threads = new Thread[100];
        Thread.enumerate(threads);
        // The thread control methods are deprecated for the exact reasons we want to use them. We want
        // to induce a deadlock or some other serious fault that hangs the JVM to test the resilience
        // mechanisms.
        for (int i = 0; i < threads.length; i++) {
          if (threads[i] != null) {
            if (threads[i] != Thread.currentThread()) {
              s_logger.debug("Suspending {}", threads[i].getName());
              threads[i].suspend();
            }
          }
        }
        Thread.currentThread().suspend();
        return null;
      case STASH_REQUEST: {
        s_logger.info("STASH_REQUEST - checking stash");
        final FudgeMsg msg = context.getStashMessage().get();
        if (msg != null) {
          s_logger.debug("Stash = {}", msg);
          if (msg.getInt("foo") == 42) {
            message.setOperation(Operation.STASH_RESPONSE);
            return message;
          }
        }
        return null;
      }
      case VOID_REQUEST:
        s_logger.info("VOID_REQUEST - no response");
        return null;
      case VOID_REQUEST_A:
        s_logger.info("VOID_REQUEST_A - sending VOID_RESPONSE_A asynchronously");
        message.setOperation(Operation.VOID_RESPONSE_A);
        context.getMessageSender().send(message);
        return null;
      case VOID_RESPONSE_A:
        throw new IllegalArgumentException("VOID_RESPONSE_A should not have been sent by the server");
      default:
        throw new IllegalArgumentException("Unexpected operation " + message.getOperation());
    }
  }

}
