/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.convert;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.rstats.data.RDataInfo;

/**
 * Converts cube (3D matrix) objects. The R encoding is a vector. This is the number of dimensions, followed by the dimension counts, followed by the data.
 */
public class CubeConverter extends AbstractMappedConverter {

  public CubeConverter() {
    conversion(TypeMap.ZERO_LOSS, JavaTypeInfo.builder(Value[][][].class).allowNull().get(), TransportTypes.DATA_ALLOW_NULL, new Action<Value[][][], Data>() {
      @Override
      protected Data convert(Value[][][] value) {
        final int zlen = value.length;
        final int ylen = value[0].length;
        final int xlen = value[0][0].length;
        final Value[] values = new Value[4 + xlen * ylen * zlen];
        values[0] = ValueUtils.of(3);
        values[1] = ValueUtils.of(xlen);
        values[2] = ValueUtils.of(ylen);
        values[3] = ValueUtils.of(zlen);
        int n = 4;
        for (int i = 0; i < zlen; i++) {
          for (int j = 0; j < ylen; j++) {
            for (int k = 0; k < xlen; k++) {
              values[n++] = value[i][j][k];
            }
          }
        }
        return RDataInfo.create().wrapperClass("Array").applyTo(DataUtils.of(values));
      }
    }, new Action<Data, Value[][][]>() {
      @Override
      public Value[][][] convert(final Data data) {
        final Value[] values = data.getLinear();
        if (values == null) {
          return null;
        }
        if (values[0].getIntValue() != 3) {
          throw new IllegalArgumentException();
        }
        final int xlen = values[1].getIntValue();
        final int ylen = values[2].getIntValue();
        final int zlen = values[3].getIntValue();
        int n = 4;
        final Value[][][] value = new Value[zlen][ylen][xlen];
        for (int i = 0; i < zlen; i++) {
          for (int j = 0; j < ylen; j++) {
            for (int k = 0; k < xlen; k++) {
              value[i][j][k] = values[n++];
            }
          }
        }
        return value;
      }
    });
  }

}
