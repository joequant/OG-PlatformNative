/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Collection;

import com.opengamma.language.invoke.AbstractTypeConverterProvider;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Constructs instances of the standard converters.
 */
public final class Converters extends AbstractTypeConverterProvider {

  @Override
  protected void loadTypeConverters(final Collection<TypeConverter> converters) {
    converters.add(ArrayDepthConverter.INSTANCE);
    converters.add(ArrayTypeConverter.INSTANCE);
    converters.add(BoxingConverter.INSTANCE);
    converters.add(DataConverter.INSTANCE);
    converters.add(EnumConverter.INSTANCE);
    converters.add(new FudgeTypeConverter());
    converters.add(ListConverter.INSTANCE);
    converters.add(MapConverter.INSTANCE);
    converters.add(PrimitiveArrayConverter.INSTANCE);
    converters.add(PrimitiveConverter.INSTANCE);
    converters.add(SetConverter.INSTANCE);
    converters.add(ValueConverter.INSTANCE);
  }

}
