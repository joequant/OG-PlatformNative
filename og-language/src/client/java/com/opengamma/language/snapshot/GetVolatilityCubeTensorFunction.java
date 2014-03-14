/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Fetches the data from a volatility surface as a 3D matrix tensor.
 */
public class GetVolatilityCubeTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetVolatilityCubeTensorFunction INSTANCE = new GetVolatilityCubeTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("snapshot", CoreModelTypes.MANAGEABLE_VOLATILITY_CUBE_SNAPSHOT), new MetaParameter("marketValue", PrimitiveTypes.BOOLEAN_DEFAULT_TRUE),
        new MetaParameter("overrideValue", PrimitiveTypes.BOOLEAN_DEFAULT_FALSE));
  }

  private GetVolatilityCubeTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "GetVolatilityCubeTensor", getParameters(), this));
  }

  protected GetVolatilityCubeTensorFunction() {
    this(new DefinitionAnnotater(GetVolatilityCubeTensorFunction.class));
  }

  public static Value[][][] invoke(final ManageableVolatilityCubeSnapshot snapshot, final Boolean marketValue, final Boolean overrideValue) {
    final Set<Tenor> keyXSet = new HashSet<Tenor>();
    final Set<Tenor> keyYSet = new HashSet<Tenor>();
    final Set<Double> keyZSet = new HashSet<Double>();
    for (Triple<Object, Object, Object> keyTriple : snapshot.getValues().keySet()) {
      final VolatilityPoint key = StructuredMarketDataSnapshotUtil.volatilityCubeValueKeyToVolatilityPoint(keyTriple);
      keyXSet.add(key.getSwapTenor());
      keyYSet.add(key.getOptionExpiry());
      keyZSet.add(key.getRelativeStrike());
    }
    final List<Tenor> keyX = new ArrayList<Tenor>(keyXSet);
    final List<Tenor> keyY = new ArrayList<Tenor>(keyYSet);
    final List<Double> keyZ = new ArrayList<Double>(keyZSet);
    Collections.sort(keyX);
    Collections.sort(keyY);
    Collections.sort(keyZ);
    final Value[][][] values = new Value[keyZ.size()][keyY.size()][keyX.size()];
    for (int i = 0; i < keyZ.size(); i++) {
      final Double z = keyZ.get(i);
      for (int j = 0; j < keyY.size(); j++) {
        final Tenor y = keyY.get(j);
        for (int k = 0; k < keyX.size(); k++) {
          final ValueSnapshot value = snapshot.getValues().get(Triple.of(keyX.get(k), y, z));
          if (value == null) {
            values[i][j][k] = new Value();
          } else if (Boolean.TRUE.equals(overrideValue) && (value.getOverrideValue() != null)) {
            values[i][j][k] = UnstructuredMarketDataSnapshotUtil.toValue(value.getOverrideValue());
          } else if (Boolean.TRUE.equals(marketValue) && (value.getMarketValue() != null)) {
            values[i][j][k] = UnstructuredMarketDataSnapshotUtil.toValue(value.getMarketValue());
          } else {
            values[i][j][k] = new Value();
          }
        }
      }
    }
    return values;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableVolatilityCubeSnapshot) parameters[0], (Boolean) parameters[1], (Boolean) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
