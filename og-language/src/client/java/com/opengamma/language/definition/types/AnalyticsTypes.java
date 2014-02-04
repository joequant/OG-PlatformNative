/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to OG-Analytics analytics objects.
 */
@SuppressWarnings("rawtypes")
public final class AnalyticsTypes {

  /**
   * Prevents instantiation.
   */
  private AnalyticsTypes() {
  }

  public static final JavaTypeInfo<Curve> CURVE = JavaTypeInfo.builder(Curve.class).get();

  public static final JavaTypeInfo<MulticurveProviderDiscount> MULTICURVE_PROVIDER_DISCOUNT = JavaTypeInfo.builder(MulticurveProviderDiscount.class).get();

  public static final JavaTypeInfo<YieldCurve> YIELD_CURVE = JavaTypeInfo.builder(YieldCurve.class).get();

}
