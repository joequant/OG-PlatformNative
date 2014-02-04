/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;
import static com.opengamma.language.definition.types.PrimitiveTypes.BOOLEAN;
import static com.opengamma.language.definition.types.PrimitiveTypes.BOOLEAN_ALLOW_NULL;
import static com.opengamma.language.definition.types.PrimitiveTypes.DOUBLE;
import static com.opengamma.language.definition.types.PrimitiveTypes.DOUBLE_ALLOW_NULL;
import static com.opengamma.language.definition.types.PrimitiveTypes.INTEGER;
import static com.opengamma.language.definition.types.PrimitiveTypes.INTEGER_ALLOW_NULL;
import static com.opengamma.language.definition.types.PrimitiveTypes.STRING;
import static com.opengamma.language.definition.types.PrimitiveTypes.STRING_ALLOW_NULL;
import static com.opengamma.language.definition.types.TransportTypes.FUDGE_MSG;
import static com.opengamma.language.definition.types.TransportTypes.FUDGE_MSG_ALLOW_NULL;
import static com.opengamma.language.definition.types.TransportTypes.VALUE;
import static com.opengamma.language.definition.types.TransportTypes.VALUE_ALLOW_NULL;

import java.util.Map;

import org.fudgemsg.FudgeMsg;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;
import com.opengamma.language.invoke.TypeConverter;

/**
 * Basic conversions to/from the {@link Value} type.
 */
public final class ValueConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final ValueConverter INSTANCE = new ValueConverter();

  private static final TypeMap TO_VALUE_NOT_NULL = TypeMap.of(ZERO_LOSS, BOOLEAN, INTEGER, DOUBLE, STRING, FUDGE_MSG);
  private static final TypeMap TO_VALUE_ALLOW_NULL = TypeMap.of(ZERO_LOSS, BOOLEAN_ALLOW_NULL, INTEGER_ALLOW_NULL, DOUBLE_ALLOW_NULL, STRING_ALLOW_NULL, FUDGE_MSG_ALLOW_NULL);
  private static final TypeMap FROM_VALUE_NOT_NULL = TypeMap.of(ZERO_LOSS, VALUE);
  private static final TypeMap FROM_VALUE_ALLOW_NULL = TypeMap.of(ZERO_LOSS, VALUE_ALLOW_NULL);

  protected ValueConverter() {
  }

  @Override
  public boolean canConvertTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return true;
    } else {
      for (JavaTypeInfo<?> toData : ((targetType.isAllowNull() || targetType.isDefaultValue()) ? TO_VALUE_ALLOW_NULL : TO_VALUE_NOT_NULL).keySet()) {
        if (clazz == toData.getRawClass()) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(JavaTypeInfo<?> targetType) {
    final Class<?> clazz = targetType.getRawClass();
    if (clazz == Value.class) {
      return (targetType.isAllowNull() || targetType.isDefaultValue()) ? TO_VALUE_ALLOW_NULL : TO_VALUE_NOT_NULL;
    } else {
      return (targetType.isAllowNull() || targetType.isDefaultValue()) ? FROM_VALUE_ALLOW_NULL : FROM_VALUE_NOT_NULL;
    }
  }

  @Override
  public String toString() {
    return TypeConverter.class.getSimpleName() + "[to/from " + Value.class.getName() + "]";
  }

  @Override
  public void convertValue(ValueConversionContext conversionContext, Object valueObject, JavaTypeInfo<?> type) {
    if (valueObject == null) {
      if (type.isAllowNull()) {
        conversionContext.setResult(null);
      } else if (type.isDefaultValue()) {
        conversionContext.setResult(type.getDefaultValue());
      } else {
        conversionContext.setFail();
      }
      return;
    }
    final Class<?> clazz = type.getRawClass();
    if (clazz == Value.class) {
      if (valueObject instanceof Boolean) {
        conversionContext.setResult(ValueUtils.of((Boolean) valueObject));
      } else if (valueObject instanceof Integer) {
        conversionContext.setResult(ValueUtils.of((Integer) valueObject));
      } else if (valueObject instanceof Double) {
        conversionContext.setResult(ValueUtils.of((Double) valueObject));
      } else if (valueObject instanceof String) {
        conversionContext.setResult(ValueUtils.of((String) valueObject));
      } else if (valueObject instanceof FudgeMsg) {
        conversionContext.setResult(ValueUtils.of((FudgeMsg) valueObject));
      } else {
        conversionContext.setFail();
      }
    } else {
      final Value value = (Value) valueObject;
      if (ValueUtils.isNull(value)) {
        if (type.isAllowNull()) {
          conversionContext.setResult(null);
        } else if (type.isDefaultValue()) {
          conversionContext.setResult(type.getDefaultValue());
        } else {
          conversionContext.setFail();
        }
      } else if (clazz == Boolean.class) {
        if (value.getBoolValue() != null) {
          conversionContext.setResult(value.getBoolValue());
        } else {
          conversionContext.setFail();
        }
      } else if (clazz == Integer.class) {
        if (value.getIntValue() != null) {
          conversionContext.setResult(value.getIntValue());
        } else {
          conversionContext.setFail();
        }
      } else if (clazz == Double.class) {
        if (value.getDoubleValue() != null) {
          conversionContext.setResult(value.getDoubleValue());
        } else {
          conversionContext.setFail();
        }
      } else if (clazz == String.class) {
        if (value.getStringValue() != null) {
          conversionContext.setResult(value.getStringValue());
        } else {
          conversionContext.setFail();
        }
      } else if (clazz == FudgeMsg.class) {
        if (value.getMessageValue() != null) {
          conversionContext.setResult(value.getMessageValue());
        } else {
          conversionContext.setFail();
        }
      } else {
        conversionContext.setFail();
      }
    }
  }

}
