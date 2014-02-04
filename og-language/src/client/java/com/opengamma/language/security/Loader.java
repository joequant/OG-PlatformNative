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

import com.google.common.base.Supplier;
import com.opengamma.core.config.impl.EHCachingConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.core.holiday.impl.CachedHolidaySource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
import com.opengamma.core.region.impl.RemoteRegionSource;
import com.opengamma.financial.security.EHCachingFinancialSecuritySource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.procedure.ProcedureProviderBean;
import com.opengamma.language.security.fra.FRASecurityFromIndexFunction;
import com.opengamma.master.region.impl.EHCachingRegionSource;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with support for securities (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "securitySource";
  private String _configConfigurationEntry = "configSource"; // TODO: This shouldn't be here
  private String _holidayConfigurationEntry = "holidaySource"; // TODO: This shouldn't be here
  private String _regionConfigurationEntry = "regionSource"; // TODO: This shouldn't be here
  private Configuration _configuration;
  private Supplier<URI> _uri;
  private Supplier<URI> _configUri; // TODO: This shouldn't be here
  private Supplier<URI> _holidayUri; // TODO: This shouldn't be here
  private Supplier<URI> _regionUri; // TODO: This shouldn't be here
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

  public String getConfigConfigurationEntry() { // TODO: This shouldn't be here
    return _configConfigurationEntry;
  }

  public void setConfigConfigurationEntry(String configConfigurationEntry) { // TODO: This shouldn't be here
    ArgumentChecker.notNull(configConfigurationEntry, "configConfigurationEntry");
    _configConfigurationEntry = configConfigurationEntry;
  }
  
  public String getHolidayConfigurationEntry() { // TODO: This shouldn't be here
    return _holidayConfigurationEntry;
  }
  
  public void setHolidayConfigurationEntry(String holidayConfigurationEntry) { // TODO: This shouldn't be here
    ArgumentChecker.notNull(holidayConfigurationEntry, "holidayConfigurationEntry");
    _holidayConfigurationEntry = holidayConfigurationEntry;
  }
  
  public String getRegionConfigurationEntry() { // TODO: This shouldn't be here
    return _regionConfigurationEntry;
  }
  
  public void setRegionConfigurationEntry(String regionConfigurationEntry) { // TODO: This shouldn't be here
    ArgumentChecker.notNull(regionConfigurationEntry, "regionConfigurationEntry");
    _regionConfigurationEntry = regionConfigurationEntry;
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
    _uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    _configUri = getConfiguration().getURIConfiguration(getConfigConfigurationEntry()); // TODO: This shouldn't be here
    _holidayUri = getConfiguration().getURIConfiguration(getHolidayConfigurationEntry()); // TODO: This shouldn't be here
    _regionUri = getConfiguration().getURIConfiguration(getRegionConfigurationEntry()); // TODO: This shouldn't be here
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final URI uri = _uri.get();
    if (uri == null) {
      s_logger.warn("Security support not available");
      return;
    }
    s_logger.info("Configuring security support");
    globalContext.setSecuritySource(new EHCachingFinancialSecuritySource(new RemoteFinancialSecuritySource(uri), getCacheManager()));
    // TODO: Move ConfigSource population into the "config" package. It makes no sense to have it here.
    final URI configUri = _configUri.get();
    if (configUri == null) {
      s_logger.warn("Config support not available");
      return;
    }
    s_logger.info("Configuring config support");
    globalContext.setConfigSource(new EHCachingConfigSource(new RemoteConfigSource(configUri), getCacheManager()));
    // TODO: Move HolidaySource population into the "holiday" package. It makes no sense to have it here.
    final URI holidayUri = _holidayUri.get();
    if (holidayUri == null) {
      s_logger.warn("Holiday support not available");
      return;
    }
    s_logger.info("Configuring holiday support");
    globalContext.setHolidaySource(new CachedHolidaySource(new RemoteHolidaySource(holidayUri)));
    // TODO: Move RegionSource population into the "region" package. It makes no sense to have it here.
    final URI regionUri = _regionUri.get();
    if (regionUri == null) {
      s_logger.warn("Region support not available");
      return;
    }
    s_logger.info("Configuring region support");
    globalContext.setRegionSource(new EHCachingRegionSource(new RemoteRegionSource(regionUri), getCacheManager()));
    // TODO: Change to a function provider for this package
    globalContext.getFunctionProvider().addProvider(new FunctionProviderBean(FetchSecurityFunction.INSTANCE, FRASecurityFromIndexFunction.INSTANCE, GetDateUsingIndexFunction.INSTANCE));
    globalContext.getProcedureProvider().addProvider(new ProcedureProviderBean(StoreSecurityProcedure.INSTANCE));
>>>>>>> [PLAT-5672] Speed up Spring initialization.
  }

}
