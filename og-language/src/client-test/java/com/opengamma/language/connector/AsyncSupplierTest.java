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

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.language.connector.AsyncSupplier.Listener;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link AsyncSupplier} class.
 */
@Test(groups = TestGroup.UNIT)
public class AsyncSupplierTest {

  @Test(expectedExceptions = {IllegalArgumentException.class })
  public void testInvalidPost() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post("foo", new OpenGammaRuntimeException("fail"));
  }

  public void testPostNullResult() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post(null, null);
    assertNull(instance.get());
  }

  public void testPostValidResult() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post("foo", null);
    assertEquals(instance.get(), "foo");
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testPostException() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    instance.post(null, new OpenGammaRuntimeException("Fail"));
    instance.get();
  }

  public void testListenersResult() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    final List<String> results = new ArrayList<String>();
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        results.add(instance);
        assertNull(error);
      }
    });
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        throw new OpenGammaRuntimeException("Bad listener");
      }
    });
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        results.add(instance);
        assertNull(error);
      }
    });
    assertTrue(results.isEmpty());
    instance.post("Foo", null);
    assertEquals(results, Arrays.asList("Foo", "Foo"));
    results.clear();
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        results.add(instance);
        assertNull(error);
      }
    });
    assertEquals(results, Arrays.asList("Foo"));
  }

  public void testListenersError() {
    final AsyncSupplier<String> instance = new AsyncSupplier<String>() {
    };
    final List<RuntimeException> results = new ArrayList<RuntimeException>();
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        assertNull(instance);
        results.add(error);
      }
    });
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        throw new OpenGammaRuntimeException("Bad listener");
      }
    });
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        assertNull(instance);
        results.add(error);
      }
    });
    assertTrue(results.isEmpty());
    final RuntimeException fail = new OpenGammaRuntimeException("Fail");
    instance.post(null, fail);
    assertEquals(results, Arrays.asList(fail, fail));
    results.clear();
    instance.get(new Listener<String>() {
      @Override
      public void value(String instance, RuntimeException error) {
        assertNull(instance);
        results.add(error);
      }
    });
    assertEquals(results, Arrays.asList(fail));
  }

  public void testFilterOk() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function1<Integer, String>() {
      @Override
      public String execute(final Integer value) {
        return value.toString();
      }
    });
    underlying.post(42, null);
    assertEquals(instance.get(), "42");
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testFilterError() {
    final AsyncSupplier<Integer> underlying = new AsyncSupplier<Integer>() {
    };
    final AsyncSupplier.Filter<Integer, String> instance = new AsyncSupplier.Filter<Integer, String>(underlying, new Function1<Integer, String>() {
      @Override
      public String execute(final Integer value) {
        throw new InternalError();
      }
    });
    underlying.post(null, new OpenGammaRuntimeException("Fail"));
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
