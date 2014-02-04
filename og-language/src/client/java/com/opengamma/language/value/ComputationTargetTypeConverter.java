/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.types.EngineTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;

/**
 * Converts {@link ComputationTargetType} instances to/from their string representation.
 */
public class ComputationTargetTypeConverter extends AbstractMappedConverter {

  /**
   * Default instance.
   */
  public static final ComputationTargetTypeConverter INSTANCE = new ComputationTargetTypeConverter();

  protected ComputationTargetTypeConverter() {
    conversion(TypeMap.ZERO_LOSS, EngineTypes.COMPUTATION_TARGET_TYPE_ALLOW_NULL, PrimitiveTypes.STRING_ALLOW_NULL, new Action<ComputationTargetType, String>() {
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
