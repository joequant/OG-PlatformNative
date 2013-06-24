/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.rstats.data;

import com.opengamma.language.Data;
import com.opengamma.language.DataDecoration;
import com.opengamma.language.DataDecorator;
import com.opengamma.rstats.msg.DataInfo;

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

  private final DataInfo _info;

  private RDataInfo(final DataDecorator<? extends DataDecoration> decorator) {
    super(decorator);
    _info = new DataInfo();
  }

  private RDataInfo(final RDataInfo copyFrom) {
    super(copyFrom.getDecorator());
    _info = copyFrom.getInfo().clone();
  }

  private DataInfo getInfo() {
    return _info;
  }

  @Override
  public RDataInfo clone() {
    return new RDataInfo(this);
  }

  public static DataInfo getFor(final Data data) {
    final RDataInfo instance = s_decorator.get(data);
    if (instance != null) {
      return instance.getInfo();
    } else {
      return null;
    }
  }

  public static RDataInfo create() {
    return s_decorator.create();
  }

  public RDataInfo wrapperClass(final String wrapperClass) {
    getInfo().setWrapperClass(wrapperClass);
    return this;
  }

  @Override
  public String toString() {
    return getInfo().toString();
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
    return getInfo().equals(other.getInfo());
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + getInfo().hashCode();
    return hc;
  }

}
