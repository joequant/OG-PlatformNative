/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.CoreModelTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates an attribute of a position
 */
public class SetPositionAttributeFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetPositionAttributeFunction INSTANCE = new SetPositionAttributeFunction();

  private final MetaFunction _meta;

  private static final int POSITION = 0;
  private static final int ATTRIBUTE = 1;
  private static final int VALUE = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter security = new MetaParameter("position", CoreModelTypes.POSITION);
    final MetaParameter attribute = new MetaParameter("attribute", PrimitiveTypes.STRING);
    final MetaParameter value = new MetaParameter("value", PrimitiveTypes.STRING_ALLOW_NULL);
    return ImmutableList.of(security, attribute, value);
  }

  private SetPositionAttributeFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "SetPositionAttribute", getParameters(), this));
  }

  protected SetPositionAttributeFunction() {
    this(new DefinitionAnnotater(SetPositionAttributeFunction.class));
  }

  public static Position invoke(final Position position, final String attribute, final String value) {
    final SimplePosition newPosition = new SimplePosition(position);
    if (value != null) {
      newPosition.addAttribute(attribute, value);
    } else {
      newPosition.removeAttribute(attribute);
    }
    return newPosition;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke((Position) parameters[POSITION], (String) parameters[ATTRIBUTE], (String) parameters[VALUE]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
