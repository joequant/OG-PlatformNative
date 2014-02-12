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

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SetObjectPropertiesFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class SetObjectPropertiesFunctionTest {

  public void testFlexiBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final FlexiBean bean = new FlexiBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("foo", DataUtils.of("XYZ"), "bar", DataUtils.of(42), "cow", new Data()));
    assertEquals(((FlexiBean) result).get("foo"), "XYZ");
    assertEquals(((FlexiBean) result).get("bar"), 42);
    assertEquals(((FlexiBean) result).get("cow"), null);
  }

  public void testImmutableBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final CurrencyAmount bean = CurrencyAmount.of(Currency.GBP, 42d);
    assertTrue(bean instanceof ImmutableBean);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("currency", DataUtils.of("USD")));
    assertEquals(((CurrencyAmount) result).getCurrency(), Currency.USD);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidBeanField() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final SimpleExchange bean = new SimpleExchange();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("nonExistentField", DataUtils.of("Test")));
  }

  public void testMutableBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final SimpleExchange bean = new SimpleExchange();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("name", DataUtils.of("Test")));
    assertEquals(((SimpleExchange) result).getName(), "Test");
  }

  public void testNonBean() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    Object result = SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("x", DataUtils.of(42)));
    assertEquals(((ObjectFunctionTest.NonJodaBean) result).getX(), 42);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidNonBeanField1() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("y", DataUtils.of(42)));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidNonBeanField2() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("class", DataUtils.of(SimpleExchange.class.getName())));
  }

  public void testNonObject() {
    final SessionContext context = ObjectFunctionTest.createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = (new FudgeSerializer(FudgeTypeConverter.getFudgeContext(context.getGlobalContext()))).objectToFudgeMsg(bean);
    // Check we've got no type information or hints
    assertEquals(beanMsg.toString(), "FudgeMsg[x => 42]");
    Object result = SetObjectPropertiesFunction.invoke(context, beanMsg, ImmutableMap.of("y", DataUtils.of("ABC")));
    assertEquals(((FudgeMsg) result).getInt("x"), (Integer) 42);
    assertEquals(((FudgeMsg) result).getString("y"), "ABC");
  }

}
