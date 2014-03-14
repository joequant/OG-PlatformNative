/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.util.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Holds as much information about a Java type as possible. This will drive parameter conversions to/from the wire format.
 * 
 * @param <T> the raw Java type
 */
public final class JavaTypeInfo<T> {

  private static final JavaTypeInfo<Object> OBJECT = builder(Object.class).get();

  private static final ClassResolver s_resolver = new ClassResolver();

  /**
   * Constructs {@link JavaTypeInfo} instances.
   */
  public static final class Builder<T> {

    private Class<T> _rawClass;
    private boolean _allowNull;
    private boolean _hasDefaultValue;
    private T _defaultValue;
    private List<JavaTypeInfo<?>> _parameter;

    private Builder(final Class<T> rawClass) {
      _rawClass = rawClass;
    }

    @SuppressWarnings("unchecked")
    public Builder<T[]> arrayOf() {
      if (_hasDefaultValue) {
        throw new IllegalStateException();
      }
      _rawClass = (Class<T>) Array.newInstance(_rawClass, 0).getClass();
      return (Builder<T[]>) this;
    }

    public Builder<T> allowNull() {
      if (_allowNull) {
        throw new IllegalStateException();
      }
      if (_rawClass.isPrimitive()) {
        // Can't have primitives allowing null
        return this;
      }
      _allowNull = true;
      return this;
    }

    public Builder<T> defaultValue(final T defaultValue) {
      if (_hasDefaultValue) {
        throw new IllegalStateException();
      }
      if (defaultValue == null) {
        _allowNull = true;
      } else {
        if (!_rawClass.isAssignableFrom(defaultValue.getClass())) {
          throw new IllegalArgumentException();
        }
      }
      _hasDefaultValue = true;
      _defaultValue = defaultValue;
      return this;
    }

    public Builder<T> parameter(final JavaTypeInfo<?> paramType) {
      if (_rawClass.isArray()) {
        throw new IllegalStateException();
      }
      if (_parameter == null) {
        _parameter = new LinkedList<JavaTypeInfo<?>>();
      }
      _parameter.add(paramType);
      return this;
    }

    public Builder<T> parameter(final Class<?> rawClass) {
      return parameter(builder(rawClass).get());
    }

    public JavaTypeInfo<T> get() {
      final JavaTypeInfo<?>[] parameter;
      if (_rawClass.isArray()) {
        final Builder<?> type = builder(_rawClass.getComponentType());
        if (_allowNull) {
          type.allowNull();
        }
        if (_parameter != null) {
          type._parameter = _parameter;
        }
        parameter = new JavaTypeInfo<?>[] {type.get() };
      } else {
        if (_parameter != null) {
          parameter = _parameter.toArray(new JavaTypeInfo<?>[_parameter.size()]);
        } else {
          parameter = null;
        }
      }
      return new JavaTypeInfo<T>(_rawClass, _allowNull, _hasDefaultValue, _defaultValue, parameter);
    }

  }

  private final Class<T> _rawClass;
  private final boolean _allowNull;
  private final boolean _hasDefaultValue;
  private final T _defaultValue;
  private final JavaTypeInfo<?>[] _parameter;

  private JavaTypeInfo(final Class<T> rawClass, final boolean allowNull, final boolean hasDefaultValue, final T defaultValue, final JavaTypeInfo<?>[] parameter) {
    _rawClass = rawClass;
    _allowNull = allowNull;
    _hasDefaultValue = hasDefaultValue;
    _defaultValue = defaultValue;
    _parameter = parameter;
  }

  public JavaTypeInfo<?> arrayOf() {
    return arrayOfWithAllowNull(_allowNull);
  }

  @SuppressWarnings("unchecked")
  public JavaTypeInfo<?> arrayOfWithAllowNull(final boolean allowNull) {
    return new JavaTypeInfo<Object>((Class<Object>) Array.newInstance(_rawClass, 0).getClass(), allowNull, false, null, new JavaTypeInfo<?>[] {this });
  }

  @SuppressWarnings("unchecked")
  public JavaTypeInfo<?> withAllowNull(final boolean allowNull) {
    if (allowNull == _allowNull) {
      return this;
    } else {
      return new JavaTypeInfo<Object>((Class<Object>) _rawClass, allowNull, _hasDefaultValue, _defaultValue, _parameter);
    }
  }

  @SuppressWarnings("unchecked")
  public JavaTypeInfo<?> withoutDefault() {
    if (_hasDefaultValue) {
      return new JavaTypeInfo<Object>((Class<Object>) _rawClass, _allowNull, false, null, _parameter);
    } else {
      return this;
    }
  }

  public Class<T> getRawClass() {
    return _rawClass;
  }

  public boolean isAllowNull() {
    return _allowNull;
  }

  public boolean isArray() {
    return _rawClass.isArray();
  }

  public int getArrayDimension() {
    int dimensions = 0;
    JavaTypeInfo<?> t = this;
    while (t.isArray()) {
      dimensions++;
      t = t._parameter[0];
    }
    return dimensions;
  }

