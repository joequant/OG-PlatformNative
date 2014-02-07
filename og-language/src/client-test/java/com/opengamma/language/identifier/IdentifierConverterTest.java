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
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link IdentifierConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class IdentifierConverterTest extends AbstractConverterTest {

  private final IdentifierConverter _converter = new IdentifierConverter();

  public void testToBundle() {
    final JavaTypeInfo<ExternalIdBundle> target = OpenGammaTypes.EXTERNAL_ID_BUNDLE;
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, ExternalId.of("Test", "1"), target, ExternalIdBundle.of(ExternalId.of("Test", "1")));
    assertConversionCount(1, _converter, target);
  }

  public void testFromBundle() {
    final JavaTypeInfo<ExternalId> target = OpenGammaTypes.EXTERNAL_ID;
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, ExternalId.of("Test", "1").toBundle(), target, ExternalId.of("Test", "1"));
    assertConversionCount(1, _converter, target);
  }

  public void testToUniqueId() {
    final JavaTypeInfo<UniqueId> target = OpenGammaTypes.UNIQUE_ID;
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, ObjectId.of("Test", "1"), target, UniqueId.of("Test", "1"));
    assertConversionCount(1, _converter, target);
  }

  public void testToObjectId() {
    final JavaTypeInfo<ObjectId> target = OpenGammaTypes.OBJECT_ID;
    assertEquals(_converter.canConvertTo(target), true);
    assertValidConversion(_converter, UniqueId.of("Test", "1"), target, ObjectId.of("Test", "1"));
    assertConversionCount(1, _converter, target);
  }

}
