/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the methods in the {@link ValueUtils} class.
 */
@Test(groups = TestGroup.UNIT)
public class ValueUtilsTest {

  @Test
  public void testBoolean() {
    final Value value = ValueUtils.of(true);
    assertNotNull(value);
    assertEquals(Boolean.TRUE, value.getBoolValue());
  }

  @Test
  public void testDouble() {
    final Value value = ValueUtils.of(3.14);
    assertNotNull(value);
    assertEquals(3.14, value.getDoubleValue(), 0);
  }

  @Test
  public void testError() {
    final Value value = ValueUtils.ofError(42);
    assertNotNull(value);
    assertEquals((Integer) 42, value.getErrorValue());
  }

  @Test
  public void testInteger() {
    final Value value = ValueUtils.of(69);
    assertNotNull(value);
    assertEquals((Integer) 69, value.getIntValue());
  }

  @Test
  public void testMessage() {
    final Value value = ValueUtils.of(FudgeContext.EMPTY_MESSAGE);
    assertNotNull(value);
    assertEquals(FudgeContext.EMPTY_MESSAGE, value.getMessageValue());
  }

  @Test
  public void testString() {
    final Value value = ValueUtils.of("Foo");
    assertNotNull(value);
    assertEquals("Foo", value.getStringValue());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullString() {
    ValueUtils.of((String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMessage() {
    ValueUtils.of((FudgeMsg) null);
  }

  @Test
  public void testTranspose() {
    Value[][] orig = new Value[2][5];
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 5; j++) {
        orig[i][j] = ValueUtils.of(i * j);
      }
    }
    Value[][] transposed = ValueUtils.transpose(orig);
    assertEquals(5, transposed.length);
    assertEquals(2, transposed[0].length);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(orig[j][i], transposed[i][j]);
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTransposeJagged() {
    Value[][] orig = new Value[2][];
    orig[0] = new Value[3];
    orig[1] = new Value[2];
    ValueUtils.transpose(orig);
  }

  @Test
  public void testCompare() {
    final Value[] vs = new Value[] {ValueUtils.ofError(0), ValueUtils.ofError(1), ValueUtils.of(false), ValueUtils.of(true), ValueUtils.of(0), ValueUtils.of(1), ValueUtils.of(0d),
        ValueUtils.of(1d), ValueUtils.of("A"), ValueUtils.of("B"), ValueUtils.of(FudgeContext.EMPTY_MESSAGE), new Value() };
    for (int i = 0; i < vs.length; i++) {
      assertEquals(0, ValueUtils.compare(vs[i], vs[i]));
      for (int j = i + 1; j < vs.length; j++) {
        assertTrue(ValueUtils.compare(vs[i], vs[j]) < 0);
        assertTrue(ValueUtils.compare(vs[j], vs[i]) > 0);
      }
    }
  }

}
