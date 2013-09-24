/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the {@link ValueRequirementUtils} class.
 */
@Test(groups = TestGroup.UNIT)
public class ValueRequirementUtilsTest {

  public void testParseRequirement() {
    assertEquals(ValueRequirementUtils.parseRequirement("Config/Value[A=B]"), Triple.of("Config", "Value", ValueProperties.with("A", "B").get()));
    assertEquals(ValueRequirementUtils.parseRequirement("Config/Val/ue[A=B]"), Triple.of("Config", "Val/ue", ValueProperties.with("A", "B").get()));
    assertEquals(ValueRequirementUtils.parseRequirement("Config/Val/ue[A=/B]"), Triple.of("Config", "Val/ue", ValueProperties.with("A", "/B").get()));
  }

  // TODO: More thorough testing

}
