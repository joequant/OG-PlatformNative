/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to server metadata.
 */
public class ServerMetadata {

  private static final String LSID = "lsid";
  private static final String DESCRIPTION = "description";

  private final ConfigurationSourceBean _configuration;

  public ServerMetadata(final ConfigurationSourceBean configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  protected ConfigurationSourceBean getConfigurationSource() {
    return _configuration;
  }

  protected FudgeMsg getConfigurationMessage() {
    return getConfigurationSource().fetchConfigurationMessage(true);
  }

  public String getConfigurationURL() {
    return getConfigurationSource().getConfigurationURI();
  }

  public void setConfigurationURL(final String configurationURL) {
    // TODO: This might make sense, but will affect all connected clients so some form of ACL is sensible and/or fork the GC
    throw new UnsupportedOperationException("Can't set configuration URL");
  }

  public String getLogicalServerId() {
    return getConfigurationMessage().getString(LSID);
  }

  public void setLogicalServerId(final String lsid) {
    throw new UnsupportedOperationException("Can't set logical server identifier");
  }

  public FudgeMsg getPublishedConfiguration() {
    return getConfigurationMessage();
  }

  public void setPublishedConfiguration(final FudgeMsg configuration) {
    throw new UnsupportedOperationException("Can't set published configuration");
  }

  public String getServerDescription() {
    return getConfigurationMessage().getString(DESCRIPTION);
  }

  public void setServerDescription(final String description) {
    throw new UnsupportedOperationException("Can't set server description");
  }

}
