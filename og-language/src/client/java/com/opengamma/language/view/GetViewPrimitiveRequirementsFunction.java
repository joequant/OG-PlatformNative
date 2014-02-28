/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;

/**
 * Returns the primitive requirements defined in a view definition
 */
public class GetViewPrimitiveRequirementsFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final GetViewPrimitiveRequirementsFunction INSTANCE = new GetViewPrimitiveRequirementsFunction();

  private final MetaFunction _meta;

  private static final int VIEW_DEFINITION_ID = 0;
  private static final int CALC_CONFIG_NAME = 1;

  // TODO: this should take a ViewDefinition object and we use type conversion to go from a unique id to the object
  // TODO: that approach would then cope if we also had a type conversion to get from a view-client-handle to the view-definition

  private static List<MetaParameter> parameters() {
    final MetaParameter viewDefinitionId = new MetaParameter("viewDefinition", OpenGammaTypes.UNIQUE_ID);
    final MetaParameter calcConfigName = new MetaParameter("calcConfig", JavaTypeInfo.builder(String.class).defaultValue(ValueRequirementUtils.DEFAULT_CONFIG_NAME).get());
    return ImmutableList.of(viewDefinitionId, calcConfigName);
  }

  protected GetViewPrimitiveRequirementsFunction() {
    this(new DefinitionAnnotater(GetViewPrimitiveRequirementsFunction.class));
  }

  private GetViewPrimitiveRequirementsFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "GetViewPrimitiveRequirements", getParameters(), this));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  protected Set<ValueRequirement> invoke(final ConfigSource configSource, final UniqueId viewDefinitionId, final String calcConfigName) {
    ViewDefinition viewDefinition = null;
    try {
      viewDefinition = configSource.getConfig(ViewDefinition.class, viewDefinitionId);
    } catch (RuntimeException e) {
      viewDefinition = null;
    }
    if (viewDefinition == null) {
      throw new InvokeInvalidArgumentException(VIEW_DEFINITION_ID, "View definition not found");
    }
    final ViewCalculationConfiguration calcConfig = viewDefinition.getCalculationConfiguration(calcConfigName);
    if (calcConfig == null) {
      throw new InvokeInvalidArgumentException(CALC_CONFIG_NAME, "Calculation configuration not found");
    }
    return calcConfig.getSpecificRequirements();
  }

  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
    final ConfigSource configSource = sessionContext.getGlobalContext().getViewProcessor().getConfigSource();
    return invoke(configSource, (UniqueId) parameters[VIEW_DEFINITION_ID], (String) parameters[CALC_CONFIG_NAME]);
  }

}
