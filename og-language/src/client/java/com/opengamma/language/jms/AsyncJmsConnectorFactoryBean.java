/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.jms;

import javax.jms.ConnectionFactory;

import com.google.common.base.Supplier;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.language.connector.AsyncSupplier;
import com.opengamma.util.SingletonFactoryBean;
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
    return new AsyncSupplier.Filter<ConnectionFactory, JmsConnector>(getConnectionFactory(), new Function1<ConnectionFactory, JmsConnector>() {
      @Override
      public JmsConnector execute(final ConnectionFactory connectionFactory) {
        final JmsConnectorFactoryBean bean = new JmsConnectorFactoryBean();
        bean.setName(getName());
        bean.setConnectionFactory(connectionFactory);
        return bean.getObjectCreating();
      }
    });
  }

}
