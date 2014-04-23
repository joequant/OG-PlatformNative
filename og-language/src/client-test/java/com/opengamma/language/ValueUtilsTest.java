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

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the methods in the {@link ValueUtils} class.
 */
@Test(groups = TestGroup.UNIT)
public class ValueUtilsTest {

  public void testNull() {
    assertTrue(ValueUtils.isNull(null));
    final Value value = new Value();
    assertTrue(ValueUtils.isNull(value));
  }

  public void testBoolean() {
    final Value value = ValueUtils.of(true);
    assertNotNull(value);
    assertFalse(ValueUtils.isNull(value));
    assertEquals(value.getBoolValue(), Boolean.TRUE);
  }

  public void testDouble() {
    final Value value = ValueUtils.of(3.14);
    assertNotNull(value);
    assertFalse(ValueUtils.isNull(value));
    assertEquals(value.getDoubleValue(), 3.14, 0);
  }

  public void testError() {
    final Value value = ValueUtils.ofError(42);
    assertNotNull(value);
    assertFalse(ValueUtils.isNull(value));
    assertEquals(value.getErrorValue(), (Integer) 42);
  }

  public void testInteger() {
    final Value value = ValueUtils.of(69);
    assertNotNull(value);
    assertFalse(ValueUtils.isNull(value));
    assertEquals(value.getIntValue(), (Integer) 69);
  }

