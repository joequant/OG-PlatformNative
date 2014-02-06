/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fudgemsg.AnnotationReflector;
import org.joda.beans.BeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Helper class for tracking resolved class names.
 */
/* package */final class ClassResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(ClassResolver.class);

  private final ConcurrentMap<String, Class<?>> _types = new ConcurrentHashMap<String, Class<?>>();

  private volatile boolean _reflectionsLoaded;

  public ClassResolver() {
    _types.put("boolean", Boolean.TYPE);
    _types.put("char", Character.TYPE);
    _types.put("double", Double.TYPE);
    _types.put("float", Float.TYPE);
    _types.put("int", Integer.TYPE);
    _types.put("long", Long.TYPE);
    _types.put("short", Short.TYPE);
    _types.put("String", String.class);
  }

  /**
   * Resolves a class name to a class object. Short names such as {@code int} are recognized, as are common short names such as {@code String}, and any unique simple names from {@link BeanDefinition}
   * classes.
   * 
   * @param className the class name to search for, not null
   * @return the resolved class instance, not null
   */
  public Class<?> resolve(final String className) throws ClassNotFoundException {
    Class<?> clazz = _types.get(className);
    if (clazz != null) {
      if (clazz == ClassNotFoundException.class) {
        throw new ClassNotFoundException(className);
      }
      return clazz;
    }
    if (className.indexOf('.') > 0) {
      s_logger.debug("Resolving {}", className);
      try {
        clazz = Class.forName(className);
      } catch (ClassNotFoundException e) {
        _types.put(className, ClassNotFoundException.class);
        throw e;
      }
      _types.put(className, clazz);
      return clazz;
    }
    if (!_reflectionsLoaded) {
      synchronized (this) {
        if (!_reflectionsLoaded) {
          s_logger.info("Loading BeanDefinition classes");
          final AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
          final Set<Class<?>> classes = reflector.getReflector().getTypesAnnotatedWith(BeanDefinition.class);
          final Map<String, Class<?>> simpleNames = Maps.newHashMapWithExpectedSize(classes.size());
          for (Class<?> beanClass : classes) {
            final String simpleName = beanClass.getSimpleName();
            Class<?> existing = simpleNames.put(simpleName, beanClass);
            if (existing != null) {
              if (existing == ClassNotFoundException.class) {
                s_logger.debug("Bean name collision at {}", beanClass);
              } else {
                s_logger.debug("Bean name collision between {} and {}", beanClass, existing);
                simpleNames.put(simpleName, ClassNotFoundException.class);
              }
            } else {
              simpleNames.put(simpleName, beanClass);
            }
            simpleNames.put(beanClass.getName(), beanClass);
          }
          _types.putAll(simpleNames);
          _reflectionsLoaded = true;
        }
      }
    }
    s_logger.debug("Looking up {}", className);
    clazz = _types.get(className);
    if (clazz != null) {
      if (clazz == ClassNotFoundException.class) {
        throw new ClassNotFoundException(className);
      }
      return clazz;
    } else {
      throw new ClassNotFoundException(className);
    }
  }

}
