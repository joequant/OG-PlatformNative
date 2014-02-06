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
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Fetches a property from an object, using a {@link Bean} template if one is available
 */
public class GetObjectPropertyFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetObjectPropertyFunction INSTANCE = new GetObjectPropertyFunction();

  private final MetaFunction _meta;

  private static final int OBJECT = 0;
  private static final int PROPERTY = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter object = new MetaParameter("object", TransportTypes.FUDGE_MSG);
    final MetaParameter property = new MetaParameter("property", PrimitiveTypes.STRING);
    return ImmutableList.of(object, property);
  }

  private GetObjectPropertyFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MISC, "GetObjectProperty", getParameters(), this));
  }

  protected GetObjectPropertyFunction() {
    this(new DefinitionAnnotater(GetObjectPropertyFunction.class));
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg object, final String property) {
    // TODO: See if the object is an encoding of a bean; use that for meta data
    // TODO: Otherwise try and just pick out a named field from the object
    throw new UnsupportedOperationException();
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (FudgeMsg) parameters[OBJECT], (String) parameters[PROPERTY]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
