/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.Data;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.FudgeTypeConverter;
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

  private static void checkSetObjectProperty(final int error, final Map.Entry<?, Data> property) {
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

  private static boolean getSortedPropertiesForUpdate(final PropertyTypeInferer inferer, final Map<String, Data> properties, final MetaProperty<?> property,
      final Map<MetaProperty<?>, Data> propertiesToSet) {
    final Collection<MetaProperty<?>> precedents = inferer.getPrecedentProperties(property);
    if (precedents == null) {
      return !propertiesToSet.containsKey(property);
    } else {
      // Set NULL first to avoid infinite loop
      propertiesToSet.put(property, null);
      for (MetaProperty<?> precedent : precedents) {
        if (!propertiesToSet.containsKey(precedent)) {
          if (getSortedPropertiesForUpdate(inferer, properties, precedent, propertiesToSet)) {
            propertiesToSet.put(precedent, properties.get(precedent.name()));
          }
        }
      }
      propertiesToSet.remove(property);
      return true;
    }
  }

  private static Map<MetaProperty<?>, Data> sortPropertiesForUpdate(final PropertyTypeInferer inferer, final MetaBean meta, final Map<String, Data> properties) {
    try {
      final Map<MetaProperty<?>, Data> propertiesToSet = new LinkedHashMap<MetaProperty<?>, Data>();
      for (Map.Entry<String, Data> propertyEntry : properties.entrySet()) {
        final MetaProperty<?> property = meta.metaProperty(propertyEntry.getKey());
        if (!propertiesToSet.containsKey(property)) {
          if (getSortedPropertiesForUpdate(inferer, properties, property, propertiesToSet)) {
            propertiesToSet.put(property, propertyEntry.getValue());
          }
        }
      }
      return propertiesToSet;
    } catch (NoSuchElementException e) {
      throw new InvokeInvalidArgumentException(PROPERTIES, e.getMessage(), e);
    }
  }

  protected static Bean setBeanProperties(final SessionContext sessionContext, final MetaBean meta, final BeanBuilder<?> bean, final Map<String, Data> properties) {
    if (properties != null) {
      final PropertyTypeInferer inferer = SetObjectPropertyFunction.getPropertyTypeInferer(sessionContext.getGlobalContext());
      if (inferer.hasPrecedentProperties(meta)) {
        for (Map.Entry<MetaProperty<?>, Data> property : sortPropertiesForUpdate(inferer, meta, properties).entrySet()) {
          checkSetObjectProperty(SetObjectPropertyFunction.setBeanProperty(sessionContext, inferer, bean, property.getKey(), property.getValue()), property);
        }
      } else {
        for (Map.Entry<String, Data> property : properties.entrySet()) {
          checkSetObjectProperty(SetObjectPropertyFunction.setBeanPropertyNoInfer(sessionContext, meta, bean, property.getKey(), property.getValue()), property);
        }
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
      for (MetaProperty<?> property : bean.metaBean().metaPropertyIterable()) {
        builder.set(property, property.get(bean));
      }
      return setBeanProperties(sessionContext, metaBean, builder, properties);
    } else {
      final MetaBean metaBean = bean.metaBean();
      final PropertyTypeInferer inferer = SetObjectPropertyFunction.getPropertyTypeInferer(sessionContext.getGlobalContext());
      if (inferer.hasPrecedentProperties(metaBean)) {
        for (Map.Entry<MetaProperty<?>, Data> property : sortPropertiesForUpdate(inferer, metaBean, properties).entrySet()) {
          checkSetObjectProperty(SetObjectPropertyFunction.setBeanProperty(sessionContext, inferer, metaBean, bean, property.getKey(), property.getValue()), property);
        }
      } else {
        for (Map.Entry<String, Data> property : properties.entrySet()) {
          checkSetObjectProperty(SetObjectPropertyFunction.setBeanPropertyNoInfer(sessionContext, metaBean, bean, property.getKey(), property.getValue()), property);
        }
      }
      return bean;
    }
  }

  protected static void setObjectProperties(final SessionContext sessionContext, final Object object, final Map<String, Data> properties) {
    if ((properties == null) || properties.isEmpty()) {
      return;
    }
    for (Map.Entry<String, Data> property : properties.entrySet()) {
      checkSetObjectProperty(SetObjectPropertyFunction.setObjectProperty(sessionContext, object, property.getKey(), property.getValue()), property);
    }
  }

  private static FudgeMsg setFudgeMsgProperties(final SessionContext sessionContext, final FudgeMsg object, final Map<String, Data> properties) {
    final MutableFudgeMsg msg = SetObjectPropertyFunction.mutableMessage(sessionContext, object);
    for (Map.Entry<String, Data> property : properties.entrySet()) {
      SetObjectPropertyFunction.setFudgeMsgProperty(sessionContext, msg, property.getKey(), property.getValue());
    }
    return msg;
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg object, final Map<String, Data> properties) {
    if (properties == null) {
      return object;
    }
    if (object.hasField(0)) {
      final FudgeContext fudgeContext = FudgeTypeConverter.getFudgeContext(sessionContext.getGlobalContext());
      final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
      final Object encodedObject;
      try {
        encodedObject = deserializer.fudgeMsgToObject(object);
      } catch (RuntimeException e) {
        return setFudgeMsgProperties(sessionContext, object, properties);
      }
      if (encodedObject instanceof Bean) {
        return setBeanProperties(sessionContext, (Bean) encodedObject, properties);
      } else {
        setObjectProperties(sessionContext, encodedObject, properties);
        return encodedObject;
      }
    } else {
      return setFudgeMsgProperties(sessionContext, object, properties);
    }
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
