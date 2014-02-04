/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a "volatility surface" component of a snapshot
 */
public class SetSnapshotVolatilitySurfaceFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetSnapshotVolatilitySurfaceFunction INSTANCE = new SetSnapshotVolatilitySurfaceFunction();

  private final MetaFunction _meta;

  private static final int SNAPSHOT = 0;
  private static final int NAME = 1;
  private static final int SURFACE = 2;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("snapshot", CoreModelTypes.MANAGEABLE_MARKET_DATA_SNAPSHOT), new MetaParameter("name", PrimitiveTypes.STRING), new MetaParameter("surface",
        CoreModelTypes.VOLATILITY_SURFACE_SNAPSHOT_ALLOW_NULL));
  }

  private SetSnapshotVolatilitySurfaceFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetSnapshotVolatilitySurface", getParameters(), this));
  }

  protected SetSnapshotVolatilitySurfaceFunction() {
    this(new DefinitionAnnotater(SetSnapshotVolatilitySurfaceFunction.class));
  }

  public static ManageableMarketDataSnapshot invoke(final ManageableMarketDataSnapshot snapshot, final String name, final VolatilitySurfaceSnapshot surface) {
    final VolatilitySurfaceKey key = StructuredMarketDataSnapshotUtil.toVolatilitySurfaceKey(name);
    if (key == null) {
      throw new InvokeInvalidArgumentException(NAME, "Invalid surface name");
    }
    if (snapshot.getVolatilitySurfaces() == null) {
      snapshot.setVolatilitySurfaces(new HashMap<VolatilitySurfaceKey, VolatilitySurfaceSnapshot>());
    }
    if (surface != null) {
      snapshot.getVolatilitySurfaces().put(key, surface);
    } else {
      snapshot.getVolatilitySurfaces().remove(key);
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableMarketDataSnapshot) parameters[SNAPSHOT], (String) parameters[NAME], (VolatilitySurfaceSnapshot) parameters[SURFACE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
