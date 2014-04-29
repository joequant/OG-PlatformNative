/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the basic {@link SimpleClientMessageStrings} implementation.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleClientMessageStringsTest {

  @Test
  public void testValues() {
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString((Value) null), null);
    final Value v = new Value();
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "empty value");
    v.setBoolValue(true);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "42");
    v.setIntValue(null);
    final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    v.setMessageValue(msg);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "message encoded object");
    msg.add(0, "com.opengamma.example.MockObject");
    msg.add("Foo", "Bar");
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "MockObject");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Foo\"Bar");
    v.setStringValue(null);
    v.setErrorValue(15);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Error 15");
    v.setBoolValue(true);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Error 15: true");
    v.setBoolValue(null);
    v.setDoubleValue(3.14);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Error 15: 3.14");
    v.setDoubleValue(null);
    v.setIntValue(42);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Error 15: 42");
    v.setIntValue(null);
    v.setMessageValue(msg);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Error 15: MockObject");
    v.setMessageValue(null);
    v.setStringValue("Foo\"Bar");
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(v), "Error 15: Foo\"Bar");
  }

  public void testData() {
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString((Data) null), null);
    final Data data = new Data();
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(data), "empty value");
    final Value value = new Value();
    data.setSingle(value);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(data), "empty value");
    value.setStringValue("Foo");
    data.setSingle(value);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(data), "Foo");
    data.setSingle(null);
    data.setLinear(new Value[] {value, value });
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(data), "Data[2]");
    data.setLinear(null);
    data.setMatrix(new Value[2][0]);
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(data), "Data[2][0]");
    data.setMatrix(new Value[][] { {value, value }, {value, value } });
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(data), "Data[2][2]");
  }

}