  public JavaTypeInfo<?> getArrayElementType() {
    if (!isArray()) {
      throw new IllegalStateException();
    }
    return _parameter[0];
  }

  public boolean isDefaultValue() {
    return _hasDefaultValue;
  }

  public T getDefaultValue() {
    if (!isDefaultValue()) {
      throw new IllegalStateException();
    }
    return _defaultValue;
  }

  public JavaTypeInfo<?> getParameterizedType(final int index) {
    if ((_parameter != null) && (index < _parameter.length)) {
      return _parameter[index];
    } else {
      return OBJECT;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof JavaTypeInfo<?>)) {
      return false;
    }
    final JavaTypeInfo<?> other = (JavaTypeInfo<?>) o;
    if (getRawClass() != other.getRawClass()) {
      return false;
    }
    if (isAllowNull() != other.isAllowNull()) {
      return false;
    }
    if (isDefaultValue()) {
      if (other.isDefaultValue()) {
        if (!ObjectUtils.nullSafeEquals(getDefaultValue(), other.getDefaultValue())) {
          return false;
        }
      } else {
        return false;
      }
    } else {
      if (other.isDefaultValue()) {
        return false;
      }
    }
    if (_parameter == null) {
      if (other._parameter != null) {
        return false;
      }
    } else {
      if (other._parameter == null) {
        return false;
      }
      if (!Arrays.equals(_parameter, other._parameter)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hc = getRawClass().hashCode();
    hc += (hc << 4) + (isAllowNull() ? 1 : 0);
    if (isDefaultValue()) {
      hc += (hc << 4) + 1;
      hc += (hc << 4) + ObjectUtils.nullSafeHashCode(getDefaultValue());
    } else {
      hc += (hc << 4);
      hc += (hc << 4);
    }
    return hc;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (isArray()) {
      sb.append(getArrayElementType().toString());
      sb.append("[]");
    } else {
      sb.append(getRawClass().getName());
      if (_parameter != null) {
        sb.append('<');
        for (int i = 0; i < _parameter.length; i++) {
          if (i > 0) {
            sb.append(',');
          }
          sb.append(_parameter[i].toString());
        }
        sb.append('>');
      }
    }
    sb.append('{');
    if (isAllowNull()) {
      sb.append("allow null");
    } else {
      sb.append("not null");
    }
    if (isDefaultValue()) {
      sb.append(", default = ").append(getDefaultValue());
    }
    sb.append('}');
    return sb.toString();
  }

  /**
   * Return a simplified {@link #toString} that is suitable for the client as it may be shown to a user. The raw {@code toString} should contain sufficient extra information to be useful in a
   * diagnostic log but may not be particularly pretty.
   * 
   * @return the string
   */
  public String toClientString() {
    return getRawClass().getSimpleName();
  }

  /**
   * Return a string that can be processed by the "parse" method.
   * 
   * @return the string
   */
  public String toParseableString() {
    final StringBuilder sb = new StringBuilder();
    toParseableString(sb);
    return sb.toString();
  }

  private void toParseableString(final StringBuilder sb) {
    if (isArray()) {
      getArrayElementType().toParseableString(sb);
      sb.append("[]");
    } else {
      sb.append(getRawClass().getName());
      if (_parameter != null) {
        sb.append("<");
        for (int i = 0; i < _parameter.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          _parameter[i].toParseableString(sb);
        }
        sb.append(">");
      }
    }
    // TODO: include NULL constraints
    // TODO: include default
  }

  private static Pair<JavaTypeInfo<?>, String> checkArray(final Pair<JavaTypeInfo<?>, String> type) {
    if (type.getSecond().startsWith("[]")) {
      return checkArray(Pair.<JavaTypeInfo<?>, String>of(type.getFirst().arrayOf(), type.getSecond().substring(2)));
    } else {
      return type;
    }
  }

  /**
   * Resolves a class name as used in description strings.
   * 
   * @param className the name to resolve, not null
   * @return the resolved class, not null
   * @throws ClassNotFoundException if the class is not valid
   */
  public static Class<?> resolve(final String className) throws ClassNotFoundException {
    return s_resolver.resolve(className);
  }

  /**
   * Parses a string that describes the type and all enclosed meta data.
   * 
   * @param str string, not null
   * @return pair containing
   */
  private static Pair<JavaTypeInfo<?>, String> parseStringImpl(final String str) throws ClassNotFoundException {
    int i;
    for (i = 0; i < str.length(); i++) {
      switch (str.charAt(i)) {
        case '<': {
          final JavaTypeInfo.Builder<?> builder = JavaTypeInfo.builder(resolve(str.substring(0, i)));
          String remainder = str.substring(i + 1);
          do {
            final Pair<JavaTypeInfo<?>, String> param = parseStringImpl(remainder);
            if (param.getFirst() == null) {
              return param;
            }
            builder.parameter(param.getFirst());
            remainder = param.getSecond();
            if (remainder.length() == 0) {
              throw new IllegalArgumentException("Unexpected end of expression");
            }
            if (remainder.charAt(0) == ',') {
              remainder = remainder.substring(1);
              continue;
            }
            if (remainder.charAt(0) == '>') {
              return checkArray(Pair.<JavaTypeInfo<?>, String>of(builder.get(), remainder.substring(1)));
            } else {
              return Pair.of(null, remainder);
            }
          } while (true);
        }
        case ',':
        case '>': {
          return Pair.<JavaTypeInfo<?>, String>of(JavaTypeInfo.builder(resolve(str.substring(0, i))).get(), str.substring(i));
        }
        case '[': {
          return checkArray(Pair.<JavaTypeInfo<?>, String>of(JavaTypeInfo.builder(resolve(str.substring(0, i))).get(), str.substring(i)));
        }
      }
    }
    return Pair.<JavaTypeInfo<?>, String>of(JavaTypeInfo.builder(resolve(str)).get(), "");
  }

/**
   * Parses a string description of a {@code JavaTypeInfo} instance.
   * <pre>
   * JavaTypeInfo ::= ArrayJavaTypeInfo | ParameterizedJavaTypeInfo | SimpleJavaTypeInfo
   * 
   * ArrayJavaTypeInfo ::= JavaTypeInfo "[]"
   * 
   * ParameterizedJavaTypeInfo ::= JavaTypeInfo "<" JavaTypeInfo ParameterizedJavaTypeInfoTail
   * 
   * ParameterizedJavaTypeInfoTail ::= "," JavaTypeInfo ParameterizedJavaTypeInfoTail
   *                                 | ">"
   * 
   * SimpleJavaTypeInfo ::= ClassName
   * 
   * ClassName ::= FullyQualifiedClassName | PrimitiveType | BeanClassName
   * 
   * FullyQualifiedClassName ::=
   *   (for example "com.opengamma.example.Foo")
   *   
   * PrimitiveType ::= "boolean" | "char" | "double" | "float" | "int" | "long" | "short" | "String"
   * 
   * BeanClassName ::=
   *   (for example "SwapSecurity")
   * </pre>
   * 
   * @param str the string description to parse, not null
   * @return the type info, not null
   * @throws IllegalArgumentException if the description string is not valid
   */
  public static JavaTypeInfo<?> parseString(final String str) {
    try {
      final Pair<JavaTypeInfo<?>, String> parsed = parseStringImpl(str);
      if (parsed.getSecond().length() > 0) {
        throw new IllegalArgumentException("Invalid characters at end of expression - " + parsed.getSecond());
      }
      return parsed.getFirst();
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Couldn't parse type expression " + str, e);
    }
  }

  public static <T> Builder<T> builder(final Class<T> rawClass) {
    ArgumentChecker.notNull(rawClass, "rawClass");
    return new Builder<T>(rawClass);
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  private static Builder<?> ofTypeImpl(final Type type, final Set<String> visitedTypeVar) {
    if (type instanceof Class<?>) {
      return new Builder((Class<?>) type).allowNull();
    }
    if (type instanceof ParameterizedType) {
      final ParameterizedType ptype = (ParameterizedType) type;
      final Builder<?> builder = ofTypeImpl(ptype.getRawType(), visitedTypeVar);
      for (Type typeArg : ptype.getActualTypeArguments()) {
        builder.parameter(ofTypeImpl(typeArg, visitedTypeVar).get());
      }
      return builder;
    }
    if (type instanceof GenericArrayType) {
      return ofTypeImpl(((GenericArrayType) type).getGenericComponentType(), visitedTypeVar).arrayOf();
    }
    if (type instanceof TypeVariable) {
      final TypeVariable vtype = (TypeVariable) type;
      final Type[] bounds = vtype.getBounds();
      if (bounds.length != 1) {
        throw new IllegalArgumentException("Can't handle " + type);
      }
      if (visitedTypeVar == null) {
        final Set<String> typeVars = new HashSet<String>();
        typeVars.add(vtype.getName());
        return ofTypeImpl(bounds[0], typeVars);
      } else {
        final Builder<?> result;
        if (visitedTypeVar.add(vtype.getName())) {
          result = ofTypeImpl(bounds[0], visitedTypeVar);
          visitedTypeVar.remove(vtype.getName());
        } else {
          result = ofTypeImpl(Object.class, null);
        }
        return result;
      }
    }
    /*if (type instanceof VariantType) {
      // [PLAT-5880] Implement this; otherwise the WildcardType handling below will be used
      throw new UnsupportedOperationException("Can't support " + type + " yet");
    }*/
    if (type instanceof WildcardType) {
      final WildcardType wtype = (WildcardType) type;
      final Type[] bounds = wtype.getUpperBounds();
      if (bounds.length != 1) {
        throw new IllegalArgumentException("Can't handle " + type);
      }
      return ofTypeImpl(bounds[0], visitedTypeVar);
    }
    throw new IllegalArgumentException("Can't handle " + type);
  }

  public static JavaTypeInfo<?> ofType(final Type type) {
    return ofTypeImpl(type, null).get();
  }

}
