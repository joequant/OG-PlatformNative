/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.pool.PooledConnectionFactory;

import com.opengamma.language.config.Configuration;
import com.opengamma.language.connector.AsyncSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.function.Function;

/**
 * Configures an ActiveMQ JMS provider from the configuration document. The document must have a string entry giving the URL for the ActiveMQ server.
 */
public class ActiveMQConnectionFactoryFactoryBean extends SingletonFactoryBean<AsyncSupplier<ConnectionFactory>> {

  private Configuration _configuration;
  private String _configurationEntry = "activeMQ";

  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setConfigurationEntry(final String configurationEntry) {
    ArgumentChecker.notNull(configurationEntry, "configurationEntry");
    _configurationEntry = configurationEntry;
  }

  public String getConfigurationEntry() {
    return _configurationEntry;
  }

  @Override
  protected AsyncSupplier<ConnectionFactory> createObject() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    return new AsyncSupplier.Filter<String, ConnectionFactory>(getConfiguration().getStringConfiguration(getConfigurationEntry()), new Function<String, ConnectionFactory>() {
      @Override
      public ConnectionFactory apply(final String brokerURL) {
        final PooledConnectionFactory factory;
        if (brokerURL == null) {
          factory = new PooledConnectionFactory();
        } else {
          factory = new PooledConnectionFactory(brokerURL);
        }
        // Workaround for [AMQ-4366]
        factory.setIdleTimeout(0);
        return factory;
      }
    });
  }

}
