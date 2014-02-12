/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.Map;

import org.joda.beans.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.legalentity.LegalEntityCombiningFilter;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Default implementation of a {@link PropertyTypeInferer} for working with definitions in OG-Analytics and OG-Financial.
 */
public class DefaultPropertyTypeInferer extends MapPropertyTypeInferer {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultPropertyTypeInferer.class);

  private static InferenceEntry issuerCurveTypeConfigurationKeys() {
    final IssuerCurveTypeConfiguration.Meta meta = IssuerCurveTypeConfiguration.meta();
    return new InferenceEntry(meta.keys(), meta.filters()) {
      @Override
      protected JavaTypeInfo<?> inferPropertyType(final Map<MetaProperty<?>, Object> precedents) {
        final LegalEntityCombiningFilter filters = getPrecedentValue(meta.filters(), precedents);
        if (filters == null) {
          return null;
        }
        try {
          return JavaTypeInfo.ofType(filters.getFilteredDataType());
        } catch (RuntimeException e) {
          // JavaTypeInfo can't handle VariantType instances at the moment - the most likely cause of exceptions here
          s_logger.error("Caught exception", e);
          return null;
        }
      }
    };
  }

  public DefaultPropertyTypeInferer(final PropertyTypeInferer chain) {
    super(chain);
    add(issuerCurveTypeConfigurationKeys());
  }

}
