/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.ImmutableBean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
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
 * Tests the {@link SetObjectPropertyFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class SetObjectPropertyFunctionTest {

  private SessionContext createSessionContext() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setTypeConverters(new Converters());
    return testUtils.createSessionContext();
  }

  public void testFlexiBean() {
    final SessionContext context = createSessionContext();
    final FlexiBean bean = new FlexiBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "foo", DataUtils.of("XYZ"), null);
    assertEquals(((FlexiBean) result).get("foo"), "XYZ");
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "bar", DataUtils.of(42), null);
    assertEquals(((FlexiBean) result).get("bar"), 42);
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "bar", DataUtils.of(42), "float");
    assertEquals(((FlexiBean) result).get("bar"), (float) 42);
    result = SetObjectPropertyFunction.invoke(context, beanMsg, "cow", new Data(), null);
    assertEquals(((FlexiBean) result).get("cow"), null);
  }

  public void testImmutableBean() {
    final SessionContext context = createSessionContext();
    final CurrencyAmount bean = CurrencyAmount.of(Currency.GBP, 42d);
    assertTrue(bean instanceof ImmutableBean);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "currency", DataUtils.of("USD"), null);
    assertEquals(((CurrencyAmount) result).getCurrency(), Currency.USD);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidBeanField() {
    final SessionContext context = createSessionContext();
    final SimpleExchange bean = new SimpleExchange();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertyFunction.invoke(context, beanMsg, "nonExistentField", DataUtils.of("Test"), null);
  }

  public void testMutableBean() {
    final SessionContext context = createSessionContext();
    final SimpleExchange bean = new SimpleExchange();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "name", DataUtils.of("Test"), null);
    assertEquals(((SimpleExchange) result).getName(), "Test");
  }

  public void testNonBean() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "x", DataUtils.of(42), null);
    assertEquals(((ObjectFunctionTest.NonJodaBean) result).getX(), 42);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidNonBeanField1() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertyFunction.invoke(context, beanMsg, "y", DataUtils.of(42), null);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidNonBeanField2() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertyFunction.invoke(context, beanMsg, "class", DataUtils.of(SimpleExchange.class.getName()), null);
  }

  public void testNonObject() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = (new FudgeSerializer(FudgeTypeConverter.getFudgeContext(context.getGlobalContext()))).objectToFudgeMsg(bean);
    // Check we've got no type information or hints
    assertEquals(beanMsg.toString(), "FudgeMsg[x => 42]");
    Object result = SetObjectPropertyFunction.invoke(context, beanMsg, "y", DataUtils.of("ABC"), null);
    assertEquals(((FudgeMsg) result).getInt("x"), (Integer) 42);
    assertEquals(((FudgeMsg) result).getString("y"), "ABC");
  }

}
