/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link GetViewPrimitiveRequirementsFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class GetViewPrimitiveRequirementsFunctionTest {

  private ViewDefinition createViewDefinition() {
    final ViewDefinition viewDef = new ViewDefinition("Test", "Test");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDef, "A");
    calcConfig.addSpecificRequirement(new ValueRequirement("Foo", ComputationTargetSpecification.NULL, ValueProperties.none()));
    viewDef.addViewCalculationConfiguration(calcConfig);
    calcConfig = new ViewCalculationConfiguration(viewDef, "B");
    calcConfig.addSpecificRequirement(new ValueRequirement("Bar", ComputationTargetSpecification.NULL, ValueProperties.none()));
    viewDef.addViewCalculationConfiguration(calcConfig);
    return viewDef;
  }

  private SessionContext createSessionContext(final ViewDefinition viewDef) {
    final InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    viewDef.setUniqueId(configMaster.add(new ConfigDocument(ConfigItem.of(viewDef))).getUniqueId());
    final ViewProcessor viewProcessor = Mockito.mock(ViewProcessor.class);
    Mockito.when(viewProcessor.getConfigSource()).thenReturn(new MasterConfigSource(configMaster));
    final TestUtils testUtils = new TestUtils();
    testUtils.setViewProcessor(viewProcessor);
    return testUtils.createSessionContext();
  }

  @SuppressWarnings("unchecked")
  public void testOneConfiguration() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    final Set<ValueRequirement> result = (Set<ValueRequirement>) GetViewPrimitiveRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {viewDefinition.getUniqueId(), "A" });
    assertEquals(result, ImmutableSet.of(new ValueRequirement("Foo", ComputationTargetSpecification.NULL, ValueProperties.none())));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidViewDefinition() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    GetViewPortfolioRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {UniqueId.of("Not", "This"), "A" });
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidConfiguration() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    GetViewPortfolioRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {viewDefinition.getUniqueId(), "C" });
  }

}
