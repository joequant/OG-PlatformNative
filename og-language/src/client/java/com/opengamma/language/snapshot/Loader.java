/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.net.URI;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.object.GetAttributeFunction;
import com.opengamma.language.object.SetAttributeFunction;
import com.opengamma.language.procedure.ProcedureProviderBean;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the global context with market data snapshot support (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private Configuration _configuration;
  private Supplier<URI> _uri;
  private String _configurationEntry = "marketDataSnapshotSource";
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

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

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
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
    final URI uri = _uri.get();
    if (uri == null) {
      s_logger.warn("Snapshot support not available");
      return;
    }
    s_logger.info("Configuring snapshot support");
    globalContext.setMarketDataSnapshotSource(new RemoteMarketDataSnapshotSource(uri));
    globalContext.getFunctionProvider().addProvider(
        new FunctionProviderBean(FetchSnapshotFunction.INSTANCE, GetSnapshotGlobalValueFunction.INSTANCE, GetSnapshotVolatilityCubeFunction.INSTANCE,
            GetSnapshotVolatilitySurfaceFunction.INSTANCE, GetSnapshotYieldCurveFunction.INSTANCE, GetVolatilityCubeTensorFunction.INSTANCE, GetVolatilitySurfaceTensorFunction.INSTANCE,
            GetYieldCurveTensorFunction.INSTANCE, GetCurveTensorFunction.INSTANCE, SetSnapshotGlobalValueFunction.INSTANCE, SetSnapshotVolatilityCubeFunction.INSTANCE,
            SetSnapshotVolatilitySurfaceFunction.INSTANCE, SetSnapshotYieldCurveFunction.INSTANCE, SetVolatilityCubePointFunction.INSTANCE, SetVolatilityCubeTensorFunction.INSTANCE,
            SetVolatilitySurfacePointFunction.INSTANCE, SetVolatilitySurfaceTensorFunction.INSTANCE, SetYieldCurvePointFunction.INSTANCE, SetCurvePointFunction.INSTANCE,
            SetYieldCurveTensorFunction.INSTANCE, SetCurveTensorFunction.INSTANCE, SnapshotsFunction.INSTANCE, SnapshotVersionsFunction.INSTANCE, GetSnapshotCurveFunction.INSTANCE,
            TakeSnapshotNowFunction.INSTANCE,
            // REVIEW 2011-12-01 andrew -- Why did I do the following? Why not put entries into ObjectFunctionProvider?
            new GetAttributeFunction(Categories.MARKET_DATA, "GetSnapshotName", "Fetches the name of a snapshot", ManageableMarketDataSnapshot.meta().name(), new MetaParameter("snapshot",
                CoreModelTypes.MANAGEABLE_MARKET_DATA_SNAPSHOT).description("The snapshot to query")), new SetAttributeFunction(Categories.MARKET_DATA, "SetSnapshotName",
                "Updates the name of a snapshot, returning the updated snapshot", ManageableMarketDataSnapshot.meta().name(), new MetaParameter("snapshot",
                    CoreModelTypes.MANAGEABLE_MARKET_DATA_SNAPSHOT).description("The snapshot to update"), new MetaParameter("name", PrimitiveTypes.STRING)
                    .description("The new name for the snapshot")), new GetAttributeFunction(Categories.MARKET_DATA, "GetSnapshotBasisViewName",
                "Fetches the view name the snapshot was originally based on", ManageableMarketDataSnapshot.meta().name(), new MetaParameter("snapshot",
                    CoreModelTypes.MANAGEABLE_MARKET_DATA_SNAPSHOT).description("The snapshot to query")), new SetAttributeFunction(Categories.MARKET_DATA, "SetSnapshotBasisViewName",
                "Updates the view name the snapshot was originally based on, returning the updated snapshot", ManageableMarketDataSnapshot.meta().name(), new MetaParameter("snapshot",
                    CoreModelTypes.MANAGEABLE_MARKET_DATA_SNAPSHOT).description("The snapshot to update"), new MetaParameter("name", PrimitiveTypes.STRING)
                    .description("The new basis view name for the snapshot"))));
    globalContext.getProcedureProvider().addProvider(new ProcedureProviderBean(SnapshotViewResultProcedure.INSTANCE, StoreSnapshotProcedure.INSTANCE));
    // TODO: type converters
  }

}
