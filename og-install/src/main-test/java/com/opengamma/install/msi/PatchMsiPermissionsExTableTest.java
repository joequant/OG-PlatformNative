/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the PatchMsiPermissionsExTable class.
 */
@Test(groups = TestGroup.UNIT)
public class PatchMsiPermissionsExTableTest {

  public void testInheritSDDL() {
    assertEquals(PatchMsiPermissionsExTable.inherit("D:PAI(A;OICI;FA;;;SY)(A;OICI;FA;;;BA)(A;OICIIO;GA;;;CO)(A;CI;0x1200a9;;;BU)(A;OICI;0x1200a9;;;<[%COMPUTERNAME]\\OpenGammaSystem>)"),
        "D:AI(A;OICIID;FA;;;SY)(A;OICIID;FA;;;BA)(A;OICIIOID;GA;;;CO)(A;CIID;0x1200a9;;;BU)(A;OICIID;0x1200a9;;;<[%COMPUTERNAME]\\OpenGammaSystem>)");
    assertEquals(
        PatchMsiPermissionsExTable.inherit("D:AI(A;OICIID;FA;;;SY)(A;OICIID;FA;;;BA)(A;OICIIOID;GA;;;CO)(A;OICIID;0x1200a9;;;<[%COMPUTERNAME]\\OpenGammaSystem>)(A;OICI;0x1200a9;;;BU)"),
        "D:AI(A;OICIID;FA;;;SY)(A;OICIID;FA;;;BA)(A;OICIIOID;GA;;;CO)(A;OICIID;0x1200a9;;;<[%COMPUTERNAME]\\OpenGammaSystem>)(A;OICIID;0x1200a9;;;BU)");
  }

}
