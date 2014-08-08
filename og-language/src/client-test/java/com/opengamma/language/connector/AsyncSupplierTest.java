/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.function.Function;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AsyncSupplier} class.
 */
@Test(groups = TestGroup.UNIT)
public class AsyncSupplierTest {

  private static class ListenerImpl<T> implements AsyncSupplier.Listener<T> {

    @Override
    public void value(T instance) {
      throw new InternalError();
    }

    @Override
    public void exception(RuntimeException exception) {
      throw new InternalError();
    }

    @Override
    public void error(Error error) {
      throw new InternalError();
    }

  }

  private static class InlineExecutor implements Executor {
    @Override
    public void execute(final Runnable command) {
      command.run();
    }
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidPost1() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post("foo", new OpenGammaRuntimeException("fail"), null);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidPost2() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post("foo", null, new Error("fail"));
  }

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidPost3() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post(null, new OpenGammaRuntimeException("fail"), new Error("fail"));
  }

  public void testPostNullResult() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post(null, null, null);
    assertNull(instance.get());
  }

  public void testPostValidResult() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post("foo", null, null);
    assertEquals(instance.get(), "foo");
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testPostException1() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post(null, new OpenGammaRuntimeException("Fail"), null);
    instance.get();
  }

  @Test(expectedExceptions = {Error.class })
  public void testPostException2() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post(null, null, new Error("fail"));
    instance.get();
  }

  public void testListenersResult() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>(new InlineExecutor()) {
    };
    final List<String> results = new ArrayList<String>();
    instance.get(new ListenerImpl<String>() {
      @Override
      public void value(String instance) {
        results.add(instance);
      }
    });
    instance.get(new ListenerImpl<String>() {
      @Override
      public void value(String instance) {
        throw new OpenGammaRuntimeException("Bad listener");
      }
    });
    instance.get(new ListenerImpl<String>() {
      @Override
      public void value(String instance) {
        results.add(instance);
      }
    });
    assertTrue(results.isEmpty());
    instance.post("Foo", null, null);
    assertEquals(results, Arrays.asList("Foo", "Foo"));
    results.clear();
    instance.get(new ListenerImpl<String>() {
      @Override
      public void value(String instance) {
        results.add(instance);
      }
    });
    assertEquals(results, Arrays.asList("Foo"));
  }

  public void testListenersException() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>(new InlineExecutor()) {
    };
    final List<RuntimeException> results = new ArrayList<RuntimeException>();
    instance.get(new ListenerImpl<String>() {
      @Override
      public void exception(RuntimeException exception) {
        results.add(exception);
      }
    });
    instance.get(new ListenerImpl<String>() {
      @Override
      public void exception(RuntimeException exception) {
        throw new OpenGammaRuntimeException("Bad listener");
      }
    });
    instance.get(new ListenerImpl<String>() {
      @Override
      public void exception(RuntimeException exception) {
        results.add(exception);
      }
    });
    assertTrue(results.isEmpty());
    final RuntimeException fail = new OpenGammaRuntimeException("Fail");
    instance.post(null, fail, null);
    assertEquals(results, Arrays.asList(fail, fail));
    results.clear();
    instance.get(new ListenerImpl<String>() {
      @Override
      public void exception(RuntimeException exception) {
        results.add(exception);
      }
    });
    assertEquals(results, Arrays.asList(fail));
  }

  public void testListenersError() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>(new InlineExecutor()) {
    };
    final List<Error> results = new ArrayList<Error>();
    instance.get(new ListenerImpl<String>() {
      @Override
      public void error(final Error error) {
        results.add(error);
      }
    });
    instance.get(new ListenerImpl<String>() {
      @Override
      public void error(Error error) {
        throw new OpenGammaRuntimeException("Bad listener");
      }
    });
    instance.get(new ListenerImpl<String>() {
      @Override
      public void error(final Error error) {
        results.add(error);
      }
    });
    assertTrue(results.isEmpty());
    final Error fail = new Error("Fail");
    instance.post(null, null, fail);
    assertEquals(results, Arrays.asList(fail, fail));
    results.clear();
    instance.get(new ListenerImpl<String>() {
      @Override
      public void error(Error error) {
        results.add(error);
      }
    });

    assertEquals(results, Arrays.asList(fail));
  }

  public void testFilterOk() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function<Integer, String>() {
      @Override
      public String apply(final Integer value) {
        return value.toString();
      }
    });
    underlying.post(42, null, null);
    assertEquals(instance.get(), "42");
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testFilterThrowsException() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function<Integer, String>() {
      @Override
      public String apply(final Integer value) {
        throw new OpenGammaRuntimeException("fail");
      }
    });
    underlying.post(42, null, null);
    instance.get();
  }

  @Test(expectedExceptions = {Error.class })
  public void testFilterThrowsError() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function<Integer, String>() {
      @Override
      public String apply(final Integer value) {
        throw new Error("fail");
      }
    });
    underlying.post(42, null, null);
    instance.get();
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testFilterException() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function<Integer, String>() {
      @Override
      public String apply(final Integer value) {
        throw new InternalError();
      }
    });
    underlying.post(null, new OpenGammaRuntimeException("Fail"), null);
    instance.get();
  }

  @Test(expectedExceptions = {Error.class })
  public void testFilterError() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function<Integer, String>() {
      @Override
      public String apply(final Integer value) {
        throw new InternalError();
      }
    });
    underlying.post(null, null, new Error("Fail"));
    instance.get();
  }

  public void testSpawnOk() {
    final AsyncSupplier.Spawned<String> instance = new AsyncSupplier.Spawned<String>() {
      @Override
      protected String getImpl() {
        return "42";
      }
    };
    (new Thread() {
      @Override
      public void run() {
        instance.start();
      }
    }).start();
    assertEquals(instance.get(), "42");
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testSpawnedError() {
    final AsyncSupplier.Spawned<String> instance = new AsyncSupplier.Spawned<String>() {
      @Override
      protected String getImpl() {
        throw new OpenGammaRuntimeException("Fail");
      }
    };
    (new Thread() {
      @Override
      public void run() {
        instance.start();
      }
    }).start();
    instance.get();
  }

}
