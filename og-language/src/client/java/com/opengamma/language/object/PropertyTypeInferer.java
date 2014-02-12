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
 * Service for guessing which type a property should be. This can be used with beans that have a very vague definition at compile time - for example a property might be a "set of objects" - but at
 * runtime we can infer a more specific definition from other property values - for example the property might become a "set of currencies".
 */
public interface PropertyTypeInferer {

  /**
   * Tests if the object has any inference rules. This is to provide an optimization for the common case and avoid repeated calls to {@link #getPrecendentProperties} and potential re-ordering of
   * construction/update parameters.
   * 
   * @param bean the bean definition to test, not null
   */
  boolean hasPrecedentProperties(MetaBean bean);

  /**
   * Indicates which properties are used to infer the type of another.
   * 
   * @param property the property to test, not null
   * @return the properties whose values must be used for inference or null if there is no inference rule
   */
  Collection<MetaProperty<?>> getPrecedentProperties(MetaProperty<?> property);

  /**
   * Infers a logical type from the values of any precedent properties defined by a previous call to {@link #getPrecedentProperties}.
   * 
   * @param property the property to infer a type for, not null
   * @param precedents the values of other properties, not null
   * @return the logical type, or null if none can be inferred and a default should be used
   */
  JavaTypeInfo<?> inferPropertyType(MetaProperty<?> property, Map<MetaProperty<?>, Object> precedents);

}
