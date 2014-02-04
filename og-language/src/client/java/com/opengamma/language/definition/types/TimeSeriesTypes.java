/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to time-series types.
 */
public final class TimeSeriesTypes {

  /**
   * Prevents instantiation.
   */
  private TimeSeriesTypes() {
  }

  public static final JavaTypeInfo<HistoricalTimeSeries> HISTORICAL_TIME_SERIES = JavaTypeInfo.builder(HistoricalTimeSeries.class).get();

  public static final JavaTypeInfo<LocalDateDoubleTimeSeries> LOCAL_DATE_DOUBLE_TIME_SERIES = JavaTypeInfo.builder(LocalDateDoubleTimeSeries.class).get();

}
