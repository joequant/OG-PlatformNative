/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.tuple.Pair;

/**
 * Returns the portfolio requirements defined in a view definition
 */
public class GetViewPortfolioRequirementsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetViewPortfolioRequirementsFunction INSTANCE = new GetViewPortfolioRequirementsFunction();

  private final MetaFunction _meta;

  private static final int VIEW_DEFINITION_ID = 0;
  private static final int CALC_CONFIG_NAME = 1;

  // TODO: this should take a ViewDefinition object and we use type conversion to go from a unique id to the object
  // TODO: that approach would then cope if we also had a type conversion to get from a view-client-handle to the view-definition

  private static List<MetaParameter> parameters() {
    final MetaParameter viewDefinitionId = new MetaParameter("viewDefinition", OpenGammaTypes.UNIQUE_ID);
    final MetaParameter calcConfigName = new MetaParameter("calcConfig", PrimitiveTypes.STRING_ALLOW_NULL);
    return ImmutableList.of(viewDefinitionId, calcConfigName);
  }

  protected GetViewPortfolioRequirementsFunction() {
    this(new DefinitionAnnotater(GetViewPortfolioRequirementsFunction.class));
  }

  private GetViewPortfolioRequirementsFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "GetViewPortfolioRequirements", getParameters(), this));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  private void addRequirements(final String calcConfigName, final Set<Pair<String, ValueProperties>> requirements, final ArrayList<String> result) {
    result.ensureCapacity(result.size() + requirements.size());
    for (Pair<String, ValueProperties> requirement : requirements) {
      result.add(ValueRequirementUtils.generateRequirementName(calcConfigName, requirement.getFirst(), requirement.getSecond()));
    }
  }

  protected List<String> invoke(final ConfigSource configSource, final UniqueId viewDefinitionId, final String calcConfigName) {
    ViewDefinition viewDefinition = null;
    try {
      viewDefinition = configSource.getConfig(ViewDefinition.class, viewDefinitionId);
    } catch (RuntimeException e) {
      viewDefinition = null;
    }
    if (viewDefinition == null) {
      throw new InvokeInvalidArgumentException(VIEW_DEFINITION_ID, "View definition not found");
    }
    final ArrayList<String> result = new ArrayList<String>();
    if (calcConfigName != null) {
      final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(calcConfigName);
      if (calcConfig == null) {
        throw new InvokeInvalidArgumentException(CALC_CONFIG_NAME, "Calculation configuration not found");
      }
      addRequirements(calcConfig.getName(), calcConfig.getAllPortfolioRequirements(), result);
    } else {
      for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
        addRequirements(calcConfig.getName(), calcConfig.getAllPortfolioRequirements(), result);
      }
    }
    Collections.sort(result);
    return result;
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
    final ConfigSource configSource = sessionContext.getGlobalContext().getViewProcessor().getConfigSource();
    return invoke(configSource, (UniqueId) parameters[VIEW_DEFINITION_ID], (String) parameters[CALC_CONFIG_NAME]);
  }

}
