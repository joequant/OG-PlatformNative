/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to standard Java collection types.
 */
@SuppressWarnings("rawtypes")
public final class CollectionTypes {

  /**
   * Prevents instantiation.
   */
  private CollectionTypes() {
  }

  public static final JavaTypeInfo<List> LIST = JavaTypeInfo.builder(List.class).get();

  public static final JavaTypeInfo<List> LIST_ALLOW_NULL = JavaTypeInfo.builder(List.class).allowNull().get();

  public static final JavaTypeInfo<Map> MAP = JavaTypeInfo.builder(Map.class).get();

  public static final JavaTypeInfo<Map> MAP_ALLOW_NULL = JavaTypeInfo.builder(Map.class).allowNull().get();

  public static final JavaTypeInfo<Set> SET = JavaTypeInfo.builder(Set.class).get();

  public static final JavaTypeInfo<Set> SET_ALLOW_NULL = JavaTypeInfo.builder(Set.class).allowNull().get();

}
