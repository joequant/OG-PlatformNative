/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Returns the latest result from a calculating view
 */
public class GetViewProcessIdFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(GetViewProcessIdFunction.class);

  /**
   * Default instance.
   */
  public static final GetViewProcessIdFunction INSTANCE = new GetViewProcessIdFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter viewClient = new MetaParameter("viewClient", ViewClientHandle.TYPE);
    return ImmutableList.of(viewClient);
  }

  private GetViewProcessIdFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "GetViewProcessId", getParameters(), this));
  }

  protected GetViewProcessIdFunction() {
    this(new DefinitionAnnotater(GetViewProcessIdFunction.class));
  }

  public static Object invoke(final ViewClientHandle viewClientHandle) throws AsynchronousExecution {
    final ViewClient viewClient = viewClientHandle.get().getViewClient();
    ViewProcess viewProcess = viewClient.getViewProcess();
    UniqueId viewProcessId;
    if (viewProcess != null) {
      viewProcessId = viewProcess.getUniqueId();
    } else {
      viewProcessId = null;
      s_logger.error("view process object returned from view client was null");
    }
    viewClientHandle.unlock();
    return viewProcessId;
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    final ViewClientHandle viewClientHandle = (ViewClientHandle) parameters[0];
    return invoke(viewClientHandle);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
