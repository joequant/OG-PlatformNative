/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.List;

import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeParameterConversionException;

/**
 * Default implementation of {@link ParameterConverter}.
 */
public class DefaultParameterConverter implements ParameterConverter {

  @Override
  public Object[] convertParameters(final SessionContext sessionContext, final List<Data> clientParameters, final List<MetaParameter> targetParameters) {
    final ValueConverter valueConverter = sessionContext.getGlobalContext().getValueConverter();
    final Object[] parameters = new Object[clientParameters.size()];
    int i = 0;
    final ValueConversionContext context = new ValueConversionContext(sessionContext, valueConverter);
    while (i < parameters.length) {
      final Data parameter = clientParameters.get(i);
      final JavaTypeInfo<?> typeInfo = targetParameters.get(i).getJavaTypeInfo();
      if (typeInfo.getRawClass().isPrimitive()) {
        if (parameter.getSingle() != null) {
          final Value value = parameter.getSingle();
          if (value.getBoolValue() != null) {
            if (typeInfo.getRawClass() == Boolean.TYPE) {
              parameters[i++] = value.getBoolValue();
              continue;
            }
          }
          if (value.getDoubleValue() != null) {
            if (typeInfo.getRawClass() == Double.TYPE) {
              parameters[i++] = value.getDoubleValue();
              continue;
            }
          }
          if (value.getIntValue() != null) {
            if (typeInfo.getRawClass() == Integer.TYPE) {
              parameters[i++] = value.getIntValue();
              continue;
            }
          }
        }
      }
      valueConverter.convertValue(context, parameter, typeInfo);
      if (context.isFailed()) {
        throw new InvokeParameterConversionException(i, InvalidConversionException.createClientMessage(parameter, typeInfo));
      }
      parameters[i++] = context.getResult();
    }
    return parameters;
  }
}
