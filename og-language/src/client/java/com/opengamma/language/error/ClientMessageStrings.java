/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;

/**
 * Used to generate strings for reporting to the bound client in the language of the client. A specific implementation will be bound to the global context and be correctly configured for any quoting
 * requirements the client has and a level of verbosity appropriate to the client environment.
 * <p>
 * Whenever exception texts are generated for the {@link AbstractException} hierarchy then it should indirect through these to allow a binding to customise it.
 */
public interface ClientMessageStrings {

  /**
   * Uses the default {@link Object#toString} implementation of the objects. This is intended for diagnostic/debugging purposes only. Most client bindings will require a more understandable form.
   */
  ClientMessageStrings DEFAULT_TO_STRING = new ClientMessageStrings() {

    @Override
    public String toString(final Value value) {
      return value.toString();
    }

    @Override
    public String toString(final Data data) {
      return data.toString();
    }

  };

  /**
   * Applies Java escaping to any string values.
   */
  ClientMessageStrings QUOTED_FORM = new ClientMessageStrings() {

    @Override
    public String toString(final Value value) {
      return ValueUtils.toString(value, true);
    }

    @Override
    public String toString(final Data data) {
      return DataUtils.toString(data, true);
    }

  };

  /**
   * Applies no quoting to string values.
   */
  ClientMessageStrings UNQUOTED_FORM = new ClientMessageStrings() {

    @Override
    public String toString(final Value value) {
      return ValueUtils.toString(value, false);
    }

    @Override
    public String toString(final Data data) {
      return DataUtils.toString(data, false);
    }

  };

  /**
   * Produces a summary form, discarding much of the original information in the interests of brevity.
   */
  ClientMessageStrings SIMPLE_FORM = new ClientMessageStrings() {

    @Override
    public String toString(final Value value) {
      return ValueUtils.toSimpleString(value);
    }

    @Override
    public String toString(final Data data) {
      return DataUtils.toSimpleString(data);
    }

  };

  /**
   * Returns a representation of a {@link Value} instance for use in a client message.
   * 
   * @param value the value instance, or null
   * @return the client message, or null if that is meaningful to the client
   */
  String toString(Value value);

  /**
   * Returns a representation of a {@link Data} instance for use in a client message.
   * 
   * @param data the data instance, or null
   * @return the client message, or null if that is meaningful to the client
   */
  String toString(Data data);

}
