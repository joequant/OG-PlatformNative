/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.convention;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.convention.Convention;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * A function which fetches a convention from a convention source.
 */
public class FetchConventionFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(FetchConventionFunction.class);

  /**
   * Default instance.
   */
  public static final FetchConventionFunction INSTANCE = new FetchConventionFunction();

  private final MetaFunction _meta;

  private static final int IDENTIFIERS = 0;
  private static final int UNIQUE_ID = 1;

  private static List<MetaParameter> parameters() {
    final MetaParameter identifiers = new MetaParameter("identifiers", OpenGammaTypes.EXTERNAL_ID_BUNDLE_ALLOW_NULL);
    final MetaParameter uniqueIdentifier = new MetaParameter("uniqueId", OpenGammaTypes.UNIQUE_ID_ALLOW_NULL);
    return ImmutableList.of(identifiers, uniqueIdentifier);
  }

  private FetchConventionFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.CONVENTION, "FetchConvention", getParameters(), this));
  }

  protected FetchConventionFunction() {
    this(new DefinitionAnnotater(FetchConventionFunction.class));
  }

  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final ExternalIdBundle identifiers = (ExternalIdBundle) parameters[IDENTIFIERS];
    final UniqueId uniqueId = (UniqueId) parameters[UNIQUE_ID];
    if (identifiers == null) {
      if (uniqueId == null) {
        throw new InvokeInvalidArgumentException(UNIQUE_ID, "Unique identifier must be specified if identifier bundle is omitted");
      } else {
        Convention convention;
        try {
          convention = sessionContext.getGlobalContext().getConventionSource().get(uniqueId);
        } catch (Throwable e) {
          s_logger.debug("Caught exception", e);
          convention = null;
        }
        if (convention == null) {
          throw new InvokeInvalidArgumentException(UNIQUE_ID, "Unique identifier not found");
        } else {
          return convention;
        }
      }
    } else {
      if (uniqueId == null) {
        Convention convention;
        try {
          if (identifiers.size() == 1) {
            convention = sessionContext.getGlobalContext().getConventionSource().getSingle(identifiers.iterator().next());
          } else {
            convention = sessionContext.getGlobalContext().getConventionSource().getSingle(identifiers);
          }
        } catch (Throwable e) {
          s_logger.debug("Caught exception", e);
          convention = null;
        }
        if (convention == null) {
          throw new InvokeInvalidArgumentException(IDENTIFIERS, "Identifier(s) not found");
        } else {
          return convention;
        }
      } else {
        throw new InvokeInvalidArgumentException(UNIQUE_ID, "Unique identifier must be omitted if identifier bundle is specified");
      }
    }
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
