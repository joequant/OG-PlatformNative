/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Implementation of a {@link PropertyTypeInferer} based on a map of meta property data.
 */
public class MapPropertyTypeInferer extends AbstractPropertyTypeInferer {

  /**
   * Instructions for inferring an element within the property map.
   */
  public abstract static class InferenceEntry {

    private final MetaProperty<?> _property;
    private final Collection<MetaProperty<?>> _precedents;

    public InferenceEntry(final MetaProperty<?> property, final Collection<MetaProperty<?>> precedents) {
      _property = property;
      _precedents = new ArrayList<MetaProperty<?>>(precedents);
    }

    public InferenceEntry(final MetaProperty<?> property, final MetaProperty<?> precedent) {
      _property = property;
      _precedents = new ArrayList<MetaProperty<?>>();
      _precedents.add(precedent);
    }

    protected MetaProperty<?> getProperty() {
      return _property;
    }

    protected Collection<MetaProperty<?>> getPrecedents() {
      return _precedents;
    }

    protected abstract JavaTypeInfo<?> inferPropertyType(final Map<MetaProperty<?>, Object> precedents);

  }

  private final Set<MetaBean> _beans = new HashSet<MetaBean>();
  private final Map<MetaProperty<?>, InferenceEntry> _map = new HashMap<MetaProperty<?>, InferenceEntry>();

  protected MapPropertyTypeInferer(final PropertyTypeInferer chain) {
    super(chain);
  }

  public void add(final InferenceEntry entry) {
    final MetaProperty<?> property = entry.getProperty();
    _beans.add(property.metaBean());
    final InferenceEntry oldEntry = _map.put(property, entry);
    assert oldEntry == null;
  }

  // AbstractPropertyTypeInferer

  @Override
  protected boolean hasPrecedentPropertiesImpl(final MetaBean bean) {
    return _beans.contains(bean);
  }

  @Override
  protected Collection<MetaProperty<?>> getPrecedentPropertiesImpl(final MetaProperty<?> property) {
    final InferenceEntry entry = _map.get(property);
    if (entry != null) {
      return entry.getPrecedents();
    } else {
      return null;
    }
  }

  @Override
  protected JavaTypeInfo<?> inferPropertyTypeImpl(final MetaProperty<?> property, final Map<MetaProperty<?>, Object> precedents) {
    final InferenceEntry entry = _map.get(property);
    if (entry != null) {
      return entry.inferPropertyType(precedents);
    } else {
      return null;
    }
  }

}
