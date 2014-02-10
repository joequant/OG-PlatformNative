/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import org.apache.commons.lang.StringEscapeUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for converting to/from the {@link Value} type.
 */
public final class ValueUtils {

  /**
   * Prevent instantiation.
   */
  private ValueUtils() {
  }

  public static boolean isNull(final Value value) {
    if (value == null) {
      return true;
    }
    return (value.getBoolValue() == null) && (value.getDoubleValue() == null) && (value.getErrorValue() == null) && (value.getIntValue() == null) && (value.getMessageValue() == null) &&
        (value.getStringValue() == null);
  }

  public static Value of(final Boolean boolValue) {
    ArgumentChecker.notNull(boolValue, "boolValue");
    final Value value = new Value();
    value.setBoolValue(boolValue);
    return value;
  }

  public static Value of(final Double doubleValue) {
    ArgumentChecker.notNull(doubleValue, "doubleValue");
    final Value value = new Value();
    value.setDoubleValue(doubleValue);
    return value;
  }

  public static Value ofError(final int errorValue) {
    final Value value = new Value();
    value.setErrorValue(errorValue);
    return value;
  }

  public static Value of(final Integer intValue) {
    ArgumentChecker.notNull(intValue, "intValue");
    final Value value = new Value();
    value.setIntValue(intValue);
    return value;
  }

  public static Value of(final FudgeMsg messageValue) {
    ArgumentChecker.notNull(messageValue, "messageValue");
    final Value value = new Value();
    value.setMessageValue(messageValue);
    return value;
  }

  public static Value of(final String stringValue) {
    ArgumentChecker.notNull(stringValue, "stringValue");
    final Value value = new Value();
    value.setStringValue(stringValue);
    return value;
  }

  private static Value encodeData(final FudgeContext context, final Data data) {
    if (context == null) {
      throw new IllegalArgumentException();
    }
    final FudgeSerializer ser = new FudgeSerializer(context);
    return of(FudgeSerializer.addClassHeader(ser.objectToFudgeMsg(data), Data.class));
  }

  /**
   * Converts a {@link Data} object to {@link Value} instance. If the data contains a single value then that is used directly. If it is null of any form then a {@code Value} that is null gets
   * returned. For any other cases, the Fudge message representation of the {@code Data} will be used.
   * 
   * @param context the Fudge context to use (only used when encoding {@code Data} to a message)
   * @param data the data to convert
   */
  public static Value of(final FudgeContext context, final Data data) {
    if (data == null) {
      return new Value();
    } else if (data.getSingle() != null) {
      return data.getSingle();
    } else if (data.getLinear() != null) {
      final Value[] linear = data.getLinear();
      if (linear.length == 0) {
        // Not really a legal state
        return new Value();
      } else if (linear.length == 1) {
        return linear[0];
      } else {
        return encodeData(context, data);
      }
    } else if (data.getMatrix() != null) {
      final Value[][] matrix = data.getMatrix();
      if (matrix.length == 0) {
        // Not really a legal state
        return new Value();
      } else if (matrix.length == 1) {
        if (matrix[0].length == 0) {
          // Not really a legal state
          return new Value();
        } else if (matrix[0].length == 1) {
          return matrix[0][0];
        } else {
          return encodeData(context, data);
        }
      } else {
        return encodeData(context, data);
      }
    } else {
      return new Value();
    }
  }

  public static Boolean toBool(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getErrorValue() != null) {
      return null;
    } else if (data.getBoolValue() != null) {
      return data.getBoolValue();
    } else if (data.getIntValue() != null) {
      return data.getIntValue() != 0;
    } else if (data.getDoubleValue() != null) {
      return data.getDoubleValue() != 0;
    } else {
      return null;
    }
  }

  public static Double toDouble(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getErrorValue() != null) {
      return null;
    } else if (data.getDoubleValue() != null) {
      return data.getDoubleValue();
    } else if (data.getIntValue() != null) {
      return data.getIntValue().doubleValue();
    } else if (data.getBoolValue() != null) {
      return data.getBoolValue() ? 1.0 : 0.0;
    } else if (data.getStringValue() != null) {
      return Double.parseDouble(data.getStringValue());
    } else {
      return null;
    }
  }

  public static Integer toError(final Value data) {
    if (data == null) {
      return null;
    }
    if (data.getErrorValue() != null) {
      return data.getErrorValue();
    } else {
      return null;
    }
  }

  public static Integer toInt(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getErrorValue() != null) {
      return null;
    } else if (data.getIntValue() != null) {
      return data.getIntValue();
    } else if (data.getDoubleValue() != null) {
      return data.getDoubleValue().intValue();
    } else if (data.getBoolValue() != null) {
      return data.getBoolValue() ? 1 : 0;
    } else if (data.getStringValue() != null) {
      return Integer.parseInt(data.getStringValue());
    } else {
      return null;
    }
  }

  public static FudgeMsg toMessage(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getMessageValue() != null) {
      return data.getMessageValue();
    } else {
      return null;
    }
  }

  /**
   * Displayable form of the Value object.
   * 
   * @param value the value to convert to a string
   * @param quoted true to surround strings in quote marks and escape them
   * @return a displayable string representation
   */
  public static String toString(final Value value, final boolean quoted) {
    if (value == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    if (value.getErrorValue() != null) {
      sb.append("{Error ").append(value.getErrorValue());
    }
    if (value.getBoolValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getBoolValue());
    }
    if (value.getDoubleValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getDoubleValue());
    }
    if (value.getIntValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getIntValue());
    }
    if (value.getMessageValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getMessageValue());
    }
    if (value.getStringValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      if (quoted) {
        sb.append("\"");
        sb.append(StringEscapeUtils.escapeJava(value.getStringValue()));
        sb.append("\"");
      } else {
        sb.append(value.getStringValue());
      }
    }
    if (value.getErrorValue() != null) {
      sb.append("}");
    }
    return sb.toString();
  }

  public static Object toObject(final Value value) {
    if (value == null) {
      return null;
    }
    if (value.getErrorValue() != null) {
      // TODO: This is indistinguishable from the integer value. I reckon it should be an AbstractException instance containing the error value & any other payload. 
      return value.getErrorValue();
    }
    if (value.getBoolValue() != null) {
      return value.getBoolValue();
    }
    if (value.getDoubleValue() != null) {
      return value.getDoubleValue();
    }
    if (value.getIntValue() != null) {
      return value.getIntValue();
    }
    if (value.getMessageValue() != null) {
      return value.getMessageValue();
    }
    if (value.getStringValue() != null) {
      return value.getStringValue();
    }
    return null;
  }

  public static Value[][] transpose(final Value[][] range) {
    final int rowCount = range.length;
    final int columnCount = rowCount > 0 ? range[0].length : 0;
    final Value[][] transposedRange = new Value[columnCount][rowCount];
    int i = 0;
    int j;
    for (Value[] row : range) {
      if (row.length != columnCount) {
        throw new IllegalArgumentException("Unexpected jagged input range");
      }
      j = 0;
      for (Value value : row) {
        transposedRange[j][i] = value;
        j++;
      }
      i++;
    }
    return transposedRange;
  }

}
