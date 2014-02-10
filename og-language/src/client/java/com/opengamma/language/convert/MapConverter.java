/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import org.fudgemsg.FudgeContext;

import com.google.common.collect.Maps;
import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.types.CollectionTypes;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converts a map of A->B to Value[][2]
 */
public class MapConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final MapConverter INSTANCE = new MapConverter();

  private static final JavaTypeInfo<Value[][]> VALUES = JavaTypeInfo.builder(Value[][].class).get();
  private static final JavaTypeInfo<Value[][]> VALUES_ALLOW_NULL = JavaTypeInfo.builder(Value[][].class).allowNull().get();

  private static final TypeMap TO_MAP = TypeMap.of(ZERO_LOSS, VALUES);
  private static final TypeMap FROM_MAP = TypeMap.of(ZERO_LOSS, CollectionTypes.MAP);
  private static final TypeMap TO_MAP_ALLOW_NULL = TypeMap.of(ZERO_LOSS, VALUES_ALLOW_NULL);
  private static final TypeMap FROM_MAP_ALLOW_NULL = TypeMap.of(ZERO_LOSS, CollectionTypes.MAP_ALLOW_NULL);

  protected MapConverter() {
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == Map.class) || (targetType.getRawClass() == Value[][].class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if ((value == null) && type.isAllowNull()) {
      conversionContext.setResult(null);
      return;
    }
    if (type.getRawClass() == Map.class) {
      // Converting from Values[][] to Map
      final Value[][] values = (Value[][]) value;
      final JavaTypeInfo<?> keyType = type.getParameterizedType(0);
      final JavaTypeInfo<?> valueType = type.getParameterizedType(1);
      final Map<Object, Object> result = Maps.newHashMapWithExpectedSize(values.length);
      for (Value[] entry : values) {
        if (entry.length != 2) {
          conversionContext.setFail();
          return;
        }
        conversionContext.convertValue(entry[0], keyType);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        final Object key = conversionContext.getResult();
        conversionContext.convertValue(entry[1], valueType);
        if (conversionContext.isFailed()) {
          conversionContext.setFail();
          return;
        }
        result.put(key, conversionContext.getResult());
      }
      conversionContext.setResult(result);
    } else {
      // Converting from Map to Values[][]
      final FudgeContext fudgeContext = FudgeTypeConverter.getFudgeContext(conversionContext.getGlobalContext());
      final Map<?, ?> map = (Map<?, ?>) value;
      final Value[][] result = new Value[map.size()][2];
      int i = 0;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getKey() == null) {
          result[i][0] = new Value();
        } else {
          conversionContext.convertValue(entry.getKey(), TransportTypes.DATA);
          if (conversionContext.isFailed()) {
            conversionContext.setFail();
            return;
          }
          result[i][0] = ValueUtils.of(fudgeContext, conversionContext.<Data>getResult());
        }
        if (entry.getValue() == null) {
          result[i++][1] = new Value();
        } else {
          conversionContext.convertValue(entry.getValue(), TransportTypes.DATA);
          if (conversionContext.isFailed()) {
            conversionContext.setFail();
            return;
          }
          result[i++][1] = ValueUtils.of(fudgeContext, conversionContext.<Data>getResult());
        }
      }
      conversionContext.setResult(result);
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final ValueConversionContext conversionContext, final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == Map.class) {
      return targetType.isAllowNull() ? TO_MAP_ALLOW_NULL : TO_MAP;
    } else {
      return targetType.isAllowNull() ? FROM_MAP_ALLOW_NULL : FROM_MAP;
    }
  }

}
