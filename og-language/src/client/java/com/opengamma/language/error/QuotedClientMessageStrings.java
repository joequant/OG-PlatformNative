/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Applies Java escaping to any string values.
 */
public class QuotedClientMessageStrings extends DefaultClientMessageStrings {

  // DefaultClientMessageStrings

  @Override
  public String stringValue(final String value) {
    return "\"" + StringEscapeUtils.escapeJava(value) + "\"";
  }

}
