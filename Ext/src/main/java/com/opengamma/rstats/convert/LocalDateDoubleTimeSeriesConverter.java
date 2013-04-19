/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.rstats.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;
import com.opengamma.rstats.data.RDataInfo;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.time.DateUtils;

/**
 * Converts a {@link LocalDateDoubleTimeSeries} to/from an R time-series wrapper.
 */
public class LocalDateDoubleTimeSeriesConverter extends AbstractTypeConverter {

  private static final Value NULL = new Value();

  // TODO: handle nulls

  private static final JavaTypeInfo<Data> DATA = JavaTypeInfo.builder(Data.class).get();
  private static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();
  private static final JavaTypeInfo<LocalDate> LOCAL_DATE = JavaTypeInfo.builder(LocalDate.class).get();
  private static final JavaTypeInfo<LocalDateDoubleTimeSeries> LOCAL_DATE_DOUBLE_TIME_SERIES = JavaTypeInfo.builder(LocalDateDoubleTimeSeries.class).get();

  private static final TypeMap TO_LOCAL_DATE_DOUBLE_TIME_SERIES = TypeMap.of(ZERO_LOSS, DATA);
  private static final TypeMap FROM_LOCAL_DATE_DOUBLE_TIME_SERIES = TypeMap.of(ZERO_LOSS, LOCAL_DATE_DOUBLE_TIME_SERIES);

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == LocalDateDoubleTimeSeries.class) || (targetType.getRawClass() == Data.class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (type.getRawClass() == LocalDateDoubleTimeSeries.class) {
      // Converting from Data to LocalDateDoubleTimeSeries
      final Value[] values = ((Data) value).getLinear();
      if (values == null) {
        conversionContext.setFail();
        return;
      }
      final List<LocalDate> timeSeriesDates = new ArrayList<LocalDate>(values.length - 1);
      final List<Double> timeSeriesValues = new ArrayList<Double>(values.length - 1);
      conversionContext.convertValue(values[0], LOCAL_DATE);
      if (conversionContext.isFailed()) {
        conversionContext.setFail();
        return;
      }
      LocalDate iDate = conversionContext.getResult();
      int skip = 0;
      for (int i = 1; i < values.length; i++) {
        if (!ValueUtils.isNull(values[i])) {
          final Double v = ValueUtils.toDouble(values[i]);
          if (!Double.isNaN(v)) {
            iDate = iDate.plusDays(skip);
            timeSeriesDates.add(iDate);
            timeSeriesValues.add(v);
            skip = 1;
          } else {
            skip++;
          }
        } else {
          skip++;
        }
      }
      conversionContext.setResult(ImmutableLocalDateDoubleTimeSeries.of(timeSeriesDates, timeSeriesValues));
    } else {
      // Converting from LocalDateDoubleTimeSeries to Data
      final LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) value;
      final LocalDate earliest = timeSeries.getEarliestTime();
      final LocalDate latest = timeSeries.getLatestTime();
      final int size = DateUtils.getDaysBetween(earliest, true, latest, true);
      final Value[] values = new Value[size + 1];
      conversionContext.convertValue(earliest, VALUE);
      if (conversionContext.isFailed()) {
        conversionContext.setFail();
        return;
      }
      values[0] = conversionContext.getResult();
      final Iterator<Map.Entry<LocalDate, Double>> entries = timeSeries.iterator();
      int i = 1;
      LocalDate iDate = earliest;
      while (entries.hasNext()) {
        final Map.Entry<LocalDate, Double> entry = entries.next();
        int skip = DateUtils.getDaysBetween(iDate, false, entry.getKey(), false);
        while (skip > 0) {
          values[i++] = NULL;
          skip--;
        }
        iDate = entry.getKey();
        values[i++] = ValueUtils.of(entry.getValue());
      }
      conversionContext.setResult(RDataInfo.create().wrapperClass("TimeSeries").applyTo(DataUtils.of(values)));
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == LocalDateDoubleTimeSeries.class) {
      return TO_LOCAL_DATE_DOUBLE_TIME_SERIES;
    } else {
      return FROM_LOCAL_DATE_DOUBLE_TIME_SERIES;
    }
  }

}