  public void testMessage() {
    final Value value = ValueUtils.of(FudgeContext.EMPTY_MESSAGE);
    assertNotNull(value);
    assertFalse(ValueUtils.isNull(value));
    assertEquals(value.getMessageValue(), FudgeContext.EMPTY_MESSAGE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMessage() {
    ValueUtils.of((FudgeMsg) null);
  }

  public void testString() {
    final Value value = ValueUtils.of("Foo");
    assertNotNull(value);
    assertFalse(ValueUtils.isNull(value));
    assertEquals(value.getStringValue(), "Foo");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullString() {
    ValueUtils.of((String) null);
  }

  public void testData() {
    final FudgeContext ctx = FudgeContext.GLOBAL_DEFAULT;
    assertEquals(ValueUtils.of(ctx, null), new Value());
    assertEquals(ValueUtils.of(ctx, new Data()), new Value());
    assertEquals(ValueUtils.of(ctx, DataUtils.of(42)), ValueUtils.of((Integer) 42));
    assertEquals(ValueUtils.of(ctx, DataUtils.of(new Value[0])), new Value());
    assertEquals(ValueUtils.of(ctx, DataUtils.of(new Value[1][0])), new Value());
    assertEquals(ValueUtils.of(ctx, DataUtils.of(new Value[] {ValueUtils.of("A") })), ValueUtils.of("A"));
    assertEquals(ValueUtils.of(ctx, DataUtils.of(new Value[][] {{ValueUtils.of("A") } })), ValueUtils.of("A"));
    Data d = DataUtils.of(new Value[] {ValueUtils.of("A"), ValueUtils.of("B") });
    Value v = ValueUtils.of(ctx, d);
    assertNotNull(v.getMessageValue());
    assertEquals(ctx.fromFudgeMsg(v.getMessageValue()), d);
    d = DataUtils.of(new Value[][] { {ValueUtils.of("A"), ValueUtils.of("B") }, {ValueUtils.of("X"), ValueUtils.of("Y") } });
    v = ValueUtils.of(ctx, d);
    assertNotNull(v.getMessageValue());
    assertEquals(ctx.fromFudgeMsg(v.getMessageValue()), d);
  }

  public void testTranspose() {
    Value[][] orig = new Value[2][5];
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 5; j++) {
        orig[i][j] = ValueUtils.of(i * j);
      }
    }
    Value[][] transposed = ValueUtils.transpose(orig);
    assertEquals(transposed.length, 5);
    assertEquals(transposed[0].length, 2);
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(transposed[i][j], orig[j][i]);
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

  public void testCompare() {
    final Value[] vs = new Value[] {ValueUtils.ofError(0), ValueUtils.ofError(1), ValueUtils.of(false), ValueUtils.of(true), ValueUtils.of(0), ValueUtils.of(1), ValueUtils.of(0d),
        ValueUtils.of(1d), ValueUtils.of("A"), ValueUtils.of("B"), ValueUtils.of(FudgeContext.EMPTY_MESSAGE), new Value() };
    for (int i = 0; i < vs.length; i++) {
      assertEquals(ValueUtils.compare(vs[i], vs[i]), 0);
      for (int j = i + 1; j < vs.length; j++) {
        assertTrue(ValueUtils.compare(vs[i], vs[j]) < 0);
        assertTrue(ValueUtils.compare(vs[j], vs[i]) > 0);
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void testToBool() {
    assertEquals(ValueUtils.toBool(null), null);
    assertEquals(ValueUtils.toBool(new Value()), null);
    assertEquals(ValueUtils.toBool(ValueUtils.of((Integer) 0)), Boolean.FALSE);
    assertEquals(ValueUtils.toBool(ValueUtils.of((Integer) 1)), Boolean.TRUE);
    assertEquals(ValueUtils.toBool(ValueUtils.of((Integer) 2)), Boolean.TRUE);
    assertEquals(ValueUtils.toBool(ValueUtils.of(0d)), Boolean.FALSE);
    assertEquals(ValueUtils.toBool(ValueUtils.of(1d)), Boolean.TRUE);
    assertEquals(ValueUtils.toBool(ValueUtils.of(2d)), Boolean.TRUE);
    assertEquals(ValueUtils.toBool(ValueUtils.of("")), null);
    assertEquals(ValueUtils.toBool(ValueUtils.of("True")), Boolean.TRUE);
    assertEquals(ValueUtils.toBool(ValueUtils.of("False")), Boolean.FALSE);
    assertEquals(ValueUtils.toBool(ValueUtils.ofError(1)), null);
  }

  @SuppressWarnings("deprecation")
  public void testToDouble() {
    assertEquals(ValueUtils.toDouble(null), null);
    assertEquals(ValueUtils.toDouble(new Value()), null);
    assertEquals(ValueUtils.toDouble(ValueUtils.of((Integer) 0)), 0d);
    assertEquals(ValueUtils.toDouble(ValueUtils.of((Integer) 1)), 1d);
    assertEquals(ValueUtils.toDouble(ValueUtils.of((Integer) 2)), 2d);
    assertEquals(ValueUtils.toDouble(ValueUtils.of(0d)), 0d);
    assertEquals(ValueUtils.toDouble(ValueUtils.of(1d)), 1d);
    assertEquals(ValueUtils.toDouble(ValueUtils.of(2d)), 2d);
    assertEquals(ValueUtils.toDouble(ValueUtils.of("")), null);
    assertEquals(ValueUtils.toDouble(ValueUtils.of("3.14")), 3.14);
    assertEquals(ValueUtils.toDouble(ValueUtils.ofError(1)), null);
  }

  @SuppressWarnings("deprecation")
  public void testToInt() {
    assertEquals(ValueUtils.toInt(null), null);
    assertEquals(ValueUtils.toInt(new Value()), null);
    assertEquals(ValueUtils.toInt(ValueUtils.of((Integer) 0)), (Integer) 0);
    assertEquals(ValueUtils.toInt(ValueUtils.of((Integer) 1)), (Integer) 1);
    assertEquals(ValueUtils.toInt(ValueUtils.of((Integer) 2)), (Integer) 2);
    assertEquals(ValueUtils.toInt(ValueUtils.of(0d)), (Integer) 0);
    assertEquals(ValueUtils.toInt(ValueUtils.of(3.14d)), (Integer) 3);
    assertEquals(ValueUtils.toInt(ValueUtils.of("")), null);
    assertEquals(ValueUtils.toInt(ValueUtils.of("42")), (Integer) 42);
    assertEquals(ValueUtils.toInt(ValueUtils.ofError(1)), null);
  }

  public void testToString() {
    assertEquals(ValueUtils.toString(null, false), null);
    assertEquals(ValueUtils.toString(null, true), null);
    final Value v = new Value();
    assertEquals(ValueUtils.toString(v, false), "");
    assertEquals(ValueUtils.toString(v, true), "");
    v.setBoolValue(true);
    assertEquals(ValueUtils.toString(v, false), "true");
    assertEquals(ValueUtils.toString(v, true), "true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    assertEquals(ValueUtils.toString(v, false), "3.14");
    assertEquals(ValueUtils.toString(v, true), "3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    assertEquals(ValueUtils.toString(v, false), "42");
    assertEquals(ValueUtils.toString(v, true), "42");
    v.setIntValue(null);
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    v.setMessageValue(msg);
    assertEquals(ValueUtils.toString(v, false), "FudgeMsg[]");
    assertEquals(ValueUtils.toString(v, true), "FudgeMsg[]");
    msg.add(0, "com.opengamma.example.MockObject");
    msg.add("Foo", "Bar");
    assertEquals(ValueUtils.toString(v, false), "FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]");
    assertEquals(ValueUtils.toString(v, true), "FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    assertEquals(ValueUtils.toString(v, false), "Foo\"Bar");
    assertEquals(ValueUtils.toString(v, true), "\"Foo\\\"Bar\"");
    v.setStringValue(null);
    v.setErrorValue(15);
    assertEquals(ValueUtils.toString(v, false), "Error 15");
    assertEquals(ValueUtils.toString(v, true), "Error 15");
    v.setBoolValue(true);
    assertEquals(ValueUtils.toString(v, false), "Error 15: true");
    assertEquals(ValueUtils.toString(v, true), "Error 15: true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    assertEquals(ValueUtils.toString(v, false), "Error 15: 3.14");
    assertEquals(ValueUtils.toString(v, true), "Error 15: 3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    assertEquals(ValueUtils.toString(v, false), "Error 15: 42");
    assertEquals(ValueUtils.toString(v, true), "Error 15: 42");
    v.setIntValue(null);
    v.setMessageValue(msg);
    assertEquals(ValueUtils.toString(v, false), "Error 15: FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]");
    assertEquals(ValueUtils.toString(v, true), "Error 15: FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    assertEquals(ValueUtils.toString(v, false), "Error 15: Foo\"Bar");
    assertEquals(ValueUtils.toString(v, true), "Error 15: \"Foo\\\"Bar\"");
  }

}
