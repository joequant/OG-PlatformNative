/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Collection;

import org.fudgemsg.FudgeContext;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.opengamma.language.invoke.AbstractTypeConverterProvider;
import com.opengamma.language.invoke.TypeConverter;
import com.opengamma.util.ArgumentChecker;

/**
 * Constructs instances of the standard converters.
 */
public final class Converters extends AbstractTypeConverterProvider {

  private final Supplier<FudgeContext> _fudgeContext;

  public Converters(final FudgeContext fudgeContext) {
    this(Suppliers.ofInstance(fudgeContext));
  }

  public Converters(final Supplier<FudgeContext> fudgeContext) {
    _fudgeContext = ArgumentChecker.notNull(fudgeContext, "fudgeContext");
  }

  @Override
  protected void loadTypeConverters(final Collection<TypeConverter> converters) {
    converters.add(ArrayDepthConverter.INSTANCE);
    converters.add(ArrayTypeConverter.INSTANCE);
    converters.add(BoxingConverter.INSTANCE);
    converters.add(DataConverter.INSTANCE);
    converters.add(EnumConverter.INSTANCE);
    converters.add(new FudgeTypeConverter(_fudgeContext));
    converters.add(ListConverter.INSTANCE);
    converters.add(MapConverter.INSTANCE);
    converters.add(PrimitiveArrayConverter.INSTANCE);
    converters.add(PrimitiveConverter.INSTANCE);
    converters.add(SetConverter.INSTANCE);
    converters.add(ValueConverter.INSTANCE);
  }

}
