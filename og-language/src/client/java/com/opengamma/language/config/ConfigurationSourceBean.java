/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.opengamma.language.connector.AsyncSupplier;
import com.opengamma.language.connector.ConnectorStartupError;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider.Validater;
import com.opengamma.util.ArgumentChecker;

/**
 * Bean for querying a {@link Configuration} object from a supplied URL.
 */
public final class ConfigurationSourceBean implements InitializingBean {

  private URI _configurationUri;
  private Supplier<FudgeContext> _fudgeContext;
  private volatile FudgeMsg _lastMessage;

  public URI getConfigurationURIAsURI() {
    return _configurationUri;
  }

  public String getConfigurationURI() {
    return ObjectUtils.toString(_configurationUri, null);
  }

  public void setConfigurationURI(final String configurationUri) {
    ArgumentChecker.notNull(configurationUri, "configurationURI");
    try {
      _configurationUri = new URI(configurationUri);
    } catch (final URISyntaxException ex) {
      throw new ConnectorStartupError("The configuration URL is not valid: " + configurationUri,
          "A valid configuration URL must be supplied to start the service. Please re-run the Language Integration "
              + "configuration tool or consult the distribution documentation for more details.");
    }
  }

  public Supplier<FudgeContext> getFudgeContext() {
    return _fudgeContext;
  }

  public void setFudgeContext(final Supplier<FudgeContext> fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  /**
   * Fetches the Fudge message describing the configuration.
   * 
   * @param useCached whether to allow a previously cached message rather than fetch a new one
   * @return the Fudge configuration description, not null
   * @throws ConnectorStartupError if there is a fault fetching the configuration
   */
  public FudgeMsg fetchConfigurationMessage(final boolean useCachedMessage) {
    FudgeMsg msg;
    if (useCachedMessage) {
      msg = _lastMessage;
      if (msg != null) {
        return msg;
      }
    }
    final RemoteConfiguration remote = new RemoteConfiguration(getConfigurationURIAsURI());
    try {
      msg = remote.getConfigurationMsg();
    } catch (final Throwable t) {
      throw new ConnectorStartupError("The OpenGamma server at " + getConfigurationURI() + " is not responding",
          "The Language Integration service cannot start without an OpenGamma backend server to connect to. " + "Please check the OpenGamma server is running and try again.");
    }
    if (msg == null) {
      throw new ConnectorStartupError("The configuration address " + getConfigurationURI() + " is not valid",
          "Either the configuration address is wrong, or the OpenGamma server is not running correctly.", "Please check the OpenGamma server is running, re-run the Language Integration "
              + "configuration tool or consult the distribution documentation for more details.");
    }
    _lastMessage = msg;
    return msg;
  }

  public Configuration fetchConfigurationObject() {
    final AsyncSupplier.Spawned<FudgeMsg> configuration = new AsyncSupplier.Spawned<FudgeMsg>() {
      @Override
      protected FudgeMsg getImpl() {
        return fetchConfigurationMessage(true);
      }
    };
    configuration.start();
    final Validater validater = UriEndPointDescriptionProvider.validater(AsyncSupplier.EXECUTOR, getConfigurationURIAsURI());
    // Creating/configuring the validation client takes >1s so fork it off
    AsyncSupplier.EXECUTOR.execute(new Runnable() {
      @Override
      public void run() {
        validater.setTimeout(5000);
      }
    });
    return new Configuration(getFudgeContext(), configuration, validater);
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(_configurationUri, "configurationURI");
    if (_fudgeContext == null) {
      _fudgeContext = Suppliers.ofInstance(FudgeContext.GLOBAL_DEFAULT);
    }
  }

}
