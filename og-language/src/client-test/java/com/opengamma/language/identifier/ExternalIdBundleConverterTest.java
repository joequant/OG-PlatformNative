/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ExternalIdBundleConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class ExternalIdBundleConverterTest extends AbstractConverterTest {

  private final ExternalIdBundleConverter _converter = new ExternalIdBundleConverter();

  public void testToBundle() {
    final JavaTypeInfo<ExternalIdBundle> target = OpenGammaTypes.EXTERNAL_ID_BUNDLE;
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, new ExternalId[] {ExternalId.of("Test", "1"), ExternalId.of("Test", "2") }, target,
        ExternalIdBundle.of(ExternalId.of("Test", "1"), ExternalId.of("Test", "2")));
    assertInvalidConversion(_converter, ExternalId.of("Test", "1"), target);
    assertConversionCount(1, _converter, target);
  }

  public void testFromBundle() {
    final JavaTypeInfo<ExternalId[]> target = JavaTypeInfo.builder(ExternalId[].class).get();
    assertEquals(_converter.canConvertTo(target), false);
  }

}
