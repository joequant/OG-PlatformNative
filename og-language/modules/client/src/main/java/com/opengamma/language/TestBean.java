package com.opengamma.language;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
@BeanDefinition
public class TestBean implements Bean {

  @PropertyDefinition
  private String foo;

  @PropertyDefinition
  private Integer bar;

  //@PropertyDefinition
  //private Map<String, Integer> map;

  //@PropertyDefinition
  //private List<String> list;

  @PropertyDefinition
  private Set<String> set;

  @PropertyDefinition
  private TestBeanInner innerBean;


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TestBean}.
   * @return the meta-bean, not null
   */
  public static TestBean.Meta meta() {
    return TestBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TestBean.Meta.INSTANCE);
  }

  @Override
  public TestBean.Meta metaBean() {
    return TestBean.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the foo.
   * @return the value of the property
   */
  public String getFoo() {
    return foo;
  }

  /**
   * Sets the foo.
   * @param foo  the new value of the property
   */
  public void setFoo(String foo) {
    this.foo = foo;
  }

  /**
   * Gets the the {@code foo} property.
   * @return the property, not null
   */
  public final Property<String> foo() {
    return metaBean().foo().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bar.
   * @return the value of the property
   */
  public Integer getBar() {
    return bar;
  }

  /**
   * Sets the bar.
   * @param bar  the new value of the property
   */
  public void setBar(Integer bar) {
    this.bar = bar;
  }

  /**
   * Gets the the {@code bar} property.
   * @return the property, not null
   */
  public final Property<Integer> bar() {
    return metaBean().bar().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set.
   * @return the value of the property
   */
  public Set<String> getSet() {
    return set;
  }

  /**
   * Sets the set.
   * @param set  the new value of the property
   */
  public void setSet(Set<String> set) {
    this.set = set;
  }

  /**
   * Gets the the {@code set} property.
   * @return the property, not null
   */
  public final Property<Set<String>> set() {
    return metaBean().set().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the innerBean.
   * @return the value of the property
   */
  public TestBeanInner getInnerBean() {
    return innerBean;
  }

  /**
   * Sets the innerBean.
   * @param innerBean  the new value of the property
   */
  public void setInnerBean(TestBeanInner innerBean) {
    this.innerBean = innerBean;
  }

  /**
   * Gets the the {@code innerBean} property.
   * @return the property, not null
   */
  public final Property<TestBeanInner> innerBean() {
    return metaBean().innerBean().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public TestBean clone() {
    BeanBuilder<? extends TestBean> builder = metaBean().builder();
    for (MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
      if (mp.style().isBuildable()) {
        Object value = mp.get(this);
        if (value instanceof Bean) {
          value = ((Bean) value).clone();
        }
        builder.set(mp.name(), value);
      }
    }
    return builder.build();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TestBean other = (TestBean) obj;
      return JodaBeanUtils.equal(getFoo(), other.getFoo()) &&
          JodaBeanUtils.equal(getBar(), other.getBar()) &&
          JodaBeanUtils.equal(getSet(), other.getSet()) &&
          JodaBeanUtils.equal(getInnerBean(), other.getInnerBean());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getFoo());
    hash += hash * 31 + JodaBeanUtils.hashCode(getBar());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSet());
    hash += hash * 31 + JodaBeanUtils.hashCode(getInnerBean());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("TestBean{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("foo").append('=').append(JodaBeanUtils.toString(getFoo())).append(',').append(' ');
    buf.append("bar").append('=').append(JodaBeanUtils.toString(getBar())).append(',').append(' ');
    buf.append("set").append('=').append(JodaBeanUtils.toString(getSet())).append(',').append(' ');
    buf.append("innerBean").append('=').append(JodaBeanUtils.toString(getInnerBean())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TestBean}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code foo} property.
     */
    private final MetaProperty<String> _foo = DirectMetaProperty.ofReadWrite(
        this, "foo", TestBean.class, String.class);
    /**
     * The meta-property for the {@code bar} property.
     */
    private final MetaProperty<Integer> _bar = DirectMetaProperty.ofReadWrite(
        this, "bar", TestBean.class, Integer.class);
    /**
     * The meta-property for the {@code set} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<String>> _set = DirectMetaProperty.ofReadWrite(
        this, "set", TestBean.class, (Class) Set.class);
    /**
     * The meta-property for the {@code innerBean} property.
     */
    private final MetaProperty<TestBeanInner> _innerBean = DirectMetaProperty.ofReadWrite(
        this, "innerBean", TestBean.class, TestBeanInner.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "foo",
        "bar",
        "set",
        "innerBean");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 101574:  // foo
          return _foo;
        case 97299:  // bar
          return _bar;
        case 113762:  // set
          return _set;
        case -528499930:  // innerBean
          return _innerBean;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TestBean> builder() {
      return new DirectBeanBuilder<TestBean>(new TestBean());
    }

    @Override
    public Class<? extends TestBean> beanType() {
      return TestBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code foo} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> foo() {
      return _foo;
    }

    /**
     * The meta-property for the {@code bar} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> bar() {
      return _bar;
    }

    /**
     * The meta-property for the {@code set} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<String>> set() {
      return _set;
    }

    /**
     * The meta-property for the {@code innerBean} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<TestBeanInner> innerBean() {
      return _innerBean;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 101574:  // foo
          return ((TestBean) bean).getFoo();
        case 97299:  // bar
          return ((TestBean) bean).getBar();
        case 113762:  // set
          return ((TestBean) bean).getSet();
        case -528499930:  // innerBean
          return ((TestBean) bean).getInnerBean();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 101574:  // foo
          ((TestBean) bean).setFoo((String) newValue);
          return;
        case 97299:  // bar
          ((TestBean) bean).setBar((Integer) newValue);
          return;
        case 113762:  // set
          ((TestBean) bean).setSet((Set<String>) newValue);
          return;
        case -528499930:  // innerBean
          ((TestBean) bean).setInnerBean((TestBeanInner) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
