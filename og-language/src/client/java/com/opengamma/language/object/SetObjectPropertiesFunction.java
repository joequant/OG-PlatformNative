/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeMsg;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates properties in an object, using a {@link Bean} template if one is available.
 */
public class SetObjectPropertiesFunction extends AbstractFunctionInvoker implements PublishedFunction {

  @SuppressWarnings({"unchecked", "rawtypes" })
  protected static final JavaTypeInfo<Map<String, Data>> PROPERTIES_TYPE = (JavaTypeInfo) JavaTypeInfo.builder(Map.class).parameter(PrimitiveTypes.STRING)
      .parameter(TransportTypes.DATA_ALLOW_NULL).allowNull().get();

  /**
   * Default instance.
   */
  public static final SetObjectPropertiesFunction INSTANCE = new SetObjectPropertiesFunction();

  private final MetaFunction _meta;

  private static final int OBJECT = 0;
  private static final int PROPERTIES = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter object = new MetaParameter("object", TransportTypes.FUDGE_MSG);
    final MetaParameter properties = new MetaParameter("properties", PROPERTIES_TYPE);
    return ImmutableList.of(object, properties);
  }

  private SetObjectPropertiesFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MISC, "SetObjectProperties", getParameters(), this));
  }

  protected SetObjectPropertiesFunction() {
    this(new DefinitionAnnotater(SetObjectPropertiesFunction.class));
  }

  private static void checkSetObjectProperty(final int error, final Map.Entry<String, Data> property) {
    switch (error) {
      case SetObjectPropertyFunction.PROPERTY:
        throw new InvokeInvalidArgumentException(PROPERTIES, property.getKey() + " not found");
      case SetObjectPropertyFunction.VALUE:
        throw new InvokeInvalidArgumentException(PROPERTIES, property.getKey() + " not correct type");
      case 0:
        return;
      default:
        throw new InternalError();
    }
  }

  protected static Bean setBeanProperties(final SessionContext sessionContext, final MetaBean meta, final BeanBuilder<?> bean, final Map<String, Data> properties) {
    if (properties != null) {
      for (Map.Entry<String, Data> property : properties.entrySet()) {
        checkSetObjectProperty(SetObjectPropertyFunction.setBeanProperty(sessionContext, meta, bean, property.getKey(), property.getValue()), property);
      }
    }
    try {
      return bean.build();
    } catch (RuntimeException e) {
      throw new InvokeInvalidArgumentException(PROPERTIES, e.getMessage(), e);
    }
  }

  /**
   * Updates the bean properties. If the bean is mutable, it can be modified in place (a copy was created during the parameter conversion). If the bean is immutable then a copy must be made and
   * modified via the builder.
   */
  protected static Bean setBeanProperties(final SessionContext sessionContext, final Bean bean, final Map<String, Data> properties) {
    if (properties == null) {
      return bean;
    }
    if (bean instanceof ImmutableBean) {
      final MetaBean metaBean = bean.metaBean();
      final BeanBuilder<?> builder = metaBean.builder();
      builder.setAll(bean.metaBean().createPropertyMap(bean));
      return setBeanProperties(sessionContext, metaBean, builder, properties);
    } else {
      final MetaBean metaBean = bean.metaBean();
      for (Map.Entry<String, Data> property : properties.entrySet()) {
        checkSetObjectProperty(SetObjectPropertyFunction.setBeanProperty(sessionContext, metaBean, bean, property.getKey(), property.getValue()), property);
      }
      return bean;
    }
  }

  protected static Object setObjectProperties(final SessionContext sessionContext, final Object object, final Map<String, Data> properties) {
    if (properties == null) {
      return object;
    }
    for (Map.Entry<String, Data> property : properties.entrySet()) {
      checkSetObjectProperty(SetObjectPropertyFunction.setObjectProperty(sessionContext, object, property.getKey(), property.getValue()), property);
    }
    return object;
  }

  protected static FudgeMsg setFudgeMsgProperties(final SessionContext sessionContext, final FudgeMsg object, final Map<String, Data> properties) {
    if (properties == null) {
      return object;
    }
    // TODO: Test if the FudgeMsg can be decoded to a known object, otherwise just modify the raw message
    throw new UnsupportedOperationException();
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg object, final Map<String, Data> properties) {
    // TODO: Test if the object is a Bean or not
    throw new UnsupportedOperationException();
  }

  // AbstractFunctionInvoker

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (FudgeMsg) parameters[OBJECT], (Map<String, Data>) parameters[PROPERTIES]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
