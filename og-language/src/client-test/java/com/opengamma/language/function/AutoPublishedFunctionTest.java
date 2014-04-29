/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.UserContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.error.InvokeParameterConversionException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class AutoPublishedFunctionTest {

  private SessionContext createSessionContext() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setTypeConverters(new Converters());
    testUtils.setTypeConverterFudgeContext(FudgeContext.GLOBAL_DEFAULT);
    return testUtils.createSessionContext();
  }

  public static class A {

    public static final PublishedFunction INSTANCE = new AutoPublishedFunction("Test", "A", A.class);

    public A() {
      fail();
    }

    public static String execute(String a, int b) {
      assertEquals(a, "Foo");
      assertEquals(b, 42);
      return "Bar";
    }

  }

  public void testA() throws AsynchronousExecution {
    final MetaFunction meta = A.INSTANCE.getMetaFunction();
    assertEquals(meta.getName(), "A");
    assertEquals(meta.getDescription(), "A's description");
    assertEquals(meta.getCategory(), "Test");
    assertEquals(meta.getParameter().get(0).getName(), "param1");
    assertEquals(meta.getParameter().get(0).getDescription(), "A's first parameter");
    assertEquals(meta.getParameter().get(1).getName(), "param2");
    assertEquals(meta.getParameter().get(1).getDescription(), "A's second parameter");
    final Result result = meta.getInvoker().invoke(createSessionContext(), ImmutableList.of(DataUtils.of("Foo"), DataUtils.of(42)));
    assertEquals(result.getResult(), ImmutableList.of(DataUtils.of("Bar")));
  }

  public static class B {

    public static final PublishedFunction INSTANCE = new AutoPublishedFunction("Test", "B", B.class);

    public String execute(SessionContext context, String a, int b) {
      assertNotNull(context);
      assertEquals(a, "Foo");
      assertEquals(b, 42);
      return "Bar";
    }

  }

  public void testB() throws AsynchronousExecution {
    final MetaFunction meta = B.INSTANCE.getMetaFunction();
    assertEquals(meta.getName(), "B");
    assertEquals(meta.getDescription(), "B's description");
    assertEquals(meta.getCategory(), "Test");
    assertEquals(meta.getParameter().get(0).getName(), "a");
    assertEquals(meta.getParameter().get(0).getDescription(), "B's first parameter");
    assertEquals(meta.getParameter().get(1).getName(), "b");
    assertEquals(meta.getParameter().get(1).getDescription(), "B's second parameter");
    final Result result = meta.getInvoker().invoke(createSessionContext(), ImmutableList.of(DataUtils.of("Foo"), DataUtils.of(42)));
    assertEquals(result.getResult(), ImmutableList.of(DataUtils.of("Bar")));
  }

  public static class C {

    public static final PublishedFunction INSTANCE = new AutoPublishedFunction("Test", "C", new C(42));

    private final int _x;

    public C(int x) {
      _x = x;
    }

    public String execute(UserContext context, @AutoPublishedFunction.Param(name = "x", allowNull = false) Integer x, int y,
        @AutoPublishedFunction.Param(name = "z", allowNull = true) Integer z) {
      assertNotNull(context);
      assertEquals((int) x, _x);
      return "Bar";
    }

  }

  public void testC() throws AsynchronousExecution {
    final MetaFunction meta = C.INSTANCE.getMetaFunction();
    assertEquals(meta.getName(), "ExampleC");
    assertEquals(meta.getDescription(), "C's description");
    assertEquals(meta.getCategory(), "Test");
    assertEquals(meta.getParameter().get(0).getName(), "x");
    assertEquals(meta.getParameter().get(0).getDescription(), "C's first parameter");
    assertEquals(meta.getParameter().get(1).getName(), "param2");
    assertEquals(meta.getParameter().get(1).getDescription(), "C's second parameter");
    // Third "Data" parameter is a "Null" in its transport encoding - allowed
    final Result result = meta.getInvoker().invoke(createSessionContext(), ImmutableList.of(DataUtils.of(42), DataUtils.of(0), new Data()));
    assertEquals(result.getResult(), ImmutableList.of(DataUtils.of("Bar")));
    try {
      // First "Data" parameter is a "Null" in its transport encoding - not allowed
      meta.getInvoker().invoke(createSessionContext(), ImmutableList.of(new Data(), DataUtils.of(0), new Data()));
      fail();
    } catch (InvokeParameterConversionException e) {
      assertEquals((int) e.getParameterIndex(), 0);
      assertEquals(e.getMessage(), "Could not convert empty value to Integer");
    }
  }

  public static class D {

    public static final PublishedFunction INSTANCE = new AutoPublishedFunction("Test", "D", D.class);

    public String execute(GlobalContext context) {
      assertNotNull(context);
      return "Bar";
    }

  }

  public void testD() throws AsynchronousExecution {
    final MetaFunction meta = D.INSTANCE.getMetaFunction();
    assertEquals(meta.getName(), "D");
    assertEquals(meta.getDescription(), "D's description");
    assertEquals(meta.getCategory(), "Test");
    assertEquals(meta.getParameter().size(), 0);
    final Result result = meta.getInvoker().invoke(createSessionContext(), ImmutableList.<Data>of());
    assertEquals(result.getResult(), ImmutableList.of(DataUtils.of("Bar")));
  }

  public static class E {

    public static final PublishedFunction INSTANCE = new AutoPublishedFunction("Test", "E", E.class);

    public String execute(@AutoPublishedFunction.Param(name = "a", defaultValue = "42") int a, @AutoPublishedFunction.Param(name = "b", defaultValue = "True") boolean b,
        @AutoPublishedFunction.Param(name = "c", defaultValue = "Foo") String c) {
      try {
        assertEquals(a, 42);
      } catch (Throwable t) {
        throw AutoPublishedFunction.invalidArgumentException("a", t);
      }
      try {
        assertEquals(b, true);
      } catch (Throwable t) {
        throw AutoPublishedFunction.invalidArgumentException("b", "B is invalid");
      }
      try {
        assertEquals(c, "Foo");
      } catch (Throwable t) {
        throw AutoPublishedFunction.invalidArgumentException("c", "C is invalid", t);
      }
      return "Bar";
    }

  }

  public void testE() throws AsynchronousExecution {
    final Result result = E.INSTANCE.getMetaFunction().getInvoker().invoke(createSessionContext(), ImmutableList.of(new Data(), new Data(), new Data()));
    assertEquals(result.getResult(), ImmutableList.of(DataUtils.of("Bar")));
    try {
      E.INSTANCE.getMetaFunction().getInvoker().invoke(createSessionContext(), ImmutableList.of(DataUtils.of(0), new Data(), new Data()));
      fail();
    } catch (InvokeInvalidArgumentException e) {
      assertEquals((int) e.getParameterIndex(), 0);
    }
    try {
      E.INSTANCE.getMetaFunction().getInvoker().invoke(createSessionContext(), ImmutableList.of(new Data(), DataUtils.of(false), new Data()));
      fail();
    } catch (InvokeInvalidArgumentException e) {
      assertEquals((int) e.getParameterIndex(), 1);
    }
    try {
      E.INSTANCE.getMetaFunction().getInvoker().invoke(createSessionContext(), ImmutableList.of(new Data(), new Data(), DataUtils.of("Bar")));
      fail();
    } catch (InvokeInvalidArgumentException e) {
      assertEquals((int) e.getParameterIndex(), 2);
    }
  }

}
