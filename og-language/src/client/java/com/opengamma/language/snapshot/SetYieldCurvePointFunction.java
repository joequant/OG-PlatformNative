/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a point on a "yield curve"
 */
public class SetYieldCurvePointFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetYieldCurvePointFunction INSTANCE = new SetYieldCurvePointFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("snapshot", CoreModelTypes.MANAGEABLE_YIELD_CURVE_SNAPSHOT), new MetaParameter("valueName", PrimitiveTypes.STRING), new MetaParameter(
        "identifier", OpenGammaTypes.EXTERNAL_ID), new MetaParameter("overrideValue", PrimitiveTypes.DOUBLE_ALLOW_NULL), new MetaParameter("marketValue", PrimitiveTypes.DOUBLE_ALLOW_NULL));
  }

  // TODO: update the valuation time for the curve

  private SetYieldCurvePointFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MARKET_DATA, "SetYieldCurvePoint", getParameters(), this));
  }

  protected SetYieldCurvePointFunction() {
    this(new DefinitionAnnotater(SetYieldCurvePointFunction.class));
  }

  public static ManageableYieldCurveSnapshot invoke(ManageableYieldCurveSnapshot snapshot, final String valueName, final ExternalId identifier, final Double overrideValue,
      final Double marketValue) {
    if (snapshot.getValues() == null) {
      snapshot = snapshot.toBuilder().values(new ManageableUnstructuredMarketDataSnapshot()).build();
    }
    UnstructuredMarketDataSnapshotUtil.setValue(snapshot.getValues(), valueName, identifier, overrideValue, marketValue);
    return snapshot;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((ManageableYieldCurveSnapshot) parameters[0], (String) parameters[1], (ExternalId) parameters[2], (Double) parameters[3], (Double) parameters[4]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
