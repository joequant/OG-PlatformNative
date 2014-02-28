/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.error.InvokeInternalException;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Creates a function publication based on assumptions and annotations. This is intended for quick authoring of trivial functions only - more complicated behaviors (some parameter defaults, automatic
 * null/not-null control) will require more manual construction to set the parameter metadata correctly.
 */
public final class AutoPublishedFunction implements PublishedFunction {

  /**
   * The name of the method in the function implementation class that will be published by this wrapper.
   */
  public static final String EXECUTE_METHOD_NAME = "execute";

  private interface ParameterExtract {

    Object parameter(SessionContext sessionContext, Object[] parameters);

  }

  private static final class IndexParameter implements ParameterExtract {

    private final int _index;

    public IndexParameter(final int index) {
      _index = index;
    }

    // ParameterExtract

    @Override
    public Object parameter(final SessionContext sessionContext, final Object[] parameters) {
      return parameters[_index];
    }

  }

  private static final Map<Class<?>, ParameterExtract> s_parameters;

  static {
    s_parameters = new HashMap<Class<?>, ParameterExtract>();
    s_parameters.put(SessionContext.class, new ParameterExtract() {
      @Override
      public Object parameter(final SessionContext sessionContext, final Object[] parameters) {
        return sessionContext;
      }
    });
    s_parameters.put(UserContext.class, new ParameterExtract() {
      @Override
      public Object parameter(final SessionContext sessionContext, final Object[] parameters) {
        return sessionContext.getUserContext();
      }
    });
    s_parameters.put(GlobalContext.class, new ParameterExtract() {
      @Override
      public Object parameter(final SessionContext sessionContext, final Object[] parameters) {
        return sessionContext.getGlobalContext();
      }
    });
    // TODO: Are there any other things that would normally be extracted from a context?
  }

  private static final class NamedParameterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String _name;

    public NamedParameterException(final String name, final String message) {
      super(message);
      _name = name;
    }

    public NamedParameterException(final String name, final Throwable cause) {
      super(cause);
      _name = name;
    }

    public NamedParameterException(final String name, final String message, final Throwable cause) {
      super(message, cause);
      _name = name;
    }

