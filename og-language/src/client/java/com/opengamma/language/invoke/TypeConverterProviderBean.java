/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

/**
 * Bean implementation of a {@link TypeConverterProvider}.
 */
public class TypeConverterProviderBean extends AbstractTypeConverterProvider implements InitializingBean {

  private Collection<TypeConverter> _converters;

  public TypeConverterProviderBean(final Collection<TypeConverter> converters) {
    setConverters(converters);
  }

  public TypeConverterProviderBean(final TypeConverter... converters) {
    setConverters(Arrays.asList(converters));
  }

  public TypeConverterProviderBean() {
  }

  public void setConverters(final Collection<TypeConverter> converters) {
    ArgumentChecker.notNull(converters, "converters");
    _converters = new ArrayList<TypeConverter>(converters);
  }

  public void addConverter(final TypeConverter converter) {
    if (_converters == null) {
      _converters = new ArrayList<TypeConverter>();
    }
    _converters.add(converter);
  }

  private Collection<TypeConverter> getConvertersInternal() {
    return _converters;
  }

  public Collection<TypeConverter> getConverters() {
    return Collections.unmodifiableCollection(getConvertersInternal());
  }

  // InitializingBean

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNull(getConvertersInternal(), "converters");
  }

  // AbstractTypeConverterProvider

  @Override
  protected void loadTypeConverters(Collection<TypeConverter> converters) {
    converters.addAll(getConverters());
  }

}
