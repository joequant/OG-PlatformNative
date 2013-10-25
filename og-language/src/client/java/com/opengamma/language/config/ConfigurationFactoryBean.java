/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for creating a {@link Configuration} object from a supplied URL.
 */
public final class ConfigurationFactoryBean extends SingletonFactoryBean<Configuration> {

  private ConfigurationSourceBean _configuration;
  private boolean _failOnInvalidURI;
  private boolean _failOnMissingConfiguration;

  public ConfigurationSourceBean getConfiguration() {
    return _configuration;
  }

  public void setConfiguration(final ConfigurationSourceBean configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public boolean isFailOnInvalidURI() {
    return _failOnInvalidURI;
  }

  public void setFailOnInvalidURI(final boolean failOnInvalidURI) {
    _failOnInvalidURI = failOnInvalidURI;
  }

  public boolean isFailOnMissingConfiguration() {
    return _failOnMissingConfiguration;
  }

  public void setFailOnMissingConfiguration(final boolean failOnMissingConfiguration) {
    _failOnMissingConfiguration = failOnMissingConfiguration;
  }

  // SingletonFactoryBean

  @Override
  protected Configuration createObject() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    final Configuration configuration = getConfiguration().fetchConfigurationObject(true);
    configuration.setFailOnInvalidURI(isFailOnInvalidURI());
    configuration.setFailOnMissingConfiguration(isFailOnMissingConfiguration());
    return configuration;
  }

}
