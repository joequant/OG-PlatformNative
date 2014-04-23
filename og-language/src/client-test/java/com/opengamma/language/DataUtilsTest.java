/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the methods in the {@link DataUtils} class.
 */
@Test(groups = TestGroup.UNIT)
public class DataUtilsTest {

  public void testNull() {
    assertTrue(DataUtils.isNull(new Data()));
  }

  public void testSingle() {
    final Value value = ValueUtils.of(42);
    final Data data = DataUtils.of(value);
    assertNotNull(data);
    assertFalse(DataUtils.isNull(data));
    assertEquals(data.getSingle(), value);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSingleNull() {
    DataUtils.of((Value) null);
  }

  public void testLinear() {
    final Value[] values = new Value[] {ValueUtils.of(1), ValueUtils.of(2) };
    final Data data = DataUtils.of(values);
    assertNotNull(data);
    assertFalse(DataUtils.isNull(data));
    assertArrayEquals(data.getLinear(), values);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLinearNull() {
    DataUtils.of((Value[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLinearNullElement() {
    final Value[] values = new Value[] {ValueUtils.of(1), null, ValueUtils.of(2) };
    DataUtils.of(values);
  }

  public void testMatrix() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtils.of(1), ValueUtils.of(2) }, new Value[] {ValueUtils.of(3), ValueUtils.of(4) } };
    final Data data = DataUtils.of(values);
    assertNotNull(data);
    assertFalse(DataUtils.isNull(data));
    assertArrayEquals(data.getMatrix(), values);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMatrixNull() {
    DataUtils.of((Value[][]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMatrixNullElement() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtils.of(1), null }, new Value[] {ValueUtils.of(3), ValueUtils.of(4) } };
    DataUtils.of(values);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMatrixNullArray() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtils.of(1), ValueUtils.of(2) }, null };
    DataUtils.of(values);
  }

  public void testToString() {
    assertEquals(DataUtils.toString(null, false), null);
    assertEquals(DataUtils.toString(null, true), null);
    final Data data = new Data();
    assertEquals(DataUtils.toString(data, false), "Data");
    assertEquals(DataUtils.toString(data, true), "Data");
    final Value value = new Value();
    data.setSingle(value);
    assertEquals(DataUtils.toString(data, false), "");
    assertEquals(DataUtils.toString(data, true), "");
    value.setStringValue("Foo");
    data.setSingle(value);
    assertEquals(DataUtils.toString(data, false), "Foo");
    assertEquals(DataUtils.toString(data, true), "\"Foo\"");
    data.setSingle(null);
    data.setLinear(new Value[] {value, value });
    data.setLinear(new Value[] {value, value });
    assertEquals(DataUtils.toString(data, false), "[Foo, Foo]");
    assertEquals(DataUtils.toString(data, true), "[\"Foo\", \"Foo\"]");
    data.setLinear(null);
    data.setMatrix(new Value[2][0]);
    assertEquals(DataUtils.toString(data, false), "[[], []]");
    assertEquals(DataUtils.toString(data, true), "[[], []]");
    data.setMatrix(new Value[][] { {value, value }, {value, value } });
    assertEquals(DataUtils.toString(data, false), "[[Foo, Foo], [Foo, Foo]]");
    assertEquals(DataUtils.toString(data, true), "[[\"Foo\", \"Foo\"], [\"Foo\", \"Foo\"]]");
  }

  @SuppressWarnings("deprecation")
  public void testToBool() {
    assertEquals(DataUtils.toBool(new Data()), null);
    assertEquals(DataUtils.toBool(DataUtils.of(true)), Boolean.TRUE);
    assertEquals(DataUtils.toBool(DataUtils.of("False")), Boolean.FALSE);
    assertEquals(DataUtils.toBool(DataUtils.of("")), null);
    assertEquals(DataUtils.toBool(DataUtils.of(1d)), Boolean.TRUE);
    assertEquals(DataUtils.toBool(DataUtils.of(0)), Boolean.FALSE);
    assertEquals(DataUtils.toBool(DataUtils.ofError(42)), null);
    assertEquals(DataUtils.toBool(DataUtils.of(FudgeContext.EMPTY_MESSAGE)), null);
  }

}
