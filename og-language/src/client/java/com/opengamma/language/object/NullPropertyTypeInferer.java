/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.Collection;
import java.util.Map;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Base implementation which does no inference.
 */
public final class NullPropertyTypeInferer implements PropertyTypeInferer {

  @Override
  public boolean hasPrecedentProperties(MetaBean bean) {
    return false;
  }

  @Override
  public Collection<MetaProperty<?>> getPrecedentProperties(MetaProperty<?> property) {
    return null;
  }

  @Override
  public JavaTypeInfo<?> inferPropertyType(MetaProperty<?> property, Map<MetaProperty<?>, Object> precedents) {
    return null;
  }

}
