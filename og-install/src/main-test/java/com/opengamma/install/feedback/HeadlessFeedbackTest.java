/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.install.feedback;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link Feedback} class when the native method is not available (which is the case when running as TestNG).
 */
@Test(groups = TestGroup.UNIT)
public class HeadlessFeedbackTest {

  public void testReport() {
    final Feedback fb = new Feedback();
    assertEquals(fb.getState(), Feedback.STATE_UNKNOWN);
    fb.report("First message");
    assertEquals(fb.getState(), Feedback.STATE_BROKEN);
    fb.report("Second message");
  }

}
