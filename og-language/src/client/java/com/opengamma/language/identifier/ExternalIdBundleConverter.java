/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.types.OpenGammaTypes;

/**
 * Converts an array of {@link ExternalId} values to an {@link ExternalIdBundle}.
 */
public class ExternalIdBundleConverter extends AbstractMappedConverter {

  /**
   * Default instance.
   */
  public static final ExternalIdBundleConverter INSTANCE = new ExternalIdBundleConverter();

  protected ExternalIdBundleConverter() {
    conversion(TypeMap.ZERO_LOSS, JavaTypeInfo.builder(ExternalId[].class).get(), OpenGammaTypes.EXTERNAL_ID_BUNDLE, new Action<ExternalId[], ExternalIdBundle>() {
      @Override
      protected ExternalIdBundle convert(final ExternalId[] values) {
        return ExternalIdBundle.of(values);
      }
    });
  }
}
