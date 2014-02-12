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
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link PropertyTypeInferer} that chains onto another.
 */
public abstract class AbstractPropertyTypeInferer implements PropertyTypeInferer {

  private final PropertyTypeInferer _chain;

  protected AbstractPropertyTypeInferer(final PropertyTypeInferer chain) {
    _chain = ArgumentChecker.notNull(chain, "chain");
  }

  protected final boolean hasPrecedentPropertiesChain(final MetaBean bean) {
    return _chain.hasPrecedentProperties(bean);
  }

  protected final Collection<MetaProperty<?>> getPrecedentPropertiesChain(final MetaProperty<?> property) {
    return _chain.getPrecedentProperties(property);
  }

  protected final JavaTypeInfo<?> inferPropertyTypeChain(final MetaProperty<?> property, final Map<MetaProperty<?>, Object> precedents) {
    return _chain.inferPropertyType(property, precedents);
  }

  /**
   * Extracts a precedent property from the bundle of data used for inference.
   * 
   * @param key the property to query, not null
   * @param precedents the bundle passed to {@link #inferPropertyType}
   * @return the value
   */
  protected static <T> T getPrecedentValue(final MetaProperty<T> key, final Map<MetaProperty<?>, Object> precedents) {
    return (T) precedents.get(key);
  }

  /**
   * Tests whether this inferer has any definitions for the bean.
   * <p>
   * When this returns false, the chain will be consulted.
   * 
   * @param bean the bean to test, not null
   * @return true if there are definitions, false otherwise
   */
  protected abstract boolean hasPrecedentPropertiesImpl(MetaBean bean);

  /**
   * Tests whether this inferer has any precedent definitions for the property.
   * <p>
   * When this returns null, the chain will be consulted. If an implementation needs to construct the union of chained definitions then it may call {@link #getPrecedentPropertiesImpl}.
   * 
   * @param property the property to test, not null
   * @return the precedent properties, or null if none
   */
  protected abstract Collection<MetaProperty<?>> getPrecedentPropertiesImpl(MetaProperty<?> property);

  /**
   * Infers the type for the property using these definitions.
   * <p>
   * When this returns null, the chain will be consulted. If an implementation needs to construct a type union of chained definitions then it may call {@link #inferPropertyTypeChain}.
   * 
   * @param property the property to test, not null
   * @param precedents the values of precedent properties, not null
   * @return the inferred type, or null if none
   */
  protected abstract JavaTypeInfo<?> inferPropertyTypeImpl(MetaProperty<?> property, Map<MetaProperty<?>, Object> precedents);

  // PropertyTypeInferer

  @Override
  public final boolean hasPrecedentProperties(final MetaBean bean) {
    return hasPrecedentPropertiesImpl(bean) || hasPrecedentPropertiesChain(bean);
  }

  @Override
  public final Collection<MetaProperty<?>> getPrecedentProperties(final MetaProperty<?> property) {
    Collection<MetaProperty<?>> properties = getPrecedentPropertiesImpl(property);
    if (properties == null) {
      return getPrecedentPropertiesChain(property);
    } else {
      return properties;
    }
  }

  @Override
  public final JavaTypeInfo<?> inferPropertyType(final MetaProperty<?> property, final Map<MetaProperty<?>, Object> precedents) {
    final JavaTypeInfo<?> type = inferPropertyTypeImpl(property, precedents);
    if (type == null) {
      return inferPropertyTypeChain(property, precedents);
    } else {
      return type;
    }
  }

}
