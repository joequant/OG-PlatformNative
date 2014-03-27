/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.object;

import static com.opengamma.lambdava.streams.Lambdava.functional;
import static com.opengamma.language.object.SetObjectPropertyFunction.propertyValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.joda.beans.Bean;

import com.google.common.collect.ImmutableList;
import com.opengamma.lambdava.functions.Function1;
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
import com.opengamma.language.invoke.InvalidConversionException;

/**
 * Fetches a property from an object, using a {@link Bean} template if one is available
 */
public class InvokeObjectMethodFunction extends AbstractFunctionInvoker implements PublishedFunction {


  @SuppressWarnings({"unchecked", "rawtypes" })
  protected static final JavaTypeInfo<List<Data>> ARGUMENTS_TYPE = (JavaTypeInfo) JavaTypeInfo.builder(List.class).parameter(
      TransportTypes.DATA_ALLOW_NULL).allowNull().get();

  /**
   * Default instance.
   */
  public static final InvokeObjectMethodFunction INSTANCE = new InvokeObjectMethodFunction();


  private final MetaFunction _meta;

  private static final int OBJECT = 0;
  private static final int METHOD = 1;
  private static final int ARGUMENTS = 2;

  private static List<MetaParameter> parameters() {
    final MetaParameter object = new MetaParameter("object", TransportTypes.FUDGE_MSG);
    final MetaParameter method = new MetaParameter("method", PrimitiveTypes.STRING);
    final MetaParameter arguments = new MetaParameter("arguments", ARGUMENTS_TYPE);
    return ImmutableList.of(object, method, arguments);
  }

  private InvokeObjectMethodFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.MISC, "InvokeObjectMethod", getParameters(), this));
  }

  protected InvokeObjectMethodFunction() {
    this(new DefinitionAnnotater(InvokeObjectMethodFunction.class));
  }

  public static Object invoke(final SessionContext sessionContext, final FudgeMsg fudgeMsg, final String methodName, final List<Data> data) {
    final FudgeContext fudgeContext = FudgeTypeConverter.getFudgeContext(sessionContext.getGlobalContext());
    final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    final Object object = deserializer.fudgeMsgToObject(fudgeMsg);
    return invoke(sessionContext, object, methodName, data);
  }

  public static Object invoke(final SessionContext sessionContext, final Object object, final String methodName, final List<Data> data) {
    Class clazz = object.getClass();
    List<Method> methods = functional(clazz.getMethods()).filter(new Function1<Method, Boolean>() {
      @Override
      public Boolean execute(Method method) {
        return method.getName().equals(methodName) && method.getParameterTypes().length == data.size();
      }
    }).asList();

    if(methods.isEmpty()){
      throw new InvokeInvalidArgumentException("No method named: "+methodName+" having "+data.size()+" numbers of arguments");
    }

    InvocationTargetException invocationTargetException = null;
    for (Method method : methods) {
      try {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Data[] dataArray = data.toArray(new Data[]{});
        Object[] objects = new Object[dataArray.length];
        for (int i = 0; i < dataArray.length; i++) {
          Data d = dataArray[i];
          objects[i] = propertyValue(sessionContext, parameterTypes[i].getClass(), d);
        }
        // we got data converted let's invoke the method with them
        method.setAccessible(true);
        return method.invoke(object, objects);
      } catch (InvalidConversionException e) {
        // we couldn't convert using type hins obtained from method's parameter types, let's move on
      } catch (InvocationTargetException e) {
        // the the underlying method has thrown an exception.
        invocationTargetException = e;
      } catch (IllegalAccessException e) {
        // we have method access problem, let's move on
      }
    }
    if(invocationTargetException != null){
      throw new InvokeInvalidArgumentException("The method named: "+methodName+" has thrown "+invocationTargetException.getTargetException()+" during its invokation");
    } else {
      throw new InvokeInvalidArgumentException("No method named: "+methodName+" and matching provided arguments could be found.");
    }

  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext,
                  (FudgeMsg) parameters[OBJECT],
                  (String) parameters[METHOD],
                  (List<Data>) parameters[ARGUMENTS]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
