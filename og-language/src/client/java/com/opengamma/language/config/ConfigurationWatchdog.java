/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.ClientContextFactoryBean;
import com.opengamma.language.connector.Main;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Fetches, and monitors, the logical server identifier for changes.
 */
public final class ConfigurationWatchdog extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigurationWatchdog.class);

  /**
   * The configuration document provider.
   */
  private ConfigurationSourceBean _configuration;
  /**
   * The client context factory (used for a housekeeper timer)
   */
  private ClientContextFactoryBean _clientFactory;
  /**
   * The name of the field containing the logical server identifier.
   */
  private String _logicalServerId = "lsid";
  /**
   * The watchdog thread.
   */
  private volatile ScheduledFuture<?> _watchdog;

  public void setConfiguration(final ConfigurationSourceBean configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public ConfigurationSourceBean getConfiguration() {
    return _configuration;
  }

  public void setClientContextFactory(final ClientContextFactoryBean clientFactory) {
    _clientFactory = clientFactory;
  }

  public ClientContextFactoryBean getClientContextFactory() {
    return _clientFactory;
  }

  public void setLogicalServerId(final String logicalServerId) {
    ArgumentChecker.notNull(logicalServerId, "logicalServerId");
    _logicalServerId = logicalServerId;
  }

  public String getLogicalServerId() {
    return _logicalServerId;
  }

  protected void restart(final String logMessage) {
    s_logger.info("Restarting, message {}", logMessage);
    try {
      Main.halt(logMessage);
    } catch (Throwable t) {
      s_logger.error("Couldn't restart local stack", t);
    }
  }

  protected void serverNotAvailable(final String message) {
    s_logger.error("Server not available: {}", message);
    restart("Disconnected from the back-end OpenGamma server.\n" + message);
  }

  protected void serverChanged() {
    restart("Back-end OpenGamma server configuration has changed - disconnecting.");
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    ArgumentChecker.notNull(getClientContextFactory(), "clientContextFactory");
    ArgumentChecker.notNull(getLogicalServerId(), "logicalServerId");
  }

  @Override
  protected void initContext(final MutableGlobalContext context) {
    final String lsid = getConfiguration().fetchConfigurationMessage(true).getString(getLogicalServerId());
    s_logger.info("Setting logical server identifier to {}", lsid);
    context.setLogicalServerId(lsid);
    if (_watchdog == null) {
      synchronized (this) {
        if (_watchdog == null) {
          final Runnable watchdog = new Runnable() {
            @Override
            public void run() {
              final String newServerId;
              try {
                s_logger.info("Polling server for logical changes");
                newServerId = getConfiguration().fetchConfigurationMessage(false).getString(getLogicalServerId());
              } catch (Throwable t) {
                s_logger.info("Caught exception", t);
                serverNotAvailable(t.getMessage());
                return;
              }
              if (ObjectUtils.equals(lsid, newServerId)) {
                s_logger.debug("LSID ok");
                return;
              }
              s_logger.info("LSID change from {} to {}", lsid, newServerId);
              serverChanged();
              synchronized (this) {
                _watchdog.cancel(false);
                _watchdog = null;
              }
            }
          };
          final int timeout = Integer.parseInt(System.getProperty("opengamma.configuration.watchdogPeriod", "120"));
          _watchdog = getClientContextFactory().getHousekeepingScheduler().scheduleWithFixedDelay(watchdog, timeout, timeout, TimeUnit.SECONDS);
        }
      }
    }
  }

}
