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
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Triple;

/**
 * Modifies a volatility cube to take values from the updated 2D matrix tensor.
 */
public class SetVolatilityCubeTensorFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetVolatilityCubeTensorFunction INSTANCE = new SetVolatilityCubeTensorFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("snapshot", CoreModelTypes.MANAGEABLE_VOLATILITY_CUBE_SNAPSHOT), new MetaParameter("overrideValue", JavaTypeInfo.builder(Value.class).arrayOf()
        .arrayOf().arrayOf().allowNull().get()), new MetaParameter("marketValue", JavaTypeInfo.builder(Value.class).arrayOf().arrayOf().arrayOf().allowNull().get()));
  }

  private SetVolatilityCubeTensorFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetVolatilityCubeTensor", getParameters(), this));
  }

  protected SetVolatilityCubeTensorFunction() {
    this(new DefinitionAnnotater(SetVolatilityCubeTensorFunction.class));
  }

  public static ManageableVolatilityCubeSnapshot invoke(final ManageableVolatilityCubeSnapshot snapshot, final Value[][][] overrideValue, final Value[][][] marketValue) {
    final Set<Object> keyXSet = new HashSet<>();
    final Set<Object> keyYSet = new HashSet<>();
    final Set<Object> keyZSet = new HashSet<>();
    for (Triple<Object, Object, Object> key : snapshot.getValues().keySet()) {
	keyXSet.add((Object) key.getFirst());
	keyYSet.add((Object) key.getSecond());
	keyZSet.add((Object) key.getThird());
    }
    final List<Object> keyX = new ArrayList<>(keyXSet);
    final List<Object> keyY = new ArrayList<>(keyYSet);
    final List<Object> keyZ = new ArrayList<>(keyZSet);

    if ((overrideValue != null) && (overrideValue.length < keyZ.size())) {
      throw new InvokeInvalidArgumentException(1, "Not enough planes in cube");
    }
    if ((marketValue != null) && (marketValue.length < keyZ.size())) {
      throw new InvokeInvalidArgumentException(2, "Not enough planes in cube");
    }
    for (int i = 0; i < keyZ.size(); i++) {
      final Object z = keyZ.get(i);
      if ((overrideValue != null) && (overrideValue[i].length < keyY.size())) {
        throw new InvokeInvalidArgumentException(1, "Not enough rows in cube");
      }
      if ((marketValue != null) && (marketValue[i].length < keyY.size())) {
        throw new InvokeInvalidArgumentException(2, "Not enough rows in cube");
      }
      for (int j = 0; j < keyY.size(); j++) {
        final Object y = keyY.get(j);
        if ((overrideValue != null) && (overrideValue[i][j].length < keyX.size())) {
          throw new InvokeInvalidArgumentException(1, "Not enough columns in cube");
        }
        if ((marketValue != null) && (marketValue[i][j].length < keyX.size())) {
          throw new InvokeInvalidArgumentException(2, "Not enough columns in cube");
        }
        for (int k = 0; k < keyX.size(); k++) {
	    final Triple<Object, Object, Object> key = new  Triple<Object, Object, Object>(keyX.get(k), y, z);
	    Map<Triple<Object, Object, Object>, ValueSnapshot> snapshotValues = snapshot.getValues();
          final ValueSnapshot value = snapshotValues.get(key);
          if (marketValue != null) {
            final Double override;
            if (overrideValue != null) {
              override = overrideValue[i][j][k].getDoubleValue();
            } else {
              if (value != null) {
                override = UnstructuredMarketDataSnapshotUtil.getOverrideValue(value);
              } else {
                override = null;
              }
            }
            snapshotValues.put(key, ValueSnapshot.of(marketValue[i][j][k].getDoubleValue(), override));
          } else if (overrideValue != null) {
            if (value != null) {
              value.setOverrideValue(overrideValue[i][j][k].getDoubleValue());
            } else {
              snapshotValues.put(key, ValueSnapshot.of(null, overrideValue[i][j][k].getDoubleValue()));
            }
          }
        }
      }
    }
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableVolatilityCubeSnapshot) parameters[0], (Value[][][]) parameters[1], (Value[][][]) parameters[2]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
