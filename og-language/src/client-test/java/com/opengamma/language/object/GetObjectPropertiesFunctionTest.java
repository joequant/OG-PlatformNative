/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link GetObjectPropertiesFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class GetObjectPropertiesFunctionTest {

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
    final Map<String, Data> properties = GetObjectPropertiesFunction.invoke(context, beanMsg);
    assertEquals(properties.size(), 3);
    assertEquals(properties.get("foo"), DataUtils.of("XYZ"));
    assertEquals(properties.get("bar"), DataUtils.of(42d));
    assertEquals(properties.get("cow"), new Data());
  }

  public void testDirectBean() {
    final SessionContext context = createSessionContext();
    final CurrencyAmount bean = CurrencyAmount.of(Currency.GBP, 42d);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    final Map<String, Data> properties = GetObjectPropertiesFunction.invoke(context, beanMsg);
    assertEquals(properties.size(), 2);
    assertEquals(properties.get("currency"), DataUtils.of("GBP"));
    assertEquals(properties.get("amount"), DataUtils.of(42d));
  }

  public void testNonBean() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = context.getGlobalContext().getValueConverter().convertValue(context, bean, TransportTypes.FUDGE_MSG);
    final Map<String, Data> properties = GetObjectPropertiesFunction.invoke(context, beanMsg);
    assertEquals(properties.size(), 1);
    assertEquals(properties.get("x"), DataUtils.of(42));
  }

  public void testNonObject() {
    final SessionContext context = createSessionContext();
    final ObjectFunctionTest.NonJodaBean bean = new ObjectFunctionTest.NonJodaBean();
    bean.setX(42);
    final FudgeMsg beanMsg = (new FudgeSerializer(FudgeTypeConverter.getFudgeContext(context.getGlobalContext()))).objectToFudgeMsg(bean);
    // Check we've got no type information or hints
    assertEquals(beanMsg.toString(), "FudgeMsg[x => 42]");
    final Map<String, Data> properties = GetObjectPropertiesFunction.invoke(context, beanMsg);
    assertEquals(properties.size(), 1);
    assertEquals(properties.get("x"), DataUtils.of(42));
  }

}
