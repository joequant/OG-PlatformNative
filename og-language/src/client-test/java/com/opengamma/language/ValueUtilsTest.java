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
import org.fudgemsg.MutableFudgeMsg;
import org.testng.Assert;
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

  @Test
  public void testToString() {
    Assert.assertEquals(ValueUtils.toString(null, false), null);
    Assert.assertEquals(ValueUtils.toString(null, true), null);
    final Value v = new Value();
    Assert.assertEquals(ValueUtils.toString(v, false), "");
    Assert.assertEquals(ValueUtils.toString(v, true), "");
    v.setBoolValue(true);
    Assert.assertEquals(ValueUtils.toString(v, false), "true");
    Assert.assertEquals(ValueUtils.toString(v, true), "true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    Assert.assertEquals(ValueUtils.toString(v, false), "3.14");
    Assert.assertEquals(ValueUtils.toString(v, true), "3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    Assert.assertEquals(ValueUtils.toString(v, false), "42");
    Assert.assertEquals(ValueUtils.toString(v, true), "42");
    v.setIntValue(null);
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    v.setMessageValue(msg);
    Assert.assertEquals(ValueUtils.toString(v, false), "FudgeMsg[]");
    Assert.assertEquals(ValueUtils.toString(v, true), "FudgeMsg[]");
    msg.add(0, "com.opengamma.example.MockObject");
    msg.add("Foo", "Bar");
    Assert.assertEquals(ValueUtils.toString(v, false), "FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]");
    Assert.assertEquals(ValueUtils.toString(v, true), "FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    Assert.assertEquals(ValueUtils.toString(v, false), "Foo\"Bar");
    Assert.assertEquals(ValueUtils.toString(v, true), "\"Foo\\\"Bar\"");
    v.setStringValue(null);
    v.setErrorValue(15);
    Assert.assertEquals(ValueUtils.toString(v, false), "{Error 15}");
    Assert.assertEquals(ValueUtils.toString(v, true), "{Error 15}");
    v.setBoolValue(true);
    Assert.assertEquals(ValueUtils.toString(v, false), "{Error 15, true}");
    Assert.assertEquals(ValueUtils.toString(v, true), "{Error 15, true}");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    Assert.assertEquals(ValueUtils.toString(v, false), "{Error 15, 3.14}");
    Assert.assertEquals(ValueUtils.toString(v, true), "{Error 15, 3.14}");
    v.setDoubleValue(null);
    v.setIntValue(42);
    Assert.assertEquals(ValueUtils.toString(v, false), "{Error 15, 42}");
    Assert.assertEquals(ValueUtils.toString(v, true), "{Error 15, 42}");
    v.setIntValue(null);
    v.setMessageValue(msg);
    Assert.assertEquals(ValueUtils.toString(v, false), "{Error 15, FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]}");
    Assert.assertEquals(ValueUtils.toString(v, true), "{Error 15, FudgeMsg[0:  => com.opengamma.example.MockObject, Foo => Bar]}");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    Assert.assertEquals(ValueUtils.toString(v, false), "{Error 15, Foo\"Bar}");
    Assert.assertEquals(ValueUtils.toString(v, true), "{Error 15, \"Foo\\\"Bar\"}");
  }

  @Test
  public void testToSimpleString() {
    Assert.assertEquals(ValueUtils.toSimpleString(null), null);
    final Value v = new Value();
    Assert.assertEquals(ValueUtils.toSimpleString(v), "");
    v.setBoolValue(true);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "42");
    v.setIntValue(null);
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    v.setMessageValue(msg);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Message encoded object");
    msg.add(0, "com.opengamma.example.MockObject");
    msg.add("Foo", "Bar");
    Assert.assertEquals(ValueUtils.toSimpleString(v), "MockObject");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Foo\"Bar");
    v.setStringValue(null);
    v.setErrorValue(15);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Error 15");
    v.setBoolValue(true);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Error 15: true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Error 15: 3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Error 15: 42");
    v.setIntValue(null);
    v.setMessageValue(msg);
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Error 15: MockObject");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    Assert.assertEquals(ValueUtils.toSimpleString(v), "Error 15: Foo\"Bar");
  }

}
