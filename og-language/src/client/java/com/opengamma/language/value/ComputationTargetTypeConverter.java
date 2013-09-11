/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts {@link ComputationTargetType} instances to/from their string representation.
 */
public class ComputationTargetTypeConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<ComputationTargetType> COMPUTATION_TARGET_TYPE = JavaTypeInfo.builder(ComputationTargetType.class).get();
  private static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();

  /**
   * Default instance.
   */
  public static final ComputationTargetTypeConverter INSTANCE = new ComputationTargetTypeConverter();

  protected ComputationTargetTypeConverter() {
    conversion(TypeMap.ZERO_LOSS, COMPUTATION_TARGET_TYPE, STRING, new Action<ComputationTargetType, String>() {
      @Override
      protected String convert(final ComputationTargetType value) {
        return value.toString();
      }
    }, new Action<String, ComputationTargetType>() {
      @Override
      protected ComputationTargetType convert(final String value) {
        return ComputationTargetType.parse(value);
      }
    });
  }

}
