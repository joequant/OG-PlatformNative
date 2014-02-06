/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link GetObjectPropertyFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class GetObjectPropertyFunctionTest {

  private SessionContext createSessionContext() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setTypeConverters(new Converters());
    return testUtils.createSessionContext();
  }

  public void testFlexiBean() {
    final SessionContext context = createSessionContext();
    final FlexiBean bean = new FlexiBean();
    bean.set("foo", "XYZ");
    bean.set("bar", 42d);
    bean.set("cow", null);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "foo"), "XYZ");
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "bar"), 42d);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "cow"), null);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "class"), FlexiBean.class.getName());
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testFlexiBean_fieldNotFound() {
    final SessionContext context = createSessionContext();
    final FlexiBean bean = new FlexiBean();
    bean.set("foo", "XYZ");
    bean.set("bar", 42d);
    bean.set("cow", null);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    GetObjectPropertyFunction.invoke(context, beanMsg, "dog");
  }

  public void testDirectBean() {
    final SessionContext context = createSessionContext();
    final CurrencyAmount bean = CurrencyAmount.of(Currency.GBP, 42d);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "currency"), Currency.GBP);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "amount"), 42d);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "class"), CurrencyAmount.class.getName());
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testDirectBean_fieldNotFound() {
    final SessionContext context = createSessionContext();
    final CurrencyAmount bean = CurrencyAmount.of(Currency.GBP, 42d);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    GetObjectPropertyFunction.invoke(context, beanMsg, "foo");
  }

  public void testNonBean() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "x"), 42);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "class"), ObjectFunctionTest.NonJodaBean.class.getName());
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testNonBean_fieldNotFound() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    GetObjectPropertyFunction.invoke(context, beanMsg, "y");
  }

  public void testNonObject() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = (new FudgeSerializer(FudgeTypeConverter.getFudgeContext(context.getGlobalContext()))).objectToFudgeMsg(bean);
    // Check we've got no type information or hints
    assertEquals(beanMsg.toString(), "FudgeMsg[x => 42]");
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "x"), (byte) 42);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "y"), null);
    assertEquals(GetObjectPropertyFunction.invoke(context, beanMsg, "class"), null);
  }

}
