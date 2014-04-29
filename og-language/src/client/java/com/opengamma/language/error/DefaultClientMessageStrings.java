/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import org.fudgemsg.FudgeMsg;

import com.opengamma.language.Data;
import com.opengamma.language.Value;

/**
 * Default/base implementation of message strings. Hooks are defined for this to be used as the basis for more specific handling strategies.
 */
public class DefaultClientMessageStrings implements ClientMessageStrings {

  protected String nullValue() {
    return null;
  }

  protected String emptyValue() {
    return "";
  }

  /**
   * Tests if the passed value is the {@link #emptyValue} value.
   * 
   * @param value the value to test, not null
   * @return true if this is the {@code emptyValue} value, false otherwise
   */
  protected boolean isEmptyValue(final String value) {
    return value.equals(emptyValue());
  }

  protected String booleanValue(final Boolean value) {
    return value.toString();
  }

  protected String doubleValue(final Double value) {
    return value.toString();
  }

  protected String integerValue(final Integer value) {
    return value.toString();
  }

  protected String messageValue(final FudgeMsg value) {
    return value.toString();
  }

  protected String emptyString() {
    return "";
  }

  protected String stringValue(final String value) {
    if (value.length() > 0) {
      return value;
    } else {
      return emptyString();
    }
  }

  protected String nonErrorValue(final Value value) {
    if (value.getBoolValue() != null) {
      return booleanValue(value.getBoolValue());
    } else if (value.getDoubleValue() != null) {
      return doubleValue(value.getDoubleValue());
    } else if (value.getIntValue() != null) {
      return integerValue(value.getIntValue());
    } else if (value.getMessageValue() != null) {
      return messageValue(value.getMessageValue());
    } else if (value.getStringValue() != null) {
      return stringValue(value.getStringValue());
    } else {
      return emptyValue();
    }
  }

  protected String errorValue(final Value value) {
    final StringBuilder sb = new StringBuilder();
    if (value.getErrorValue() != null) {
      sb.append("Error ").append(value.getErrorValue());
    }
    final String valueStr = nonErrorValue(value);
    if ((valueStr != null) && !isEmptyValue(valueStr)) {
      sb.append(": ");
      sb.append(valueStr);
    }
    return sb.toString();
  }

  protected String nullData() {
    return null;
  }

  protected String emptyData() {
    return "Data";
  }

  protected String singleData(final Value data) {
    return toString(data);
  }

  protected String linearData(final Value[] data) {
    final StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < data.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(toString(data[i]));
    }
    sb.append(']');
    return sb.toString();
  }

  protected String matrixData(final Value[][] data) {
    final StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = 0; i < data.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append('[');
      for (int j = 0; j < data[i].length; j++) {
        if (j > 0) {
          sb.append(", ");
        }
        sb.append(toString(data[i][j]));
      }
      sb.append(']');
    }
    sb.append(']');
    return sb.toString();
  }

  // ClientMessageStrings

  @Override
  public String toString(final Value value) {
    if (value == null) {
      return nullValue();
    }
    if (value.getErrorValue() != null) {
      return errorValue(value);
    } else {
      return nonErrorValue(value);
    }
  }

  @Override
  public String toString(final Data data) {
    if (data == null) {
      return nullData();
    } else if (data.getSingle() != null) {
      return singleData(data.getSingle());
    } else if (data.getLinear() != null) {
      return linearData(data.getLinear());
    } else if (data.getMatrix() != null) {
      return matrixData(data.getMatrix());
    } else {
      return emptyData();
    }
  }

}
