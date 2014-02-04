/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;
import static com.opengamma.language.definition.types.PrimitiveTypes.BOOLEAN;
import static com.opengamma.language.definition.types.PrimitiveTypes.BOOLEAN_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.BYTE;
import static com.opengamma.language.definition.types.PrimitiveTypes.BYTE_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.CHARACTER;
import static com.opengamma.language.definition.types.PrimitiveTypes.CHARACTER_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.DOUBLE;
import static com.opengamma.language.definition.types.PrimitiveTypes.DOUBLE_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.FLOAT;
import static com.opengamma.language.definition.types.PrimitiveTypes.FLOAT_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.INTEGER;
import static com.opengamma.language.definition.types.PrimitiveTypes.INTEGER_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.LONG;
import static com.opengamma.language.definition.types.PrimitiveTypes.LONG_PRIMITIVE;
import static com.opengamma.language.definition.types.PrimitiveTypes.SHORT;
import static com.opengamma.language.definition.types.PrimitiveTypes.SHORT_PRIMITIVE;

/**
 * Unbox the primitive types.
 */
public class BoxingConverter extends AbstractMappedConverter {

  private static final Action<?, ?> s_identity = new Action<Object, Object>() {
    @Override
    protected Object convert(Object value) {
      return value;
    }
  };

  /**
   * Default instance.
   */
  public static final BoxingConverter INSTANCE = new BoxingConverter();

  @SuppressWarnings("unchecked")
  private static <T> Action<T, T> identity() {
    return (Action<T, T>) s_identity;
  }

  protected BoxingConverter() {
    conversion(ZERO_LOSS, BOOLEAN_PRIMITIVE, BOOLEAN, BoxingConverter.<Boolean>identity(), BoxingConverter.<Boolean>identity());
    conversion(ZERO_LOSS, BYTE_PRIMITIVE, BYTE, BoxingConverter.<Byte>identity(), BoxingConverter.<Byte>identity());
    conversion(ZERO_LOSS, CHARACTER_PRIMITIVE, CHARACTER, BoxingConverter.<Character>identity(), BoxingConverter.<Character>identity());
    conversion(ZERO_LOSS, DOUBLE_PRIMITIVE, DOUBLE, BoxingConverter.<Double>identity(), BoxingConverter.<Double>identity());
    conversion(ZERO_LOSS, FLOAT_PRIMITIVE, FLOAT, BoxingConverter.<Float>identity(), BoxingConverter.<Float>identity());
    conversion(ZERO_LOSS, INTEGER_PRIMITIVE, INTEGER, BoxingConverter.<Integer>identity(), BoxingConverter.<Integer>identity());
    conversion(ZERO_LOSS, LONG_PRIMITIVE, LONG, BoxingConverter.<Long>identity(), BoxingConverter.<Long>identity());
    conversion(ZERO_LOSS, SHORT_PRIMITIVE, SHORT, BoxingConverter.<Short>identity(), BoxingConverter.<Short>identity());
  }

}
