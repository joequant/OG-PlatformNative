/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;

/**
 * Tests the {@link GetViewPortfolioRequirementsFunction} class.
 */
@Test
public class GetViewPortfolioRequirementsFunctionTest {

  private ViewDefinition createViewDefinition() {
    final ViewDefinition viewDef = new ViewDefinition("Test", "Test");
    viewDef.addPortfolioRequirement("A", "SECURITY", "Bar", ValueProperties.none());
    viewDef.addPortfolioRequirement("A", "SECURITY", "Foo", ValueProperties.with("Name", "X").get());
    viewDef.addPortfolioRequirement("B", "SECURITY", "Bar", ValueProperties.none());
    viewDef.addPortfolioRequirement("B", "SECURITY", "Foo", ValueProperties.with("Name", "Y").get());
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
  public void testAllConfigurations() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    final List<String> result = (List<String>) GetViewPortfolioRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {viewDefinition.getUniqueId(), null });
    assertEquals(result, ImmutableList.of("A/Bar", "A/Foo[Name=X]", "B/Bar", "B/Foo[Name=Y]"));
  }

  @SuppressWarnings("unchecked")
  public void testOneConfiguration() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    final List<String> result = (List<String>) GetViewPortfolioRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {viewDefinition.getUniqueId(), "A" });
    assertEquals(result, ImmutableList.of("A/Bar", "A/Foo[Name=X]"));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidViewDefinition() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    GetViewPortfolioRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {UniqueId.of("Not", "This"), null });
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidConfiguration() {
    final ViewDefinition viewDefinition = createViewDefinition();
    final SessionContext sessionContext = createSessionContext(viewDefinition);
    GetViewPortfolioRequirementsFunction.INSTANCE.invokeImpl(sessionContext, new Object[] {viewDefinition.getUniqueId(), "C" });
  }

}
