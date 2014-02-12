/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityCombiningFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.financial.legalentity.LegalEntitySector;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.AbstractGlobalContextEventHandler;
import com.opengamma.language.context.GlobalContextEventHandler;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ObjectFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class ObjectFunctionTest {

  /* package */static SessionContext createSessionContext() {
    final TestUtils testUtils = new TestUtils() {
      @Override
      protected GlobalContextEventHandler createGlobalContextEventHandler() {
        return new AbstractGlobalContextEventHandler(super.createGlobalContextEventHandler()) {
          @Override
          protected void initContextImpl(final MutableGlobalContext context) {
            SetObjectPropertyFunction.setPropertyTypeInferer(context, new DefaultPropertyTypeInferer(new NullPropertyTypeInferer()));
          }
        };
      }
    };
    testUtils.setTypeConverters(new Converters());
    return testUtils.createSessionContext();
  }

  public void testFlexiBean() {
    final SessionContext context = createSessionContext();
    Object value = ObjectFunction.invoke(context, null, ImmutableMap.of("foo", DataUtils.of("XYZ"), "bar", DataUtils.of(42d)));
    final FlexiBean bean = (FlexiBean) value;
    assertEquals(bean.getString("foo"), "XYZ");
    assertEquals(bean.getDouble("bar"), 42d);
    value = ObjectFunction.invoke(context, null, ImmutableMap.<String, Data>of());
    assertEquals(value, new FlexiBean());
    value = ObjectFunction.invoke(context, null, null);
    assertEquals(value, new FlexiBean());
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidClassName() {
    ObjectFunction.invoke(createSessionContext(), "com.opengamma.doesnt.exist.ClassName", Collections.<String, Data>emptyMap());
  }

  public static class ImproperJodaBean implements Bean {

    @Override
    public MetaBean metaBean() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <R> Property<R> property(String propertyName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> propertyNames() {
      throw new UnsupportedOperationException();
    }

    @Override
    public ImproperJodaBean clone() {
      return new ImproperJodaBean();
    }

  }

  public void testImproperJodaBean() {
    final Object result = ObjectFunction.invoke(createSessionContext(), ImproperJodaBean.class.getName(), null);
    assertTrue(result instanceof ImproperJodaBean);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testJodaBeanValidationFail1() {
    ObjectFunction.invoke(createSessionContext(), SwapSecurity.class.getName(), null);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testJodaBeanValidationFail2() {
    ObjectFunction.invoke(createSessionContext(), SwapSecurity.class.getName(), ImmutableMap.<String, Data>of());
  }

  public void testMutableJodaBean() {
    final SessionContext context = createSessionContext();
    Object result = ObjectFunction.invoke(context, SimpleExchange.class.getName(), null);
    assertEquals(result, new SimpleExchange());
    result = ObjectFunction.invoke(context, SimpleExchange.class.getName(), ImmutableMap.of("name", DataUtils.of("Test")));
    final SimpleExchange expected = new SimpleExchange();
    expected.setName("Test");
    assertEquals(result, expected);
  }

  public void testImmutableJodaBean() {
    assertTrue(ImmutableBean.class.isAssignableFrom(CurrencyAmount.class));
    final Object result = ObjectFunction.invoke(createSessionContext(), CurrencyAmount.class.getName(), ImmutableMap.of("currency", DataUtils.of("GBP"), "amount", DataUtils.of(42d)));
    assertEquals(result, CurrencyAmount.of(Currency.GBP, 42d));
  }

  public static final class NonJodaBean {

    private int _x;

    public void setX(final int x) {
      _x = x;
    }

    public int getX() {
      return _x;
    }

    @Override
    public boolean equals(final Object o) {
      if (o instanceof NonJodaBean) {
        return ((NonJodaBean) o)._x == _x;
      } else {
        return false;
      }
    }

  }

  public void testNonJodaBean() {
    final SessionContext context = createSessionContext();
    Object result = ObjectFunction.invoke(context, NonJodaBean.class.getName(), null);
    assertEquals(result, new NonJodaBean());
    result = ObjectFunction.invoke(context, NonJodaBean.class.getName(), ImmutableMap.of("x", DataUtils.of(42)));
    final NonJodaBean expected = new NonJodaBean();
    expected.setX(42);
    assertEquals(result, expected);
  }

  public static class NonBean {

    private final int _x;
    private int _y;
    private int _z;

    public NonBean(int x) {
      _x = x;
    }

    public NonBean(int x, int z) {
      _x = x;
      _z = z;
    }

    public int getX() {
      return _x;
    }

    public void setY(final int y) {
      _y = y;
    }

    public int getY() {
      return _y;
    }

    public void setZ(final int z) {
      fail();
    }

    public int getZ() {
      return _z;
    }

  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testNonBean_fail() {
    ObjectFunction.invoke(createSessionContext(), NonBean.class.getName(), null);
  }

  public void testNonBean_ok() {
    final SessionContext context = createSessionContext();
    NonBean result = (NonBean) ObjectFunction.invoke(context, NonBean.class.getName(), ImmutableMap.of("x", DataUtils.of(42)));
    assertEquals(result.getX(), 42);
    assertEquals(result.getY(), 0);
    assertEquals(result.getZ(), 0);
    result = (NonBean) ObjectFunction.invoke(context, NonBean.class.getName(), ImmutableMap.of("x", DataUtils.of(4), "y", DataUtils.of(2)));
    assertEquals(result.getX(), 4);
    assertEquals(result.getY(), 2);
    assertEquals(result.getZ(), 0);
    result = (NonBean) ObjectFunction.invoke(context, NonBean.class.getName(), ImmutableMap.of("x", DataUtils.of(4), "z", DataUtils.of(2)));
    assertEquals(result.getX(), 4);
    assertEquals(result.getY(), 0);
    assertEquals(result.getZ(), 2);
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  public void testIssuerCurveTypeConfiguration() {
    final SessionContext context = createSessionContext();
    // Enough state in the filter for the keys to be correctly typed
    LegalEntityCombiningFilter filters = new LegalEntityCombiningFilter();
    filters.setFiltersToUse(ImmutableSet.<LegalEntityFilter<LegalEntity>>of((LegalEntityFilter) new LegalEntityRegion(false, false, Collections.<Country>emptySet(), true, Collections
        .singleton(Currency.GBP))));
    Data filtersData = context.getGlobalContext().getValueConverter().convertValue(context, filters, TransportTypes.DATA);
    IssuerCurveTypeConfiguration result = (IssuerCurveTypeConfiguration) ObjectFunction.invoke(context, "IssuerCurveTypeConfiguration",
        ImmutableMap.of("keys", DataUtils.of("GBP"), "filters", filtersData));
    assertEquals(result.getKeys(), ImmutableSet.of(Currency.GBP));
    // Not enough state in the filter, so currency ends up as arbitrary value
    filters = new LegalEntityCombiningFilter();
    filters.setFiltersToUse(ImmutableSet.<LegalEntityFilter<LegalEntity>>of((LegalEntityFilter) new LegalEntitySector(false, false, ImmutableSet.of("Foo"))));
    filtersData = context.getGlobalContext().getValueConverter().convertValue(context, filters, TransportTypes.DATA);
    result = (IssuerCurveTypeConfiguration) ObjectFunction.invoke(context, "IssuerCurveTypeConfiguration", ImmutableMap.of("keys", DataUtils.of("GBP"), "filters", filtersData));
    assertEquals(result.getKeys(), ImmutableSet.of(ValueUtils.of("GBP")));
  }

}
