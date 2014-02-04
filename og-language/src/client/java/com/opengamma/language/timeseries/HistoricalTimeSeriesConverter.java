/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.types.TimeSeriesTypes;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Converts {@link HistoricalTimeSeries} to {@link LocalDateDoubleTimeSeries}. The reverse conversion is not possible as a unique identifier is not available. Any functions should probably be working
 * with LocalDateDoubleTimeSeries to do anything useful.
 */
public class HistoricalTimeSeriesConverter extends AbstractMappedConverter {

  /**
   * Default instance.
   */
  public static final HistoricalTimeSeriesConverter INSTANCE = new HistoricalTimeSeriesConverter();

  protected HistoricalTimeSeriesConverter() {
    conversion(TypeMap.ZERO_LOSS, TimeSeriesTypes.HISTORICAL_TIME_SERIES, TimeSeriesTypes.LOCAL_DATE_DOUBLE_TIME_SERIES, new Action<HistoricalTimeSeries, LocalDateDoubleTimeSeries>() {
      @Override
      protected LocalDateDoubleTimeSeries convert(final HistoricalTimeSeries value) {
        return value.getTimeSeries();
      }
    });
  }

}
