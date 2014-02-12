/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import org.joda.beans.MetaProperty;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityCombiningFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityREDCode;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link DefaultPropertyTypeInferer} class.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultPropertyTypeInfererTest {

  private PropertyTypeInferer createInferer() {
    return new DefaultPropertyTypeInferer(new NullPropertyTypeInferer());
  }

  public void testNonInferredBean() {
    final PropertyTypeInferer inferer = createInferer();
    assertFalse(inferer.hasPrecedentProperties(SimpleExchange.meta()));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public void testIssuerCurveTypeConfiguration() {
    final PropertyTypeInferer inferer = createInferer();
    assertTrue(inferer.hasPrecedentProperties(IssuerCurveTypeConfiguration.meta()));
    assertEquals(inferer.getPrecedentProperties(IssuerCurveTypeConfiguration.meta().filters()), null);
    assertEquals(inferer.getPrecedentProperties(IssuerCurveTypeConfiguration.meta().keys()), ImmutableSet.of(IssuerCurveTypeConfiguration.meta().filters()));
    final LegalEntityCombiningFilter filters = new LegalEntityCombiningFilter();
    filters.setFiltersToUse(ImmutableSet.<LegalEntityFilter<LegalEntity>>of((LegalEntityFilter) new LegalEntityREDCode()));
    assertEquals(inferer.inferPropertyType(IssuerCurveTypeConfiguration.meta().keys(), ImmutableMap.<MetaProperty<?>, Object>of(IssuerCurveTypeConfiguration.meta().filters(), filters)),
        JavaTypeInfo.builder(Set.class).allowNull().parameter(JavaTypeInfo.builder(String.class).allowNull().get()).get());
  }

}
