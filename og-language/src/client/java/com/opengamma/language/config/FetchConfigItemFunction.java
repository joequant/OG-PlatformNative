/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.IdUtils;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Fetches a configuration item from the {@link ConfigSource}.
 * <p>
 * Most "fetch" functions have an identifier as the first parameter as this is typically known (or there is a non-trivial lookup/resolution mechanism). Configuration documents are typically unique by
 * their type/name pair so this is a more useful and common key to reference them by - hence the parameter ordering.
 */
public class FetchConfigItemFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final FetchConfigItemFunction INSTANCE = new FetchConfigItemFunction();

  private static final int TYPE = 0;
  private static final int NAME = 1;
  private static final int IDENTIFIER = 2;

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter typeParameter = new MetaParameter("type", PrimitiveTypes.STRING_ALLOW_NULL);
    final MetaParameter nameParameter = new MetaParameter("name", PrimitiveTypes.STRING_ALLOW_NULL);
    final MetaParameter identifierParameter = new MetaParameter("identifier", OpenGammaTypes.UNIQUE_ID_ALLOW_NULL);
    return ImmutableList.of(typeParameter, nameParameter, identifierParameter);
  }

  private FetchConfigItemFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.CONFIG, "FetchConfigItem", getParameters(), this));
  }

  protected FetchConfigItemFunction() {
    this(new DefinitionAnnotater(FetchConfigItemFunction.class));
  }

  private static Class<?> getType(final String type) {
    try {
      return JavaTypeInfo.resolve(type);
    } catch (ClassNotFoundException e) {
      throw new InvokeInvalidArgumentException(TYPE, e);
    }
  }

  private static Object getItem(final ConfigItem<?> item) {
    final Object value = item.getValue();
    IdUtils.setInto(value, item.getUniqueId());
    return value;
  }

  private static Object getItem(final Collection<ConfigItem<?>> items) {
    final int size = items.size();
    if (size == 1) {
      return getItem(items.iterator().next());
    } else {
      final Collection<Object> result = new ArrayList<Object>(size);
      for (ConfigItem<?> item : items) {
        result.add(getItem(item));
      }
      return result;
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  protected static Object invoke(final SessionContext sessionContext, final String type, final String name, final UniqueId identifier) {
    if (identifier != null) {
      final ConfigItem<?> item;
      try {
        item = sessionContext.getGlobalContext().getConfigSource().get(identifier);
      } catch (RuntimeException e) {
        throw new InvokeInvalidArgumentException(IDENTIFIER, "identifier not found");
      }
      if (type != null) {
        if (!getType(type).isAssignableFrom(item.getType())) {
          throw new InvokeInvalidArgumentException(TYPE, "item is a " + item.getType().getSimpleName());
        }
      }
      if (name != null) {
        if (!name.equals(item.getName())) {
          throw new InvokeInvalidArgumentException(NAME, "item is called " + item.getName());
        }
      }
      return getItem(item);
    } else {
      if (type != null) {
        final Class clazz = getType(type);
        final Collection<ConfigItem<?>> item;
        if (name != null) {
          item = sessionContext.getGlobalContext().getConfigSource().get(clazz, name, VersionCorrection.LATEST);
          if (item.isEmpty()) {
            throw new InvokeInvalidArgumentException(NAME, "no items found");
          }
        } else {
          item = sessionContext.getGlobalContext().getConfigSource().getAll(clazz, VersionCorrection.LATEST);
          if ((item == null) || item.isEmpty()) {
            throw new InvokeInvalidArgumentException(TYPE, "no items found");
          }
        }
        return getItem(item);
      } else {
        if (name != null) {
          throw new InvokeInvalidArgumentException(TYPE, "type must be specified to search by name");
        } else {
          throw new InvokeInvalidArgumentException("at least one parameter must be specified");
        }
      }
    }
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (String) parameters[TYPE], (String) parameters[NAME], (UniqueId) parameters[IDENTIFIER]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
