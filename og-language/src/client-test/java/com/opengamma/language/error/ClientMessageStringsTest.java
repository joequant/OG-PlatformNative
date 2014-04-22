/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.language.DataUtils;
import com.opengamma.language.ValueUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the basic {@link ClientMessageStrings} implementations.
 */
@Test(groups = TestGroup.UNIT)
public class ClientMessageStringsTest {

  public void testDefaultToString() {
    assertEquals(ClientMessageStrings.DEFAULT_TO_STRING.toString(ValueUtils.of("Foo")),
        "Value[_boolValue=<null>,_intValue=<null>,_doubleValue=<null>,_stringValue=Foo,_messageValue=<null>,_errorValue=<null>]");
    assertEquals(ClientMessageStrings.DEFAULT_TO_STRING.toString(DataUtils.of("Foo")),
        "Data[_single=Value[_boolValue=<null>,_intValue=<null>,_doubleValue=<null>,_stringValue=Foo,_messageValue=<null>,_errorValue=<null>],_linear=<null>,_matrix=<null>]");
  }

  public void testQuotedForm() {
    assertEquals(ClientMessageStrings.QUOTED_FORM.toString(ValueUtils.of("Foo")), "\"Foo\"");
    assertEquals(ClientMessageStrings.QUOTED_FORM.toString(DataUtils.of("Foo")), "\"Foo\"");
  }

  public void testUnquotedForm() {
    assertEquals(ClientMessageStrings.UNQUOTED_FORM.toString(ValueUtils.of("Foo")), "Foo");
    assertEquals(ClientMessageStrings.UNQUOTED_FORM.toString(DataUtils.of("Foo")), "Foo");
  }

  public void testSimpleForm() {
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(ValueUtils.of("Foo")), "Foo");
    assertEquals(ClientMessageStrings.SIMPLE_FORM.toString(DataUtils.of("Foo")), "Foo");
  }

}
