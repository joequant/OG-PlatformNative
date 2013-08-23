/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.security;

import java.net.URI;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.EHCachingConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.financial.security.EHCachingFinancialSecuritySource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.procedure.ProcedureProviderBean;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with support for securities (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "securitySource";
  private String _configConfigurationEntry = "securitySource";
  private Configuration _configuration;
  private CacheManager _cacheManager = CacheManager.getInstance();

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

  public String getConfigConfigurationEntry() {
    return _configConfigurationEntry;
  }

  public void setConfigConfigurationEntry(String configConfigurationEntry) {
    ArgumentChecker.notNull(configConfigurationEntry, "configConfigurationEntry");
    _configConfigurationEntry = configConfigurationEntry;
  }

  public void setCacheManager(final CacheManager cacheManager) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = cacheManager;
  }

  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final URI uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    if (uri == null) {
      s_logger.warn("Security support not available");
      return;
    }
    s_logger.info("Configuring security support");
    globalContext.setSecuritySource(new EHCachingFinancialSecuritySource(new RemoteFinancialSecuritySource(uri), getCacheManager()));
    final URI configUri = getConfiguration().getURIConfiguration(getConfigConfigurationEntry());
    if (configUri == null) {
      s_logger.warn("Config support not available");
      return;
    }
    s_logger.info("Configuring config support");
    globalContext.setConfigSource(new EHCachingConfigSource(new RemoteConfigSource(configUri), getCacheManager()));
    globalContext.getFunctionProvider().addProvider(
        new FunctionProviderBean(FetchSecurityFunction.INSTANCE));
    globalContext.getProcedureProvider().addProvider(
        new ProcedureProviderBean(StoreSecurityProcedure.INSTANCE));
  }

}
