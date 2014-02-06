/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.Constants;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.language.invoke.ValueConverter;

/**
 * Fetches all properties from an object, using a {@link Bean} template if one is available.
 */
public class GetObjectPropertiesFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(GetObjectPropertiesFunction.class);

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

  private static Data convertValue(final ValueConverter converter, final SessionContext sessionContext, final Object value) {
    final Data resultValue;
    try {
      resultValue = converter.convertValue(sessionContext, value, TransportTypes.DATA_ALLOW_NULL);
    } catch (RuntimeException e) {
      s_logger.debug("Caught exception", e);
      final Value errorValue = ValueUtils.ofError(Constants.ERROR_RESULT_CONVERSION);
      errorValue.setStringValue(e.getMessage());
      return DataUtils.of(errorValue);
    }
    if (resultValue == null) {
      return new Data();
    } else {
      return resultValue;
    }
  }

  protected static Map<String, Data> getBeanProperties(final SessionContext sessionContext, final Bean object) {
    final MetaBean meta = object.metaBean();
    final ValueConverter converter = sessionContext.getGlobalContext().getValueConverter();
    final Map<String, Data> result = new HashMap<String, Data>();
    for (Map.Entry<String, Property<?>> propertyEntry : meta.createPropertyMap(object).entrySet()) {
      result.put(propertyEntry.getKey(), convertValue(converter, sessionContext, propertyEntry.getValue().get()));
    }
    return result;
  }

  protected static Map<String, Data> getFudgeMsgProperties(final SessionContext sessionContext, final FudgeMsg object) {
    final ValueConverter converter = sessionContext.getGlobalContext().getValueConverter();
    final Map<String, Data> result = new HashMap<String, Data>();
    for (FudgeField field : object) {
      final String name = field.getName();
      if (name != null) {
        Data value = converter.convertValue(sessionContext, field.getValue(), TransportTypes.DATA_ALLOW_NULL);
        if (value == null) {
          value = new Data();
        }
        result.put(name, value);
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  protected static Map<String, Data> getObjectProperties(final SessionContext sessionContext, final Object object) {
    final Map<String, Object> properties;
    try {
      properties = PropertyUtils.describe(object);
    } catch (Exception e) {
      throw new InvokeInvalidArgumentException(OBJECT, "Can't read properties");
    }
    final ValueConverter converter = sessionContext.getGlobalContext().getValueConverter();
    final Map<String, Data> result = Maps.newHashMapWithExpectedSize(properties.size());
    for (Map.Entry<String, Object> property : properties.entrySet()) {
      final String name = property.getKey();
      if (!"class".equals(name)) {
        result.put(name, convertValue(converter, sessionContext, property.getValue()));
      }
    }
    return result;
  }

  public static Map<String, Data> invoke(final SessionContext sessionContext, final FudgeMsg object) {
    if (object.hasField(0)) {
      final FudgeContext fudgeContext = FudgeTypeConverter.getFudgeContext(sessionContext.getGlobalContext());
      final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
      final Object encodedObject;
      try {
        encodedObject = deserializer.fudgeMsgToObject(object);
      } catch (RuntimeException e) {
        return getFudgeMsgProperties(sessionContext, object);
      }
      if (encodedObject instanceof Bean) {
        return getBeanProperties(sessionContext, (Bean) encodedObject);
      } else {
        return getObjectProperties(sessionContext, encodedObject);
      }
    } else {
      return getFudgeMsgProperties(sessionContext, object);
    }
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
