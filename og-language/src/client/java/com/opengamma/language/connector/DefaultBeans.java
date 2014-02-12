/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.connector;

import java.util.ArrayList;
import java.util.Collection;

import org.fudgemsg.FudgeContext;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Supplier;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.GlobalContextFactoryBean;
import com.opengamma.language.context.SessionContextFactoryBean;
import com.opengamma.language.context.UserContextFactoryBean;
import com.opengamma.language.curve.GetCurveYValuesFunction;
import com.opengamma.language.debug.DebugLiveDataRandom;
import com.opengamma.language.debug.DebugProcedureIncrement;
import com.opengamma.language.external.ExternalFunctionProvider;
import com.opengamma.language.function.FunctionProvider;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.invoke.TypeConverterProvider;
import com.opengamma.language.livedata.LiveDataProviderBean;
import com.opengamma.language.procedure.ProcedureProviderBean;
import com.opengamma.util.jms.JmsConnector;

/**
 * Creates all of the standard beans that will configure the base system but don't require Spring exposure as extension points for language bindings. Anything that must be exposed should be put in
 * Client.xml - anything else should be put in here.
 */
public class DefaultBeans implements InitializingBean {

  private ClientContextFactoryBean _clientContextFactory;
  private SessionContextFactoryBean _sessionContextFactory;
  private UserContextFactoryBean _userContextFactory;
  private GlobalContextFactoryBean _globalContextFactory;

  private Conditional _debugLoaderCondition;

  private Configuration _configuration;
  private Supplier<FudgeContext> _fudgeContext;
  private Supplier<JmsConnector> _jmsConnector;

  // Properties exposed to Spring

  public ClientContextFactoryBean getClientContextFactory() {
    return _clientContextFactory;
  }

  public void setClientContextFactory(final ClientContextFactoryBean clientContextFactory) {
    _clientContextFactory = clientContextFactory;
  }

  public SessionContextFactoryBean getSessionContextFactory() {
    return _sessionContextFactory;
  }

  public void setSessionContextFactory(final SessionContextFactoryBean sessionContextFactory) {
    _sessionContextFactory = sessionContextFactory;
  }

  public UserContextFactoryBean getUserContextFactory() {
    return _userContextFactory;
  }

  public void setUserContextFactory(final UserContextFactoryBean userContextFactory) {
    _userContextFactory = userContextFactory;
  }

  public GlobalContextFactoryBean getGlobalContextFactory() {
    return _globalContextFactory;
  }

  public void setGlobalContextFactory(final GlobalContextFactoryBean globalContextFactory) {
    _globalContextFactory = globalContextFactory;
  }

  public Conditional getDebugLoaderCondition() {
    return _debugLoaderCondition;
  }

