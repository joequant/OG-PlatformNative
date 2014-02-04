/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to engine types.
 */
public final class EngineTypes {

  /**
   * Prevents instantiation.
   */
  private EngineTypes() {
  }

  public static final JavaTypeInfo<AvailableOutputs> AVAILABLE_OUTPUTS = JavaTypeInfo.builder(AvailableOutputs.class).get();

  public static final JavaTypeInfo<ComputationTargetType> COMPUTATION_TARGET_TYPE = JavaTypeInfo.builder(ComputationTargetType.class).get();

  public static final JavaTypeInfo<ComputationTargetType> COMPUTATION_TARGET_TYPE_ALLOW_NULL = JavaTypeInfo.builder(ComputationTargetType.class).allowNull().get();

  public static final JavaTypeInfo<MarketDataSpecification> MARKET_DATA_SPECIFICATION = JavaTypeInfo.builder(MarketDataSpecification.class).get();

  public static final JavaTypeInfo<ValueProperties> VALUE_PROPERTIES = JavaTypeInfo.builder(ValueProperties.class).get();

  public static final JavaTypeInfo<ValueRequirement> VALUE_REQUIREMENT_ALLOW_NULL = JavaTypeInfo.builder(ValueRequirement.class).allowNull().get();

  public static final JavaTypeInfo<ViewCycleExecutionSequence> VIEW_CYCLE_EXECUTION_SEQUENCE = JavaTypeInfo.builder(ViewCycleExecutionSequence.class).get();

  public static final JavaTypeInfo<ViewCalculationResultModel> VIEW_CALCULATION_RESULT_MODEL = JavaTypeInfo.builder(ViewCalculationResultModel.class).get();

  public static final JavaTypeInfo<ViewComputationResultModel> VIEW_COMPUTATION_RESULT_MODEL = JavaTypeInfo.builder(ViewComputationResultModel.class).get();

  public static final JavaTypeInfo<ViewDefinition> VIEW_DEFINITION = JavaTypeInfo.builder(ViewDefinition.class).get();

  public static final JavaTypeInfo<ViewExecutionFlags> VIEW_EXECUTION_FLAGS = JavaTypeInfo.builder(ViewExecutionFlags.class).get();

}