    public RuntimeException translate(final Map<String, Integer> paramsByName) {
      final Integer arg = paramsByName.get(_name);
      if (arg == null) {
        if (getCause() != null) {
          return new InvokeInternalException("parameter " + _name + " not defined", getCause());
        } else {
          return new InvokeInternalException("parameter " + _name + " not defined");
        }
      }
      if (getMessage() != null) {
        if (getCause() != null) {
          return new InvokeInvalidArgumentException(arg, getMessage(), getCause());
        } else {
          return new InvokeInvalidArgumentException(arg, getMessage());
        }
      } else {
        if (getCause() != null) {
          return new InvokeInvalidArgumentException(arg, getCause());
        } else {
          return new InvokeInternalException("invalid internal state");
        }
      }
    }

  }

  private static final class Invoker extends AbstractFunctionInvoker {

    private final Object _instance;
    private final Method _method;
    private final ParameterExtract[] _parameters;
    private final Map<String, Integer> _paramsByName;

    public Invoker(final Object instance, final Method method, final List<MetaParameter> parameters, final ParameterExtract[] extractors, final Map<String, Integer> paramsByName) {
      super(parameters);
      _instance = instance;
      _method = method;
      _parameters = extractors;
      _paramsByName = paramsByName;
    }

    // AbstractFunctionInvoker

    @Override
    protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
      final Object[] invokeParams = new Object[_parameters.length];
      for (int i = 0; i < invokeParams.length; i++) {
        invokeParams[i] = _parameters[i].parameter(sessionContext, parameters);
      }
      try {
        return _method.invoke(_instance, invokeParams);
      } catch (InvocationTargetException e) {
        if (e.getCause() instanceof NamedParameterException) {
          throw ((NamedParameterException) e.getCause()).translate(_paramsByName);
        } else if (e.getCause() instanceof RuntimeException) {
          throw (RuntimeException) e.getCause();
        } else {
          throw new InvokeInternalException(e.getCause());
        }
      } catch (IllegalAccessException | IllegalArgumentException ex) {
        throw new InvokeInternalException(ex);
      }
    }

  }

  private final MetaFunction _meta;

  /**
   * Annotation for providing additional information about a parameter.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  public @interface Param {

    /**
     * The parameter's name, as published to the language binding and referenced in the local environment file.
     */
    String name();

    /**
     * Whether to allow null values.
     */
    boolean allowNull() default true;

    /**
     * The default value to use, as a string.
     */
    String defaultValue() default "";

  }

  /**
   * Creates a new instance.
   * 
   * @param category the category of functions this should be placed in, not null
   * @param name the name of the function, not null
   * @param function either a class containing a method called "execute" that will perform the function's actions or an instance of such a class, not null
   */
  public AutoPublishedFunction(final String category, final String name, final Object function) {
    ArgumentChecker.notNull(category, "category");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(function, "function");
    final Class<?> clazz = getFunctionClass(function);
    final DefinitionAnnotater info = new DefinitionAnnotater(clazz);
    final Method method = getExecuteMethod(clazz);
    if (method == null) {
      throw new IllegalArgumentException(clazz + " does not have a public " + EXECUTE_METHOD_NAME + " method");
    }
    final Object instance = Modifier.isStatic(method.getModifiers()) ? null : getFunctionInstance(function);
    final Invoker invoker = createInvoker(info, instance, method);
    _meta = info.annotate(new MetaFunction(category, name, invoker.getParameters(), invoker));
  }

  protected static Class<?> getFunctionClass(final Object function) {
    if (function instanceof Class) {
      return (Class<?>) function;
    } else {
      return function.getClass();
    }
  }

  protected static Object getFunctionInstance(final Object function) {
    if (function instanceof Class) {
      try {
        return ((Class<?>) function).newInstance();
      } catch (Exception e) {
        throw new IllegalArgumentException(function + " can't be instantiated");
      }
    } else {
      return function;
    }
  }

  protected static Method getExecuteMethod(final Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (EXECUTE_METHOD_NAME.equals(method.getName()) && Modifier.isPublic(method.getModifiers())) {
        return method;
      }
    }
    if (clazz.getSuperclass() != null) {
      return getExecuteMethod(clazz.getSuperclass());
    }
    return null;
  }

  protected static Invoker createInvoker(final DefinitionAnnotater info, final Object instance, final Method method) {
    final Type[] parameterTypes = method.getGenericParameterTypes();
    final ParameterExtract[] parameterInvokes = new ParameterExtract[parameterTypes.length];
    final Annotation[][] parameterAnnos = method.getParameterAnnotations();
    final List<MetaParameter> parameters = new ArrayList<MetaParameter>(parameterTypes.length);
    final Map<String, Integer> paramsByName = new HashMap<String, Integer>();
    int index = 0;
    for (int i = 0; i < parameterTypes.length; i++) {
      final Type parameterType = parameterTypes[i];
      if (parameterType instanceof Class) {
        final ParameterExtract extract = s_parameters.get(parameterType);
        if (extract != null) {
          parameterInvokes[i] = extract;
          continue;
        }
      }
      parameterInvokes[i] = new IndexParameter(index++);
      final Annotation[] annotations = parameterAnnos[i];
      String name = null;
      JavaTypeInfo<?> typeInfo = null;
      boolean rejectNull = false;
      if (annotations != null) {
        for (Annotation annotation : annotations) {
          if (annotation instanceof Param) {
            final Param userParameter = (Param) annotation;
            name = userParameter.name();
            final String defaultValue = userParameter.defaultValue();
            if (defaultValue.length() > 0) {
              if ((parameterType == Boolean.class) || (parameterType == Boolean.TYPE)) {
                typeInfo = JavaTypeInfo.builder(Boolean.class).defaultValue(Boolean.parseBoolean(defaultValue)).get();
              } else if ((parameterType == Integer.class) || (parameterType == Integer.TYPE)) {
                typeInfo = JavaTypeInfo.builder(Integer.class).defaultValue(Integer.parseInt(defaultValue)).get();
              } else if (parameterType == String.class) {
                typeInfo = JavaTypeInfo.builder(String.class).defaultValue(defaultValue).get();
              } else {
                throw new UnsupportedOperationException("Can't assign default value to " + parameterType);
              }
            } else {
              if (!userParameter.allowNull()) {
                rejectNull = true;
              }
            }
          }
        }
      }
      if (name == null) {
        name = "param" + index;
      }
      paramsByName.put(name, index - 1);
      if (typeInfo == null) {
        typeInfo = JavaTypeInfo.ofType(parameterType);
        if (rejectNull) {
          typeInfo = typeInfo.withAllowNull(false);
        }
      }
      final MetaParameter parameter = new MetaParameter(name, typeInfo);
      info.annotate(parameter);
      parameters.add(parameter);
    }
    return new Invoker(instance, method, parameters, parameterInvokes, paramsByName);
  }

  /**
   * Helper method for throwing exceptions that can be correctly routed to the language binding. The parameter name will be translated to the numeric index that matches the registered definition so
   * that a correct message will always be displayed to the user (for example if a language binding has had to rename a parameter that is a reserved word).
   * 
   * @param parameter the parameter name, as defined prior to definition annotation (eg the value from the "Param" annotation)
   * @param message the message to report - this should not include parameter or function names, not null
   * @return the exception to throw
   */
  public static RuntimeException invalidArgumentException(final String parameter, final String message) {
    return new NamedParameterException(parameter, message);
  }

  /**
   * Helper method for throwing exceptions that can be correctly routed to the language binding. The parameter name will be translated to the numeric index that matches the registered definition so
   * that a correct message will always be displayed to the user (for example if a language binding has had to rename a parameter that is a reserved word).
   * 
   * @param parameter the parameter name, as defined prior to definition annotation (eg the value from the "Param" annotation)
   * @param cause the cause of the exception, not null
   * @return the exception to throw
   */
  public static RuntimeException invalidArgumentException(final String parameter, final Throwable cause) {
    return new NamedParameterException(parameter, cause);
  }

  /**
   * Helper method for throwing exceptions that can be correctly routed to the language binding. The parameter name will be translated to the numeric index that matches the registered definition so
   * that a correct message will always be displayed to the user (for example if a language binding has had to rename a parameter that is a reserved word).
   * 
   * @param parameter the parameter name, as defined prior to definition annotation (eg the value from the "Param" annotation)
   * @param message the message to report - this should not include parameter or function names, not null
   * @param cause the cause of the exception, not null
   * @return the exception to throw
   */
  public static RuntimeException invalidArgumentException(final String parameter, final String message, final Throwable cause) {
    return new NamedParameterException(parameter, message, cause);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
