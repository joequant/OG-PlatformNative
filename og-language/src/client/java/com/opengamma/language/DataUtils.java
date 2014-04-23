/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import org.fudgemsg.FudgeMsg;

import com.opengamma.language.error.ClientMessageStrings;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for converting to/from the {@link Data} type.
 */
public final class DataUtils {

  /**
   * Prevent instantiation.
   */
  private DataUtils() {
  }

  /**
   * Tests whether the {@link Data} instance is a form of null. To be considered null the object must not have any structure members set, or contain a single {@link Value} that is a form of null.
   * 
   * @param value the value to test
   * @return true if value is null or represents a null value
   */
  public static boolean isNull(final Data value) {
    if (value.getSingle() != null) {
      return ValueUtils.isNull(value.getSingle());
    } else if (value.getLinear() != null) {
      return false;
    } else if (value.getMatrix() != null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Creates a {@link Data} instance containing a single value.
   * 
   * @param value the single value, not null
   * @return the new instance, not null
   */
  public static Data of(final Value value) {
    ArgumentChecker.notNull(value, "value");
    final Data data = new Data();
    data.setSingle(value);
    return data;
  }

  /**
   * Creates a {@link Data} instance containing a single boolean value.
   * 
   * @param boolValue the boolean value
   * @return the new instance, not null
   */
  public static Data of(final boolean boolValue) {
    return of(ValueUtils.of(boolValue));
  }

  /**
   * Creates a {@link Data} instance containing a single double value.
   * 
   * @param doubleValue the double value
   * @return the new instance, not null
   */
  public static Data of(final double doubleValue) {
    return of(ValueUtils.of(doubleValue));
  }

  /**
   * Creates a {@link Data} instance containing a single error value.
   * 
   * @param errorValue the error value
   * @return the new instance, not null
   */
  public static Data ofError(final int errorValue) {
    return of(ValueUtils.ofError(errorValue));
  }

  /**
   * Creates a {@link Data} instance containing a single integer value.
   * 
   * @param intValue the integer value
   * @return the new instance, not null
   */
  public static Data of(final int intValue) {
    return of(ValueUtils.of(intValue));
  }

  /**
   * Creates a {@link Data} instance containing a single message value.
   * <p>
   * Note that the message is held by reference. Either an immutable form should be passed or the caller not modify the instance afterwards.
   * 
   * @param messageValue the error value, not null
   * @return the new instance, not null
   */
  public static Data of(final FudgeMsg messageValue) {
    return of(ValueUtils.of(messageValue));
  }

  /**
   * Creates a {@link Data} instance containing a single string value.
   * 
   * @param stringValue the string value, not null
   * @return the new instance, not null
   */
  public static Data of(final String stringValue) {
    return of(ValueUtils.of(stringValue));
  }

  /**
   * Creates a {@link Data} instance containing a 1D array of values.
   * 
   * @param values the member values, not null and not containing nulls
   * @return the new instance, not null
   */
  public static Data of(final Value[] values) {
    ArgumentChecker.notNull(values, "values");
    for (int i = 0; i < values.length; i++) {
      ArgumentChecker.notNull(values[i], "value[" + i + "]");
    }
    final Data data = new Data();
    data.setLinear(values);
    return data;
  }

  /**
   * Creates a {@link Data} instance containing a matrix of values.
   * 
   * @param values the member values, not null and not containing nulls
   * @return the new instance, not null
   */
  public static Data of(final Value[][] values) {
    ArgumentChecker.notNull(values, "values");
    for (int i = 0; i < values.length; i++) {
      ArgumentChecker.notNull(values[i], "value[" + i + "]");
      for (int j = 0; j < values[i].length; j++) {
        ArgumentChecker.notNull(values[i][j], "value[" + i + "][" + j + "]");
      }
    }
    final Data data = new Data();
    data.setMatrix(values);
    return data;
  }

  /**
   * Displayable form of the Data object for use in diagnostics or error reporting. If the object value is required as a string then the registered type conversion chains appropriate to the client
   * should be used.
   * <p>
   * This should only be used when the quoted/unquoted form is acceptable. If the requirements of a specific client binding could vary then indirection through the {@link ClientMessageStrings}
   * instance bound to the global context should be used.
   * 
   * @param data the object to convert to a string
   * @param quoted true to put quote marks around strings and escape them
   * @return the displayable string
   */
  public static String toString(final Data data, final boolean quoted) {
    return (quoted ? ClientMessageStrings.QUOTED_FORM : ClientMessageStrings.UNQUOTED_FORM).toString(data);
  }

  private static Value toValue(final Data data) {
    if (data.getSingle() != null) {
      return data.getSingle();
    } else if (data.getLinear() != null) {
      if (data.getLinear().length > 0) {
        return data.getLinear()[0];
      } else {
        return null;
      }
    } else if (data.getMatrix() != null) {
      if (data.getMatrix().length > 0) {
        if (data.getMatrix()[0].length > 0) {
          return data.getMatrix()[0][0];
        } else {
          return null;
        }
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Returns the value as a boolean, if it contains a boolean value.
   * 
   * @param data the value to query, not null
   * @return the boolean value or null if it does not contain one
   * @deprecated This has been used to handle "boolean" parameters which should have been processed with the registered type conversion chains instead
   */
  @Deprecated
  public static Boolean toBool(final Data data) {
    return ValueUtils.toBool(toValue(data));
  }

}
