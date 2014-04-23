/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;

import com.opengamma.language.Value;

/**
 * Implementation of "simple" message strings, discarding much of the original information in the interests of brevity.
 */
public class SimpleClientMessageStrings extends DefaultClientMessageStrings {

  protected String linearData(final int length) {
    return "Data[" + length + "]";
  }

  protected String matrixData(final int rows, final int cols) {
    return "Data[" + rows + "][" + cols + "]";
  }

  // DefaultClientMessageStrings

  @Override
  protected String messageValue(final FudgeMsg value) {
    final FudgeField clazz = value.getByOrdinal(0);
    if ((clazz != null) && (clazz.getValue() instanceof String)) {
      final String clazzName = (String) clazz.getValue();
      final int dot = clazzName.lastIndexOf('.');
      return clazzName.substring(dot + 1);
    } else {
      return "Message encoded object";
    }
  }

  @Override
  protected String linearData(final Value[] data) {
    return linearData(data.length);
  }

  @Override
  protected String matrixData(final Value[][] data) {
    final int rows = data.length;
    if (rows > 0) {
      return matrixData(rows, data[0].length);
    } else {
      return matrixData(rows, 0);
    }
  }

}