  public void setDebugLoaderCondition(final Conditional condition) {
    _debugLoaderCondition = condition;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setConfiguration(final Configuration configuration) {
    _configuration = configuration;
  }

  public Supplier<FudgeContext> getFudgeContext() {
    return _fudgeContext;
  }

  public void setFudgeContext(final Supplier<FudgeContext> fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public Supplier<JmsConnector> getJmsConnector() {
    return _jmsConnector;
  }

  public void setJmsConnector(final Supplier<JmsConnector> jmsConnector) {
    _jmsConnector = jmsConnector;
  }

  // Programmatic creation of beans

  protected InitializingBean createDebugLoader() {
    final com.opengamma.language.debug.Loader loader = new com.opengamma.language.debug.Loader();
    loader.setSessionContextFactory(getSessionContextFactory());
    loader.setCondition(getDebugLoaderCondition());
    return loader;
  }

  protected InitializingBean createDefaultFunctionsLoader() {
    final com.opengamma.language.function.Loader loader = new com.opengamma.language.function.Loader();
    loader.setGlobalContextFactory(getGlobalContextFactory());
    final Collection<FunctionProvider> providers = new ArrayList<FunctionProvider>(3);
    // For odd functions, use the provider bean. If a package exports more than a couple, create a provider. If a package provides other services, create a loader -->
    providers.add(new ExternalFunctionProvider());
    final FunctionProviderBean bean = new FunctionProviderBean();
    bean.addFunction(new GetCurveYValuesFunction());
    providers.add(bean);
    loader.setFunctionProviders(providers);
    return loader;
  }

  protected InitializingBean createDefaultLiveDataLoader() {
    final com.opengamma.language.livedata.Loader loader = new com.opengamma.language.livedata.Loader();
    loader.setGlobalContextFactory(getGlobalContextFactory());
    // This is a temporary measure so that the release build doesn't error in the tests; take this out when there are some valid "liveData" entries from one of the loaders
    loader.setLiveDataProvider(new LiveDataProviderBean(new DebugLiveDataRandom()));
    return loader;
  }

  protected InitializingBean createDefaultProcedureLoader() {
    final com.opengamma.language.procedure.Loader loader = new com.opengamma.language.procedure.Loader();
    loader.setGlobalContextFactory(getGlobalContextFactory());
    // This is a temporary measure so that the release build doesn't error in the tests; take this out when there are some valid "procedure" entries from one of the loaders
    loader.setProcedureProvider(new ProcedureProviderBean(new DebugProcedureIncrement()));
    return loader;
  }

  protected InitializingBean createDefaultTypeConversions() {
    final com.opengamma.language.invoke.Loader loader = new com.opengamma.language.invoke.Loader();
    loader.setGlobalContextFactory(getGlobalContextFactory());
    loader.setFudgeContext(getFudgeContext());
    final Collection<TypeConverterProvider> providers = new ArrayList<TypeConverterProvider>(2);
    // For odd converters, use the provider bean. If a package exports more than a couple, create a Converters instance. If a package provides other services, create a loader -->
    providers.add(new com.opengamma.language.convert.Converters());
    loader.setTypeConverterProviders(providers);
    return loader;
  }

  protected InitializingBean createClientLoader() {
    final com.opengamma.language.client.Loader loader = new com.opengamma.language.client.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    loader.setHousekeepingScheduler(getClientContextFactory().getHousekeepingScheduler());
    loader.setSessionContextFactory(getSessionContextFactory());
    loader.setUserContextFactory(getUserContextFactory());
    return loader;
  }

  protected InitializingBean createConfigLoader() {
    final com.opengamma.language.config.Loader loader = new com.opengamma.language.config.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createConventionLoader() {
    final com.opengamma.language.convention.Loader loader = new com.opengamma.language.convention.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createCurrencyPairsLoader() {
    final com.opengamma.language.currency.Loader loader = new com.opengamma.language.currency.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createExchangeLoader() {
    final com.opengamma.language.exchange.Loader loader = new com.opengamma.language.exchange.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createHolidayLoader() {
    final com.opengamma.language.holiday.Loader loader = new com.opengamma.language.holiday.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createIdentifierLoader() {
    final com.opengamma.language.identifier.Loader loader = new com.opengamma.language.identifier.Loader();
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createLegalEntityLoader() {
    final com.opengamma.language.legalentity.Loader loader = new com.opengamma.language.legalentity.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createMarketDataSnapshotLoader() {
    final com.opengamma.language.snapshot.Loader loader = new com.opengamma.language.snapshot.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createObjectSupportLoader() {
    final com.opengamma.language.object.Loader loader = new com.opengamma.language.object.Loader();
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createPositionLoader() {
    final com.opengamma.language.position.Loader loader = new com.opengamma.language.position.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createRegionLoader() {
    final com.opengamma.language.region.Loader loader = new com.opengamma.language.region.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createSecurityLoader() {
    final com.opengamma.language.security.Loader loader = new com.opengamma.language.security.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createTimeSeriesLoader() {
    final com.opengamma.language.timeseries.Loader loader = new com.opengamma.language.timeseries.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createTradeLoader() {
    final com.opengamma.language.trade.Loader loader = new com.opengamma.language.trade.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createValueLoader() {
    final com.opengamma.language.value.Loader loader = new com.opengamma.language.value.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  protected InitializingBean createViewProcessorLoader() {
    final com.opengamma.language.view.Loader loader = new com.opengamma.language.view.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    loader.setHousekeepingScheduler(getClientContextFactory().getHousekeepingScheduler());
    loader.setJmsConnector(getJmsConnector());
    loader.setUserContextFactory(getUserContextFactory());
    loader.setSessionContextFactory(getSessionContextFactory());
    return loader;
  }

  protected InitializingBean createVolatilityLoader() {
    final com.opengamma.language.volatility.Loader loader = new com.opengamma.language.volatility.Loader();
    loader.setConfiguration(getConfiguration());
    loader.setGlobalContextFactory(getGlobalContextFactory());
    return loader;
  }

  // InitializingBean

  @Override
  public final void afterPropertiesSet() throws Exception {
    createDebugLoader().afterPropertiesSet();
    createDefaultFunctionsLoader().afterPropertiesSet();
    createDefaultLiveDataLoader().afterPropertiesSet();
    createDefaultProcedureLoader().afterPropertiesSet();
    createDefaultTypeConversions().afterPropertiesSet();
    createClientLoader().afterPropertiesSet();
    createConfigLoader().afterPropertiesSet();
    createConventionLoader().afterPropertiesSet();
    createCurrencyPairsLoader().afterPropertiesSet();
    createExchangeLoader().afterPropertiesSet();
    createHolidayLoader().afterPropertiesSet();
    createIdentifierLoader().afterPropertiesSet();
    createLegalEntityLoader().afterPropertiesSet();
    createMarketDataSnapshotLoader().afterPropertiesSet();
    createObjectSupportLoader().afterPropertiesSet();
    createPositionLoader().afterPropertiesSet();
    createRegionLoader().afterPropertiesSet();
    createSecurityLoader().afterPropertiesSet();
    createTimeSeriesLoader().afterPropertiesSet();
    createTradeLoader().afterPropertiesSet();
    createValueLoader().afterPropertiesSet();
    createViewProcessorLoader().afterPropertiesSet();
    createVolatilityLoader().afterPropertiesSet();
  }

}
