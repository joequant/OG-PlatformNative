/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FetchConfigItemFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class FetchConfigItemFunctionTest {

  private ViewDefinition getMockConfigObject(final int i) {
    return new ViewDefinition("Test view " + i, "me");
  }

  private ConfigSource createConfigSource() {
    final InMemoryConfigMaster master = new InMemoryConfigMaster();
    master.add(new ConfigDocument(ConfigItem.of(getMockConfigObject(1))));
    master.add(new ConfigDocument(ConfigItem.of(getMockConfigObject(2))));
    return new MasterConfigSource(master);
  }

  private SessionContext createSessionContext() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setConfigSource(createConfigSource());
    return testUtils.createSessionContext();
  }

  public void testByIdentifier() {
    final Object result = FetchConfigItemFunction.invoke(createSessionContext(), null, null, UniqueId.of("MemCfg", "1"));
    assertEquals(result, getMockConfigObject(1));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByIdentifierNotFound() {
    FetchConfigItemFunction.invoke(createSessionContext(), null, null, UniqueId.of("MemCfg", "0"));
  }

  public void testByIdentifierVerifyTypeAndName() {
    final Object result = FetchConfigItemFunction.invoke(createSessionContext(), ViewDefinition.class.getName(), "Test view 1", UniqueId.of("MemCfg", "1"));
    assertEquals(result, getMockConfigObject(1));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByIdentifierBadType() {
    FetchConfigItemFunction.invoke(createSessionContext(), YieldCurveDefinition.class.getName(), null, UniqueId.of("MemCfg", "1"));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByIdentifierBadName() {
    FetchConfigItemFunction.invoke(createSessionContext(), null, "Not the test view", UniqueId.of("MemCfg", "1"));
  }

  public void testByTypeAndName() {
    final Object result = FetchConfigItemFunction.invoke(createSessionContext(), ViewDefinition.class.getName(), "Test view 1", null);
    assertEquals(result, getMockConfigObject(1));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByTypeAndNameNotFound() {
    FetchConfigItemFunction.invoke(createSessionContext(), ViewDefinition.class.getName(), "Not the test view", null);
  }

  public void testByType() {
    final List<Object> result = (List<Object>) FetchConfigItemFunction.invoke(createSessionContext(), ViewDefinition.class.getName(), null, null);
    assertEquals(new HashSet<Object>(result), ImmutableSet.of(getMockConfigObject(1), getMockConfigObject(2)));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByTypeNotFound() {
    FetchConfigItemFunction.invoke(createSessionContext(), YieldCurveDefinition.class.getName(), null, null);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByInvalidType() {
    FetchConfigItemFunction.invoke(createSessionContext(), "com.opengamma.does.not.exist.Foo", null, null);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testNoParams() {
    FetchConfigItemFunction.invoke(createSessionContext(), null, null, null);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByName() {
    FetchConfigItemFunction.invoke(createSessionContext(), null, "Test view 1", null);
  }

}
