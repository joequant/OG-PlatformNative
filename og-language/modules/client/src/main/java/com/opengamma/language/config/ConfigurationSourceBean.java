/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.language.connector.ConnectorStartupError;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider.Validater;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Bean for querying a {@link Configuration} object from a supplied URL.
 */
public final class ConfigurationSourceBean implements InitializingBean {

  private URI _configurationUri;
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
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
          "A valid configuration URL must be supplied to start the service. Please re-run the Language Integration " +
              "configuration tool or consult the distribution documentation for more details.");
    }
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
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
          "The Language Integration service cannot start without an OpenGamma backend server to connect to. " +
              "Please check the OpenGamma server is running and try again.");
    }
    if (msg == null) {
      throw new ConnectorStartupError("The configuration address " + getConfigurationURI() + " is not valid",
          "Either the configuration address is wrong, or the OpenGamma server is not running correctly.",
          "Please check the OpenGamma server is running, re-run the Language Integration " +
              "configuration tool or consult the distribution documentation for more details.");
    }
    _lastMessage = msg;
    return msg;
  }

  /**
   * Fetches the configuration from the network resource.
   * 
   * @param useCached whether to allow a previously cached message rather than fetch a new one
   * @return the configuration document object, not null
   * @throws ConnectorStartupError if there is a fault fetching the configuration
   */
  public Configuration fetchConfigurationObject(boolean useCachedMessage) {
    final Validater validater = UriEndPointDescriptionProvider.validater(Executors.newCachedThreadPool(), getConfigurationURIAsURI());
    return new Configuration(getFudgeContext(), fetchConfigurationMessage(useCachedMessage), validater);
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(_configurationUri, "configurationURI");
  }

}
