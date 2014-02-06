/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.beanutils.PropertyUtils;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Updates a property in an object, using a {@link Bean} template if one is available
 */
public class SetObjectPropertyFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final SetObjectPropertyFunction INSTANCE = new SetObjectPropertyFunction();

  private final MetaFunction _meta;

  protected static final int OBJECT = 0;
  protected static final int PROPERTY = 1;
  protected static final int VALUE = 2;
  protected static final int TYPE_CAST = 3;

  private static List<MetaParameter> parameters() {
    final MetaParameter object = new MetaParameter("object", TransportTypes.FUDGE_MSG);
    final MetaParameter property = new MetaParameter("property", PrimitiveTypes.STRING);
    final MetaParameter value = new MetaParameter("value", TransportTypes.DATA);
    final MetaParameter typeCast = new MetaParameter("type", PrimitiveTypes.STRING_ALLOW_NULL);
    return ImmutableList.of(object, property, value, typeCast);
  }

  private SetObjectPropertyFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MISC, "SetObjectProperty", getParameters(), this));
  }

  protected SetObjectPropertyFunction() {
    this(new DefinitionAnnotater(SetObjectPropertyFunction.class));
  }

  private static Object convertValue(final SessionContext sessionContext, final Value value) {
    if (value.getBoolValue() != null) {
      return value.getBoolValue();
    } else if (value.getDoubleValue() != null) {
      return value.getDoubleValue();
    } else if (value.getErrorValue() != null) {
      return null;
    } else if (value.getIntValue() != null) {
      return value.getIntValue();
    } else if (value.getMessageValue() != null) {
      final FudgeMsg message = value.getMessageValue();
      final List<FudgeField> typeHints = message.getAllByOrdinal(0);
      if (!typeHints.isEmpty()) {
        for (FudgeField typeHint : typeHints) {
          if (typeHint.getValue() instanceof String) {
            try {
              final JavaTypeInfo<?> type = JavaTypeInfo.builder(JavaTypeInfo.resolve((String) typeHint.getValue())).get();
              return sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, message, type);
            } catch (Exception e) {
              // Ignore
            }
          }
        }
      }
      return message;
    } else if (value.getStringValue() != null) {
      return value.getStringValue();
    } else {
      assert ValueUtils.isNull(value);
      return null;
    }
  }

  private static Object[] convertValues(final SessionContext sessionContext, final Value[] values) {
    final Object[] result = new Object[values.length];
    int i = 0;
    for (Value value : values) {
      result[i++] = convertValue(sessionContext, value);
    }
    return result;
  }

  private static Object[][] convertValues(final SessionContext sessionContext, final Value[][] values) {
    final Object[][] result = new Object[values.length][];
    int i = 0;
    for (Value[] valuesEntry : values) {
      result[i++] = convertValues(sessionContext, valuesEntry);
    }
    return result;
  }

  private static Object propertyValue(final SessionContext sessionContext, final Data value) {
    if (value.getSingle() != null) {
      return convertValue(sessionContext, value.getSingle());
    } else if (value.getLinear() != null) {
      return convertValues(sessionContext, value.getLinear());
    } else if (value.getMatrix() != null) {
      return convertValues(sessionContext, value.getMatrix());
    } else {
      assert DataUtils.isNull(value);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T propertyValue(final SessionContext sessionContext, final MetaProperty<T> property, final Data value) {
    if (property.propertyType() == Object.class) {
      return (T) propertyValue(sessionContext, value);
    } else {
      final JavaTypeInfo<?> type = JavaTypeInfo.builder(property.propertyGenericType()).get();
      return (T) sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, value, type);
    }
  }

  private static <T> T propertyValue(final SessionContext sessionContext, final Class<T> type, final Data value) {
    return sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, value, JavaTypeInfo.builder(type).get());
  }

  protected static int setBeanProperty(final SessionContext sessionContext, final MetaBean meta, final BeanBuilder<?> bean, final String property, final Data value) {
    final MetaProperty<?> beanProperty;
    try {
      beanProperty = meta.metaProperty(property);
    } catch (NoSuchElementException e) {
      return PROPERTY;
    }
    final Object valueToSet = propertyValue(sessionContext, beanProperty, value);
    try {
      bean.set(beanProperty, valueToSet);
    } catch (RuntimeException e) {
      return VALUE;
    }
    return 0;
  }

  protected static int setBeanProperty(final SessionContext sessionContext, final MetaBean meta, final Bean bean, final String property, final Data value) {
    final MetaProperty<?> beanProperty;
    try {
      beanProperty = meta.metaProperty(property);
    } catch (NoSuchElementException e) {
      return PROPERTY;
    }
    final Object valueToSet = propertyValue(sessionContext, beanProperty, value);
    try {
      beanProperty.set(bean, valueToSet);
    } catch (RuntimeException e) {
      return VALUE;
    }
    return 0;
  }

  protected static int setObjectProperty(final SessionContext sessionContext, final Object object, final String property, final Data value) {
    final PropertyDescriptor descriptor;
    try {
      descriptor = PropertyUtils.getPropertyDescriptor(object, property);
    } catch (Exception e) {
      return PROPERTY;
    }
    final Object valueToSet = propertyValue(sessionContext, descriptor.getPropertyType(), value);
    try {
      PropertyUtils.setProperty(object, descriptor.getName(), valueToSet);
    } catch (Exception e) {
      return VALUE;
    }
    return 0;
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg object, final String property, final Data value, final String typeCast) {
    // TODO
    throw new UnsupportedOperationException();
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (FudgeMsg) parameters[OBJECT], (String) parameters[PROPERTY], (Data) parameters[VALUE], (String) parameters[TYPE_CAST]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
