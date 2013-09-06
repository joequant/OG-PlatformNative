/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import java.util.Arrays;
import java.util.List;

import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Converts a unique identifier to an object identifier, or a unique identifier at version "latest".
 */
public class LatestVersionFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final LatestVersionFunction INSTANCE = new LatestVersionFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).get()));
  }

  private LatestVersionFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.IDENTIFIER, "LatestVersion", getParameters(), this));
  }

  protected LatestVersionFunction() {
    this(new DefinitionAnnotater(LatestVersionFunction.class));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return ((UniqueId) parameters[0]).toLatest();
  }

}
