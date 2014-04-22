/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.error.InvokeParameterConversionException;
import com.opengamma.language.error.InvokeResultConversionException;

/**
 * Used when a conversion is not possible.
 * <p>
 * This is used internally by the conversion routines to report details of the conversion fault. The callers to such routines, for example for parameter conversion, should catch these exceptions and
 * convert them to ones containing additional meta-data needed by the client using the {@code toXXXException} methods.
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
   * @param context the context describing the client environment that the message is localized to
   * @return a tidied message string.
   */
  public String getClientMessage(final GlobalContext context) {
    return createClientMessage(context, _value, _type);
  }

  /**
   * Constructs the message returned by {@link #getClientMessage}.
   * 
   * @param context the context describing the client environment that the message is localized to
   * @param value the value being converted
   * @param type the target type
   * @return the tidy message string
   */
  public static String createClientMessage(final GlobalContext context, final Object value, final JavaTypeInfo<?> type) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Could not convert ");
    if (value instanceof Value) {
      sb.append(context.getClientMessageStrings().toString((Value) value));
    } else if (value instanceof Data) {
      sb.append(context.getClientMessageStrings().toString((Data) value));
    } else {
      sb.append(value.toString());
    }
    sb.append(" to ").append(type.toClientString());
    return sb.toString();
  }

  /**
   * Converts the exception to a {@link InvokeParameterConversionException} for handling by the client connector. This should be used to create a form with a client specific error message and
   * reference to the offending parameter.
   * 
   * @param context the calling client's global context, not null
   * @param parameterIndex the zero-based index of the parameter that caused this conversion error
   * @return the converted exception, not null
   */
  public InvokeParameterConversionException toParameterConversionException(final GlobalContext context, final int parameterIndex) {
    return new InvokeParameterConversionException(parameterIndex, getClientMessage(context), this);
  }

  /**
   * Converts the exception to a {@link InvokeResultConversionException} for handling by the client connector. This should be used to create a form with a client specific error message. This must be
   * used for functions, procedures and live-data connections that return single results only. Multiple result operations must use {@link #toResultConversionException(GlobalContext,int)} instead.
   * 
   * @param context the calling client's global context, not null
   * @return the converted exception, not null
   */
  public InvokeResultConversionException toResultConversionException(final GlobalContext context) {
    return new InvokeResultConversionException(getClientMessage(context), this);
  }

  /**
   * Converts the exception to a {@link InvokeResultConversionException} for handling by the client connector. This should be used to create a form with a client specific error message and reference
   * to the offending result.
   * 
   * @param context the calling client's global context, not null
   * @param resultIndex the zero-based index of the parameter that caused this conversion error
   * @return the converted exception, not null
   */
  public InvokeResultConversionException toResultConversionException(final GlobalContext context, final int resultIndex) {
    return new InvokeResultConversionException(resultIndex, getClientMessage(context), this);
  }

}
