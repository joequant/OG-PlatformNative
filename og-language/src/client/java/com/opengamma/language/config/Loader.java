/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.opengamma.core.config.impl.EHCachingConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with support for securities (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "configSource"; // TODO: This shouldn't be here
  private Configuration _configuration;
  private Supplier<URI> _uri;
  private Supplier<CacheManager> _cacheManager = DEFAULT_CACHE_MANAGER;

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

  public void setCacheManager(final CacheManager cacheManager) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    _cacheManager = Suppliers.ofInstance(cacheManager);
  }

  public CacheManager getCacheManager() {
    return _cacheManager.get();
  }

  // ContextInitializationBean

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
    _uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    // These functions are for "configuration", but don't use a config-source
    globalContext.getFunctionProvider().addProvider(new ConfigurationFunctionProvider());
    // The remainder requires a config-source
    final URI uri = _uri.get();
    if (uri == null) {
      s_logger.warn("Config database support not available");
      return;
    }
    s_logger.info("Configuring config support");
    globalContext.setConfigSource(new EHCachingConfigSource(new RemoteConfigSource(uri), getCacheManager()));
  }

}
