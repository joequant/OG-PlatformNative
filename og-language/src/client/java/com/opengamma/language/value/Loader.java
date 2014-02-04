/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.financial.view.rest.RemoteAvailableOutputsProvider;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.invoke.TypeConverterProviderBean;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the global context with value handling support (if available).
 */
public class Loader extends ContextInitializationBean {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "availableOutputs";
  private Configuration _configuration;
  private Supplier<URI> _uri;

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

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    ArgumentChecker.notNull(getGlobalContextFactory(), "globalContextFactory");
    _uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    s_logger.info("Configuring value support");
    final FunctionProviderBean functions = new FunctionProviderBean();
    functions.addFunction(ExpandComputedValuesFunction.INSTANCE);
    functions.addFunction(MarketDataRequirementNamesFunction.INSTANCE);
    functions.addFunction(ValueRequirementNamesFunction.INSTANCE);
    final URI uri = _uri.get();
    if (uri != null) {
      functions.addFunction(GetAvailableOutputsFunction.INSTANCE);
      globalContext.setAvailableOutputsProvider(new RemoteAvailableOutputsProvider(uri));
    } else {
      s_logger.warn("Available output support not available");
    }
    globalContext.getFunctionProvider().addProvider(functions);
    globalContext.getTypeConverterProvider().addTypeConverterProvider(
        new TypeConverterProviderBean(AvailableOutputsConverter.INSTANCE, ValuePropertiesConverter.INSTANCE, ComputationTargetTypeConverter.INSTANCE));
  }

}
