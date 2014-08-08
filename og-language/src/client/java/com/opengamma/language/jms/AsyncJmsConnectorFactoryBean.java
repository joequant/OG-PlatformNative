/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.jms;

import java.net.URI;

import javax.jms.ConnectionFactory;

import com.google.common.base.Supplier;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.connector.AsyncSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.function.Function;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Alternative form of {@link JmsConnectorFactoryBean} that returns a {@link Supplier} to the JmsConnector to support parallel initialization of I/O bound beans.
 */
public class AsyncJmsConnectorFactoryBean extends SingletonFactoryBean<AsyncSupplier<JmsConnector>> {

  /**
   * The configuration name.
   */
  private String _name;
  /**
   * The configuration.
   */
  private Configuration _configuration;
  /**
   * The broker url.
   */
  private String _clientBrokerUriEntry = "activeMQ";

  /**
   * The connection factory.
   */
  private AsyncSupplier<ConnectionFactory> _connectionFactory;

  public void setName(final String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public void setConnectionFactory(final AsyncSupplier<ConnectionFactory> connectionFactory) {
    _connectionFactory = connectionFactory;
  }

  public AsyncSupplier<ConnectionFactory> getConnectionFactory() {
    return _connectionFactory;
  }

  // JmsConnector

  @Override
  protected AsyncSupplier<JmsConnector> createObject() {
    return new AsyncSupplier.Filter<ConnectionFactory, JmsConnector>(getConnectionFactory(), new Function<ConnectionFactory, JmsConnector>() {
      @Override
      public JmsConnector apply(final ConnectionFactory connectionFactory) {
        final JmsConnectorFactoryBean bean = new JmsConnectorFactoryBean();
        bean.setName(getName());
        bean.setConnectionFactory(connectionFactory);
        String clientBrokerUri = getConfiguration().getStringConfiguration(getClientBrokerUriEntry()).get();
        bean.setClientBrokerUri(URI.create(clientBrokerUri));
        return bean.getObjectCreating();
      }
    });
  }

  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setClientBrokerUriEntry(final String clientBrokerUriEntry) {
    ArgumentChecker.notNull(clientBrokerUriEntry, "clientBrokerUriEntry");
    _clientBrokerUriEntry = clientBrokerUriEntry;
  }

  public String getClientBrokerUriEntry() {
    return _clientBrokerUriEntry;
  }
}
