/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.joda.beans.Bean;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Fetches all properties from an object, using a {@link Bean} template if one is available.
 */
public class GetObjectPropertiesFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetObjectPropertiesFunction INSTANCE = new GetObjectPropertiesFunction();

  private final MetaFunction _meta;

  private static final int OBJECT = 0;

  private static List<MetaParameter> parameters() {
    final MetaParameter object = new MetaParameter("object", TransportTypes.FUDGE_MSG);
    return ImmutableList.of(object);
  }

  private GetObjectPropertiesFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MISC, "GetObjectProperties", getParameters(), this));
  }

  protected GetObjectPropertiesFunction() {
    this(new DefinitionAnnotater(GetObjectPropertiesFunction.class));
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg object) {
    // TODO: Use the bean template if the object is an encoding of one
    // TODO: Otherwise just expand out the Fudge message
    throw new UnsupportedOperationException();
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (FudgeMsg) parameters[OBJECT]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
