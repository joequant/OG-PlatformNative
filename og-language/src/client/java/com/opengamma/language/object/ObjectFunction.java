/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Creates an object from a {@link Bean} template.
 */
public class ObjectFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final ObjectFunction INSTANCE = new ObjectFunction();

  private final MetaFunction _meta;

  private static final int CLASS = 0;
  private static final int PROPERTIES = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter clazz = new MetaParameter("class", PrimitiveTypes.STRING_ALLOW_NULL);
    final MetaParameter properties = new MetaParameter("properties", SetObjectPropertiesFunction.PROPERTIES_TYPE);
    return ImmutableList.of(clazz, properties);
  }

  private ObjectFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MISC, "Object", getParameters(), this));
  }

  protected ObjectFunction() {
    this(new DefinitionAnnotater(ObjectFunction.class));
  }

  private static Object createObject(final SessionContext sessionContext, final Class<?> clazz, final Map<String, Data> properties) {
    final Object object;
    try {
      object = clazz.newInstance();
    } catch (Throwable e) {
      throw new InvokeInvalidArgumentException(CLASS, "can't create object", e);
    }
    SetObjectPropertiesFunction.setObjectProperties(sessionContext, object, properties);
    return object;
  }

  public static Object invoke(final SessionContext sessionContext, final String className, final Map<String, Data> properties) {
    if (className == null) {
      return SetObjectPropertiesFunction.setBeanProperties(sessionContext, new FlexiBean(), properties);
    } else {
      final Class<?> clazz;
      try {
        clazz = JavaTypeInfo.resolve(className);
      } catch (ClassNotFoundException e) {
        throw new InvokeInvalidArgumentException(CLASS, "not found");
      }
      if (Bean.class.isAssignableFrom(clazz)) {
        final MetaBean meta;
        try {
          meta = JodaBeanUtils.metaBean(clazz);
        } catch (IllegalArgumentException e) {
          return createObject(sessionContext, clazz, properties);
        }
        return SetObjectPropertiesFunction.setBeanProperties(sessionContext, meta, meta.builder(), properties);
      } else {
        return createObject(sessionContext, clazz, properties);
      }
    }
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (String) parameters[CLASS], (Map<String, Data>) parameters[PROPERTIES]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
