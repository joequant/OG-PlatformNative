/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.Parameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * A function which creates an object from a public constructor.
 * 
 * @param <T> the class being constructed
 */
public class CreateObjectFunction<T> implements PublishedFunction {

  private Constructor<T> _constructor;
  private int _constructorParameterCount;

  private MetaBean _metaBean;
  private final int _prependedParameterCount;
  private final int _appendedParameterCount;
  private final MetaFunction _definition;

  /**
   * Constructs a function instance.
   * 
   * @param category category of the function
   * @param name name of the function, not null
   * @param clazz the class whose constructor is being exposed, not null
   * @param description the description, not null
   * @param parameterNames the constructor parameters, not null
   * @param parameterDescriptions the constructor parameter descriptions, not null
   */
  public CreateObjectFunction(final String category, final String name, final Class<T> clazz, final String description, final String[] parameterNames, final String[] parameterDescriptions) {
    final List<MetaParameter> parameters = new ArrayList<>();
    final List<MetaParameter> prependedParameters = getPrependedParameters();
    _prependedParameterCount = (prependedParameters != null) ? prependedParameters.size() : 0;
    final List<MetaParameter> appendedParameters = getAppendedParameters();
    _appendedParameterCount = (appendedParameters != null) ? appendedParameters.size() : 0;

    if (_prependedParameterCount > 0) {
      parameters.addAll(prependedParameters);
    }
    if (ImmutableBean.class.isAssignableFrom(clazz)) {
      _metaBean = JodaBeanUtils.metaBean(clazz);
      for (int i = 0; i < parameterNames.length; i++) {
        MetaProperty<Object> metaProperty = _metaBean.metaProperty(parameterNames[i]);
        final MetaParameter parameter = new MetaParameter(parameterNames[i], JavaTypeInfo.builder(metaProperty.declaringType()).get());
        parameter.setDescription(parameterDescriptions[i]);
        parameters.add(parameter);
      }
    } else {
      _constructor = findPublicConstructor(clazz, parameterNames.length);
      final Class<?>[] constructorParameters = _constructor.getParameterTypes();
      _constructorParameterCount = constructorParameters.length;
      if (_constructorParameterCount != parameterNames.length) {
        throw new OpenGammaRuntimeException("Constructor has " + _constructorParameterCount + ", expected " + parameterNames.length);
      }
      for (int i = 0; i < parameterNames.length; i++) {
        final MetaParameter parameter = new MetaParameter(parameterNames[i], JavaTypeInfo.builder(constructorParameters[i]).get());
        parameter.setDescription(parameterDescriptions[i]);
        parameters.add(parameter);
      }
    }
    if (_appendedParameterCount > 0) {
      parameters.addAll(appendedParameters);
    }
    _definition = new MetaFunction(category, name, parameters, new AbstractFunctionInvoker(parameters) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        return postConstruction(sessionContext, createObject(parameters), parameters);
      }
    });
    _definition.setDescription(description);
  }

  public CreateObjectFunction(final String category, final String name, final Constructor<T> constructor, final String description, final String[] parameterNames,
      final String[] parameterDescriptions) {
    _constructor = constructor;
    final Class<?>[] constructorParameters = constructor.getParameterTypes();
    _constructorParameterCount = constructorParameters.length;
    if (_constructorParameterCount != parameterNames.length) {
      throw new OpenGammaRuntimeException("Constructor has " + _constructorParameterCount + ", expected " + parameterNames.length);
    }
    final List<MetaParameter> prependedParameters = getPrependedParameters();
    _prependedParameterCount = (prependedParameters != null) ? prependedParameters.size() : 0;
    final List<MetaParameter> appendedParameters = getAppendedParameters();
    _appendedParameterCount = (appendedParameters != null) ? appendedParameters.size() : 0;
    final List<MetaParameter> parameters = new ArrayList<MetaParameter>(_constructorParameterCount + _prependedParameterCount + _appendedParameterCount);
    if (_prependedParameterCount > 0) {
      parameters.addAll(prependedParameters);
    }
    for (int i = 0; i < parameterNames.length; i++) {
      final MetaParameter parameter = new MetaParameter(parameterNames[i], JavaTypeInfo.builder(constructorParameters[i]).get());
      parameter.setDescription(parameterDescriptions[i]);
      parameters.add(parameter);
    }
    if (_appendedParameterCount > 0) {
      parameters.addAll(appendedParameters);
    }
    _definition = new MetaFunction(category, name, parameters, new AbstractFunctionInvoker(parameters) {
      @Override
      protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
        return postConstruction(sessionContext, createObject(parameters), parameters);
      }
    });
    _definition.setDescription(description);
  }

  @SuppressWarnings("unchecked")
  private static <T> Constructor<T> findPublicConstructor(final Class<T> clazz, final int parameterCount) {
    List<Constructor<T>> candidates = new ArrayList<Constructor<T>>();
    for (Constructor<T> constructor : (Constructor<T>[]) clazz.getConstructors()) {
      if ((constructor.getModifiers() & Modifier.PUBLIC) != 0 && constructor.getParameterTypes().length == parameterCount) {
        candidates.add(constructor);
      }
    }
    if (candidates.size() == 0) {
      throw new OpenGammaRuntimeException("Class '" + clazz + "' does not have any public constructors with " + parameterCount + " parameters");
    }
    if (candidates.size() > 1) {
      throw new OpenGammaRuntimeException("Class '" + clazz + "' has multiple public constructors with " + parameterCount + " parameters");
    }
    return candidates.get(0);
  }

  /**
   * Registers additional arguments which are to be expected before those required for construction of the object. These may be used during post-construction operations.
   * 
   * @return the additional parameters, null if none
   */
  protected List<MetaParameter> getPrependedParameters() {
    return null;
  }

  /**
   * Registers additional parameters which are to be expected after those required for constructor of the object. These may be used during post-construction operations.
   * 
   * @return the additional parameters, null if none
   */
  protected List<MetaParameter> getAppendedParameters() {
    return null;
  }

  private T createObject(final Object[] parameters) {
    final Object[] args;
    if ((_prependedParameterCount != 0) || (_appendedParameterCount != 0)) {
      args = new Object[_constructorParameterCount];
      for (int i = 0; i < _constructorParameterCount; i++) {
        args[i] = parameters[_prependedParameterCount + i];
      }
    } else {
      args = parameters;
    }
    try {
      if (_constructor != null) {
        return _constructor.newInstance(args);
      } else if (_metaBean != null) {
        BeanBuilder builder = _metaBean.builder();
        int i = 0;
        for (Parameter parameter : _definition.getParameter()) {
          builder = builder.set(parameter.getName(), parameters[i]);
          i++;
        }
        return (T) builder.build();
      } else {
        throw new OpenGammaRuntimeException("Create object function couldn't find object constructor nor the object is of org.joda.beans.Bean type");
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Processes the newly-constructed object. This provides the opportunity to set properties which are not part of the constructor, or to substitute the object with a different one.
   * 
   * @param context the current session context
   * @param newInstance the newly create object
   * @param parameters the original parameters
   * @return the object to return
   */
  protected Object postConstruction(final SessionContext context, final T newInstance, final Object[] parameters) {
    return newInstance;
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _definition;
  }

}
