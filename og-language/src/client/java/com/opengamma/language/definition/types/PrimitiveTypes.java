/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to standard Java types.
 */
@SuppressWarnings("rawtypes")
public final class PrimitiveTypes {

  /**
   * Prevents instantiation.
   */
  private PrimitiveTypes() {
  }

  public static final JavaTypeInfo<Boolean> BOOLEAN = JavaTypeInfo.builder(Boolean.class).get();

  public static final JavaTypeInfo<Boolean> BOOLEAN_PRIMITIVE = JavaTypeInfo.builder(boolean.class).get();

  public static final JavaTypeInfo<Boolean> BOOLEAN_DEFAULT_TRUE = JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.TRUE).get();

  public static final JavaTypeInfo<Boolean> BOOLEAN_DEFAULT_FALSE = JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.FALSE).get();

  public static final JavaTypeInfo<Boolean> BOOLEAN_ALLOW_NULL = JavaTypeInfo.builder(Boolean.class).allowNull().get();

  public static final JavaTypeInfo<Byte> BYTE = JavaTypeInfo.builder(Byte.class).get();

  public static final JavaTypeInfo<Byte> BYTE_PRIMITIVE = JavaTypeInfo.builder(byte.class).get();

  public static final JavaTypeInfo<Byte> BYTE_ALLOW_NULL = JavaTypeInfo.builder(Byte.class).allowNull().get();

  public static final JavaTypeInfo<Character> CHARACTER = JavaTypeInfo.builder(Character.class).get();

  public static final JavaTypeInfo<Character> CHARACTER_PRIMITIVE = JavaTypeInfo.builder(char.class).get();

  public static final JavaTypeInfo<Character> CHARACTER_ALLOW_NULL = JavaTypeInfo.builder(Character.class).allowNull().get();

  public static final JavaTypeInfo<Double> DOUBLE = JavaTypeInfo.builder(Double.class).get();

  public static final JavaTypeInfo<Double> DOUBLE_PRIMITIVE = JavaTypeInfo.builder(double.class).get();

  public static final JavaTypeInfo<Double> DOUBLE_ALLOW_NULL = JavaTypeInfo.builder(Double.class).allowNull().get();

  public static final JavaTypeInfo<Enum> ENUM = JavaTypeInfo.builder(Enum.class).get();

  public static final JavaTypeInfo<Enum> ENUM_ALLOW_NULL = JavaTypeInfo.builder(Enum.class).allowNull().get();

  public static final JavaTypeInfo<Float> FLOAT = JavaTypeInfo.builder(Float.class).get();

  public static final JavaTypeInfo<Float> FLOAT_PRIMITIVE = JavaTypeInfo.builder(float.class).get();

  public static final JavaTypeInfo<Float> FLOAT_ALLOW_NULL = JavaTypeInfo.builder(Float.class).allowNull().get();

  public static final JavaTypeInfo<Integer> INTEGER = JavaTypeInfo.builder(Integer.class).get();

  public static final JavaTypeInfo<Integer> INTEGER_PRIMITIVE = JavaTypeInfo.builder(int.class).get();

  public static final JavaTypeInfo<Integer> INTEGER_ALLOW_NULL = JavaTypeInfo.builder(Integer.class).allowNull().get();

  public static final JavaTypeInfo<Long> LONG = JavaTypeInfo.builder(Long.class).get();

  public static final JavaTypeInfo<Long> LONG_PRIMITIVE = JavaTypeInfo.builder(long.class).get();

  public static final JavaTypeInfo<Long> LONG_ALLOW_NULL = JavaTypeInfo.builder(Long.class).allowNull().get();

  public static final JavaTypeInfo<Short> SHORT = JavaTypeInfo.builder(Short.class).get();

  public static final JavaTypeInfo<Short> SHORT_PRIMITIVE = JavaTypeInfo.builder(short.class).get();

  public static final JavaTypeInfo<Short> SHORT_ALLOW_NULL = JavaTypeInfo.builder(Short.class).allowNull().get();

  public static final JavaTypeInfo<String> STRING = JavaTypeInfo.builder(String.class).get();

  public static final JavaTypeInfo<String> STRING_ALLOW_NULL = JavaTypeInfo.builder(String.class).allowNull().get();

}
