/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link JavaTypeInfo} class.
 */
@Test(groups = TestGroup.UNIT)
public class JavaTypeInfoTest {

  private static final Logger s_logger = LoggerFactory.getLogger(JavaTypeInfoTest.class);

  private JavaTypeInfo.Builder<?>[] createBuilders() {
    return new JavaTypeInfo.Builder<?>[] {JavaTypeInfo.builder(Boolean.TYPE), JavaTypeInfo.builder(Boolean.class), JavaTypeInfo.builder(Character.TYPE),
        JavaTypeInfo.builder(Character.class), JavaTypeInfo.builder(Double.TYPE), JavaTypeInfo.builder(Double.class), JavaTypeInfo.builder(Float.TYPE), JavaTypeInfo.builder(Float.class),
        JavaTypeInfo.builder(Integer.TYPE), JavaTypeInfo.builder(Integer.class), JavaTypeInfo.builder(Long.TYPE), JavaTypeInfo.builder(Long.class), JavaTypeInfo.builder(Short.TYPE),
        JavaTypeInfo.builder(Short.class), JavaTypeInfo.builder(String.class), JavaTypeInfo.builder(Map.class),
        JavaTypeInfo.builder(Map.class).parameter(JavaTypeInfo.builder(String.class).get()).parameter(JavaTypeInfo.builder(Integer.class).get()),
        JavaTypeInfo.builder(Map.class).parameter(JavaTypeInfo.builder(Integer.class).get()).parameter(JavaTypeInfo.builder(String.class).get()) };
  }

  private JavaTypeInfo.Builder<?>[] arrayOf(final JavaTypeInfo.Builder<?>[] builders) {
    for (JavaTypeInfo.Builder<?> builder : builders) {
      if (builder != null) {
        builder.arrayOf();
      }
    }
    return builders;
  }

  private JavaTypeInfo.Builder<?>[] allowNull(final JavaTypeInfo.Builder<?>[] builders) {
    for (int i = 0; i < builders.length; i++) {
      if (builders[i] != null) {
        if (builders[i].get().getRawClass().isPrimitive()) {
          builders[i] = null;
        } else {
          builders[i].allowNull();
        }
      }
    }
    return builders;
  }

  private void addTypes(final List<JavaTypeInfo<?>> types, final JavaTypeInfo.Builder<?>[] builders) {
    for (JavaTypeInfo.Builder<?> builder : builders) {
      if (builder != null) {
        types.add(builder.get());
      }
    }
  }

  private List<JavaTypeInfo<?>> createTypes() {
    final List<JavaTypeInfo<?>> types = new ArrayList<JavaTypeInfo<?>>(18 * 4);
    addTypes(types, createBuilders());
    addTypes(types, arrayOf(createBuilders()));
    addTypes(types, allowNull(createBuilders()));
    addTypes(types, arrayOf(allowNull(createBuilders())));
    addTypes(types, arrayOf(arrayOf(createBuilders())));
    addTypes(types, arrayOf(arrayOf(allowNull(createBuilders()))));
    return types;
  }

  public void testHashCodeEquals() {
    final List<JavaTypeInfo<?>> types = createTypes();
    for (int i = 0; i < types.size(); i++) {
      for (int j = 0; j < types.size(); j++) {
        s_logger.debug("Comparing {} with {}", types.get(i), types.get(j));
        assertEquals(types.get(i).equals(types.get(j)), i == j);
        assertEquals(types.get(j).equals(types.get(i)), i == j);
        if (i == j) {
          assertTrue(types.get(i).hashCode() == types.get(j).hashCode());
        }
      }
    }
  }

  public void testStringParser() {
    for (JavaTypeInfo<?> type : createTypes()) {
      if (type.isAllowNull() || type.isDefaultValue()) {
        // NULLs and DEFAULTs not implemented
        continue;
      }
      final String str = type.toParseableString();
      s_logger.debug("Type {} -> {}", type, str);
      final JavaTypeInfo<?> parsed = JavaTypeInfo.parseString(str);
      s_logger.debug("String {} -> {}", str, parsed);
      assertEquals(parsed, type);
    }
  }

  public void testValidShorthandStrings() {
    assertEquals(JavaTypeInfo.parseString("String[]"), JavaTypeInfo.builder(String.class).arrayOf().get());
    assertEquals(JavaTypeInfo.parseString("java.util.List<SwapSecurity>"), JavaTypeInfo.builder(List.class).parameter(SwapSecurity.class).get());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidShorthandString() {
    JavaTypeInfo.parseString("Map");
  }

  @SuppressWarnings("unused")
  private static <S extends Comparable<S>, T extends Number> void testExtendedTypes_method(List<Integer> p1, Set<T> p2, Set<? extends T> p3, List<?> p4, List<String>[] p5, S[] p6) {
  }

  public void testExtendedTypes() throws Exception {
    final Type[] types = getClass().getDeclaredMethod("testExtendedTypes_method", List.class, Set.class, Set.class, List.class, List[].class, Comparable[].class).getGenericParameterTypes();
    assertEquals(JavaTypeInfo.ofType(types[0]).toString(), "java.util.List<java.lang.Integer{allow null}>{allow null}");
    assertEquals(JavaTypeInfo.ofType(types[1]).toString(), "java.util.Set<java.lang.Number{allow null}>{allow null}");
    assertEquals(JavaTypeInfo.ofType(types[2]).toString(), "java.util.Set<java.lang.Number{allow null}>{allow null}");
    assertEquals(JavaTypeInfo.ofType(types[3]).toString(), "java.util.List<java.lang.Object{allow null}>{allow null}");
    assertEquals(JavaTypeInfo.ofType(types[4]).toString(), "java.util.List<java.lang.String{allow null}>{allow null}[]{allow null}");
    assertEquals(JavaTypeInfo.ofType(types[5]).toString(), "java.lang.Comparable<java.lang.Object{allow null}>{allow null}[]{allow null}");
  }

}
