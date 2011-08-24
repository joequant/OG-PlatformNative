/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.rstats.convert;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.rstats.data.RDataInfo;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Tests the {@link LocalDateDoubleTimeSeriesConverter} class.
 */
@Test
public class LocalDateDoubleTimeSeriesConverterTest extends AbstractConverterTest {

  private LocalDateDoubleTimeSeries createTimeSeries() {
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2010, 4, 1), LocalDate.of(2010, 4, 4), LocalDate.of(2010, 4, 5) };
    final double[] values = new double[] {4, 5, 6 };
    return new ArrayLocalDateDoubleTimeSeries(dates, values);
  }

  private Data createData() {
    final Value[] expectedValues = new Value[] {ValueUtils.of("2010-04-01"), ValueUtils.of(4d), new Value(), new Value(), ValueUtils.of(5d), ValueUtils.of(6d) };
    return DataUtils.of(expectedValues);
  }

  public void testFromData() {
    final LocalDateDoubleTimeSeriesConverter converter = new LocalDateDoubleTimeSeriesConverter();
    assertValidConversion(converter, createData(), JavaTypeInfo.builder(LocalDateDoubleTimeSeries.class).get(), createTimeSeries());
  }

  public void testToData () {
    final LocalDateDoubleTimeSeriesConverter converter = new LocalDateDoubleTimeSeriesConverter ();
    final Data expectedData = RDataInfo.create().wrapperClass("TimeSeries").applyTo(createData());
    assertValidConversion(converter, createTimeSeries(), JavaTypeInfo.builder(Data.class).get(), expectedData);
  }
}
