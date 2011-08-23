/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.data;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.language.Data;
import com.opengamma.language.DataDecoration;
import com.opengamma.language.DataDecorator;

/**
 * Additional metadata on {@link Data} values for use by the R wrappers.
 */
public final class RDataInfo extends DataDecoration implements Cloneable {

  private static final DataDecorator<RDataInfo> s_decorator = new DataDecorator<RDataInfo>() {
    @Override
    public RDataInfo create() {
      return new RDataInfo(this);
    }
  };

  private String _wrapperClass;

  private RDataInfo(final DataDecorator<? extends DataDecoration> decorator) {
    super(decorator);
  }

  private RDataInfo(final RDataInfo copyFrom) {
    super(copyFrom.getDecorator());
  }

  @Override
  public RDataInfo clone() {
    return new RDataInfo(this);
  }

  public static RDataInfo getFor(final Data data) {
    return s_decorator.get(data);
  }

  public static RDataInfo create() {
    return s_decorator.create();
  }

  public String getWrapperClass() {
    return _wrapperClass;
  }

  public void setWrapperClass(final String wrapperClass) {
    _wrapperClass = wrapperClass;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (getWrapperClass() != null) {
      sb.append("wrapperClass=").append(getWrapperClass());
    }
    return sb.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof RDataInfo)) {
      return false;
    }
    final RDataInfo other = (RDataInfo) o;
    return ObjectUtils.equals(getWrapperClass(), other.getWrapperClass());
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + ObjectUtils.hashCode(getWrapperClass());
    return hc;
  }

}
