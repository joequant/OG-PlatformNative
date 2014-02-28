/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Used when a conversion is not possible.
 */
public class InvalidConversionException extends IllegalArgumentException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final Object _value;
  private final JavaTypeInfo<?> _type;

  public InvalidConversionException(final Object value, final JavaTypeInfo<?> type) {
    super();
    _value = value;
    _type = type;
  }

  public Object getValue() {
    return _value;
  }

  public JavaTypeInfo<?> getTargetType() {
    return _type;
  }

  @Override
  public String getMessage() {
    return "Could not convert from " + _value + " to " + _type;
  }

  /**
   * Returns a version of {@link #getMessage} that is more useful to display to a user but less-so for programmer diagnostics in the stack trace.
   * 
   * @return a tidied message string.
   */
  public String getClientMessage() {
    return createClientMessage(_value, _type);
  }

  /**
   * Constructs the message returned by {@link #getClientMessage}.
   * 
   * @param value the value being converted
   * @param type the target type
   * @return the tidy message string
   */
  public static String createClientMessage(final Object value, final JavaTypeInfo<?> type) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Could not convert ");
    if (value instanceof Value) {
      sb.append(ValueUtils.toString((Value) value, true));
    } else if (value instanceof Data) {
      sb.append(DataUtils.toString((Data) value, true));
    } else {
      sb.append(value.toString());
    }
    sb.append(" to ").append(type.toClientString());
    return sb.toString();
  }

}
