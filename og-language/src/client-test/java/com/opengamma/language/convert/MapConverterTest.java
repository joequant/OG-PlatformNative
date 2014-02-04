/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.types.CollectionTypes;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MapConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class MapConverterTest extends AbstractConverterTest {

  private final MapConverter _converter = new MapConverter();

  private Value[][] createValues() {
    return new Value[][] { {ValueUtils.of("Foo"), ValueUtils.of("42") }, {ValueUtils.of("Bar"), ValueUtils.of(42) }, {ValueUtils.of("Cow"), new Value() } };
  }

  public void testToValues() {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("Foo", "42");
    map.put("Bar", 42);
    map.put("Cow", null);
    assertValidConversion(_converter, map, JavaTypeInfo.builder(Value[][].class).get(), createValues());
  }

  public void testToUntypedMap() {
    final Map<Object, Object> map = new HashMap<Object, Object>();
    // Untyped conversion will leave us with Value instances
    map.put(ValueUtils.of("Foo"), ValueUtils.of("42"));
    map.put(ValueUtils.of("Bar"), ValueUtils.of(42));
    map.put(ValueUtils.of("Cow"), new Value());
    assertValidConversion(_converter, createValues(), CollectionTypes.MAP, map);
  }

  public void testToTypedMap1() {
    final Map<String, Object> map = new HashMap<String, Object>();
    // Typed conversion will give us String keys, but the Value values (as they match "Object")
    map.put("Foo", ValueUtils.of("42"));
    map.put("Bar", ValueUtils.of(42));
    map.put("Cow", new Value());
    assertValidConversion(_converter, createValues(), JavaTypeInfo.builder(Map.class).parameter(String.class).parameter(Object.class).get(), map);
  }

  public void testToTypedMap2() {
    final Map<String, Data> map = new HashMap<String, Data>();
    // Typed conversion will give us String keys and Data values (boxing the original values)
    map.put("Foo", DataUtils.of(ValueUtils.of("42")));
    map.put("Bar", DataUtils.of(ValueUtils.of(42)));
    map.put("Cow", DataUtils.of(new Value()));
    assertValidConversion(_converter, createValues(), JavaTypeInfo.builder(Map.class).parameter(String.class).parameter(Data.class).get(), map);
  }

}
