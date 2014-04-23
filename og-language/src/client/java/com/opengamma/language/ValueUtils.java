/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.language.error.ClientMessageStrings;
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

  /**
   * Tests whether the {@link Value} instance is a form of null (or the value passed is null). To be considered null the object must not have any of its typed members set.
   * 
   * @param value the value to test
   * @return true if value is null or represents a null value
   */
  public static boolean isNull(final Value value) {
    if (value == null) {
      return true;
    }
    return (value.getBoolValue() == null) && (value.getDoubleValue() == null) && (value.getErrorValue() == null) && (value.getIntValue() == null) && (value.getMessageValue() == null) &&
        (value.getStringValue() == null);
  }

  /**
   * Creates a {@link Value} instance representing the boolean value.
   * 
   * @param boolValue the boolean value, not null
   * @return the new instance, not null
   */
  public static Value of(final Boolean boolValue) {
    ArgumentChecker.notNull(boolValue, "boolValue");
    final Value value = new Value();
    value.setBoolValue(boolValue);
    return value;
  }

  /**
   * Creates a {@link Value} instance representing the double value.
   * 
   * @param doubleValue the double value, not null
   * @return the new instance, not null
   */
  public static Value of(final Double doubleValue) {
    ArgumentChecker.notNull(doubleValue, "doubleValue");
    final Value value = new Value();
    value.setDoubleValue(doubleValue);
    return value;
  }

  /**
   * Creates a {@link Value} instance representing the error value.
   * 
   * @param errorValue the integer error value, see {@link Constants}
   * @return the new instance, not null
   */
  public static Value ofError(final int errorValue) {
    final Value value = new Value();
    value.setErrorValue(errorValue);
    return value;
  }

  /**
   * Creates a {@link Value} instance representing the integer value.
   * 
   * @param intValue the integer value, not null
   * @return the new instance, not null
   */
  public static Value of(final Integer intValue) {
    ArgumentChecker.notNull(intValue, "intValue");
    final Value value = new Value();
    value.setIntValue(intValue);
    return value;
  }

  /**
   * Creates a {@link Value} instance representing the message.
   * <p>
   * The message is held by reference - the caller should either pass an immutable form or not modify the original.
   * 
   * @param messageValue the message value, not null
   * @return the new instance, not null
   */
  public static Value of(final FudgeMsg messageValue) {
    ArgumentChecker.notNull(messageValue, "messageValue");
    final Value value = new Value();
    value.setMessageValue(messageValue);
    return value;
  }

  /**
   * Creates a {@link Value} instance representing the string.
   * 
   * @param stringValue the string value, not null
   * @return the new instance, not null
   */
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
   * @return the value instance, not null
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

  /**
   * Returns the {@link Value} representation as a boolean if possible.
   * <ul>
   * <li>Non-zero numeric values are treated as true
   * <li>Numeric zeroes are treated as false
   * <li>String values of "true" and "false" (ignoring case) are treated as true and false respectively
   * </ul>
   * 
   * @param data the instance to convert
   * @return a boolean form of the value or null if it does not contain suitable data
   * @deprecated conversions from the transport value types should be performed with the type conversion chains instead
   */
  @Deprecated
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
    } else if (data.getStringValue() != null) {
      if ("true".equalsIgnoreCase(data.getStringValue())) {
        return true;
      } else if ("false".equalsIgnoreCase(data.getStringValue())) {
        return false;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Returns the {@link Value} representation as a double if possible.
   * <ul>
   * <li>Boolean values are converted to 1.0 (if true) and 0.0 (if false)
   * <li>String values are parsed using {@link Double#parseDouble}
   * </ul>
   * 
   * @param data the instance to convert
   * @return a double form of the value or null if it does not contain suitable data
   * @deprecated conversions from the transport value types should be performed with the type conversion chains instead
   */
  @Deprecated
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
      try {
        return Double.parseDouble(data.getStringValue());
      } catch (NumberFormatException e) {
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Returns the {@link Value} representation as an integer if possible.
   * <ul>
   * <li>Boolean values are converted to 1 (for true) and 0 (for false)
   * <li>Strings are parsed using {@link Integer#parseInt}
   * </ul>
   * 
   * @param data the instance to convert
   * @return the integer value or null if it does not contain suitable data
   * @deprecated conversions from the transport value types should be performed with the type conversion chains instead
   */
  @Deprecated
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
      try {
        return Integer.parseInt(data.getStringValue());
      } catch (NumberFormatException e) {
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Displayable form of the Value object for use in diagnostics or error reporting. If the object value is required as a string then the registered type conversion chains appropriate to the client
   * should be used.
   * <p>
   * This should only be used when the quoted/unquoted form is acceptable. If the requirements of a specific client binding could vary then indirection through the {@link ClientMessageStrings}
   * instance bound to the global context should be used.
   * 
   * @param value the value to convert to a string
   * @param quoted true to surround strings in quote marks and escape them
   * @return a displayable string representation
   */
  public static String toString(final Value value, final boolean quoted) {
    return (quoted ? ClientMessageStrings.QUOTED_FORM : ClientMessageStrings.UNQUOTED_FORM).toString(value);
  }

  /**
   * Transposes a matrix of values.
   * 
   * @param range the matrix to transpose, not null
   * @return the transposed matrix
   * @throws IllegalArgumentException if the matrix is jagged
   */
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

  private static int compareBool(final Value a, final Value b) {
    final Boolean v1 = a.getBoolValue();
    final Boolean v2 = b.getBoolValue();
    if (v1 == null) {
      if (v2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (v2 == null) {
        return -1;
      } else {
        return v1.compareTo(v2);
      }
    }
  }

  private static int compareInt(final Value a, final Value b) {
    final Integer v1 = a.getIntValue();
    final Integer v2 = b.getIntValue();
    if (v1 == null) {
      if (v2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (v2 == null) {
        return -1;
      } else {
        return v1.compareTo(v2);
      }
    }
  }

  private static int compareDouble(final Value a, final Value b) {
    final Double v1 = a.getDoubleValue();
    final Double v2 = b.getDoubleValue();
    if (v1 == null) {
      if (v2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (v2 == null) {
        return -1;
      } else {
        return v1.compareTo(v2);
      }
    }
  }

  private static int compareString(final Value a, final Value b) {
    final String v1 = a.getStringValue();
    final String v2 = b.getStringValue();
    if (v1 == null) {
      if (v2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (v2 == null) {
        return -1;
      } else {
        return v1.compareTo(v2);
      }
    }
  }

  private static int compareMessage(final Value a, final Value b) {
    final FudgeMsg v1 = a.getMessageValue();
    final FudgeMsg v2 = b.getMessageValue();
    if (v1 == null) {
      if (v2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (v2 == null) {
        return -1;
      } else {
        // Any messages are equal enough (for now) - doesn't really make sense to order them
        return 0;
      }
    }
  }

  private static int compareError(final Value a, final Value b) {
    final Integer v1 = a.getErrorValue();
    final Integer v2 = b.getErrorValue();
    if (v1 == null) {
      if (v2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else {
      if (v2 == null) {
        return -1;
      } else {
        return v1.compareTo(v2);
      }
    }
  }

  /**
   * Compares two {@link Value} instances for the purpose of returning a consistent order where needed. Values are first grouped as:
   * <ul>
   * <li>Errors
   * <li>Booleans
   * <li>Integers
   * <li>Doubles
   * <li>Strings
   * <li>Fudge messages
   * </ul>
   * Then sorted within each group using the natural sort ordering for that type (except for Fudge messages which are considered equal for the purpose of sorting).
   * 
   * @param a the first comparand, not null
   * @param b the second comparant, not null
   * @return -1 if {@code a} should precede {@code b}, 1 if {@code a} should succeed {@code b}, 0 otherwise
   */
  public static int compare(final Value a, final Value b) {
    int c = compareError(a, b);
    if (c != 0) {
      return c;
    }
    c = compareBool(a, b);
    if (c != 0) {
      return c;
    }
    c = compareInt(a, b);
    if (c != 0) {
      return c;
    }
    c = compareDouble(a, b);
    if (c != 0) {
      return c;
    }
    c = compareString(a, b);
    if (c != 0) {
      return c;
    }
    c = compareMessage(a, b);
    if (c != 0) {
      return c;
    }
    return 0;
  }

}
