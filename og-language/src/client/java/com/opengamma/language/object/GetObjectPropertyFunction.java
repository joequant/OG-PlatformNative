/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.PropertyUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
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

  protected static Object getFudgeMsgProperty(final SessionContext sessionContext, final FudgeMsg object, final String property) {
    // TODO: Handle repeated field
    FudgeField field = object.getByName(property);
    if (field != null) {
      return field.getValue();
    } else {
      if ("class".equals(property)) {
        field = object.getByOrdinal(0);
        if (field != null) {
          return field.getValue();
        } else {
          return null;
        }
      } else {
        return null;
      }
    }
  }

  protected static Object getBeanProperty(final SessionContext sessionContext, final Bean object, final String property) {
    if ("class".equals(property)) {
      return object.getClass().getName();
    }
    final MetaProperty<?> metaProperty;
    try {
      metaProperty = object.metaBean().metaProperty(property);
    } catch (NoSuchElementException e) {
      throw new InvokeInvalidArgumentException(PROPERTY, "not found", e);
    }
    try {
      return metaProperty.get(object);
    } catch (RuntimeException e) {
      throw new InvokeInvalidArgumentException(PROPERTY, "can't read property", e);
    }
  }

  protected static Object getObjectProperty(final SessionContext sessionContext, final Object object, final String property) {
    if ("class".equals(property)) {
      return object.getClass().getName();
    }
    try {
      return PropertyUtils.getProperty(object, property);
    } catch (Exception e) {
      throw new InvokeInvalidArgumentException(PROPERTY, "can't read property", e);
    }
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg object, final String property) {
    if (object.hasField(0)) {
      final FudgeContext fudgeContext = FudgeTypeConverter.getFudgeContext(sessionContext.getGlobalContext());
      final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
      final Object encodedObject;
      try {
        encodedObject = deserializer.fudgeMsgToObject(object);
      } catch (RuntimeException e) {
        return getFudgeMsgProperty(sessionContext, object, property);
      }
      if (encodedObject instanceof Bean) {
        return getBeanProperty(sessionContext, (Bean) encodedObject, property);
      } else {
        return getObjectProperty(sessionContext, encodedObject, property);
      }
    } else {
      return getFudgeMsgProperty(sessionContext, object, property);
    }
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
