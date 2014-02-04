/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts ExternalSchemeRank to/from an array of strings.
 */
public class ExternalSchemeRankConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<String[]> STRING_ARRAY_ALLOW_NULL = JavaTypeInfo.builder(String[].class).allowNull().get();

  /**
   * Default instance.
   */
  public static final ExternalSchemeRankConverter INSTANCE = new ExternalSchemeRankConverter();

  protected ExternalSchemeRankConverter() {
    conversion(TypeMap.ZERO_LOSS, STRING_ARRAY_ALLOW_NULL, ExternalSchemeRank.TYPE_ALLOW_NULL, new Action<String[], ExternalSchemeRank>() {
      @Override
      protected ExternalSchemeRank convert(final String[] value) {
        return ExternalSchemeRank.ofStrings(value);
      }
    });
    conversion(TypeMap.MINOR_LOSS, ExternalSchemeRank.TYPE_ALLOW_NULL, STRING_ARRAY_ALLOW_NULL, new Action<ExternalSchemeRank, String[]>() {
      @Override
      protected String[] convert(final ExternalSchemeRank value) {
        return value.asStrings();
      }
    });
  }

}
