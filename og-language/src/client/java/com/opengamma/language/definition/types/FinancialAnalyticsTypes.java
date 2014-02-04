/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix2D;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to OG-Financial analytics objects.
 */
@SuppressWarnings("rawtypes")
public final class FinancialAnalyticsTypes {

  /**
   * Prevents instantiation.
   */
  private FinancialAnalyticsTypes() {
  }

  public static final JavaTypeInfo<DayCount> DAY_COUNT = JavaTypeInfo.builder(DayCount.class).get();

  public static final JavaTypeInfo<LabelledMatrix2D> LABELLED_MATRIX_2D = JavaTypeInfo.builder(LabelledMatrix2D.class).get();

  public static final JavaTypeInfo<LabelledMatrix2D> LABELLED_MATRIX_2D_ALLOW_NULL = JavaTypeInfo.builder(LabelledMatrix2D.class).allowNull().get();

  public static final JavaTypeInfo<LabelledMatrix1D> LABELLED_MATRIX_1D = JavaTypeInfo.builder(LabelledMatrix1D.class).get();

  //public static final JavaTypeInfo<LabelledMatrix1D> LABELLED_MATRIX_1D_ALLOW_NULL = JavaTypeInfo.builder(LabelledMatrix1D.class).allowNull().get();

}
