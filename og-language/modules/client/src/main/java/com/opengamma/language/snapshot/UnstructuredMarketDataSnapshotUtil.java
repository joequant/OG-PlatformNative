/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import org.fudgemsg.FudgeMsg;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.id.ExternalId;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;

/* package */final class UnstructuredMarketDataSnapshotUtil {

  private UnstructuredMarketDataSnapshotUtil() {
  }

  public static void setValue(final ManageableUnstructuredMarketDataSnapshot snapshot, final String valueName, final ExternalId identifier, final Double overrideValue, final Double marketValue) {
    if (marketValue != null) {
      snapshot.putValue(identifier, valueName, ValueSnapshot.of(marketValue, overrideValue));
    } else if (overrideValue != null) {
      final ValueSnapshot value = snapshot.getValue(identifier, valueName);
      if (value != null) {
        value.setOverrideValue(overrideValue);
      } else {
        snapshot.putValue(identifier, valueName, ValueSnapshot.of(marketValue, overrideValue));
      }
    } else {
      snapshot.removeValue(identifier, valueName);
    }
  }

  /**
   * Temporary code until PLAT-4046 is resolved. Should return value directly and rely on registered type conversion chains.
   */
  @Deprecated
  public static Value toValue(final Object objectValue) {
    if (objectValue instanceof Boolean) {
      return ValueUtils.of((Boolean) objectValue);
    }
    if (objectValue instanceof Integer) {
      return ValueUtils.of((Integer) objectValue);
    }
    if (objectValue instanceof FudgeMsg) {
      return ValueUtils.of((FudgeMsg) objectValue);
    }
    if (objectValue instanceof Double) {
      return ValueUtils.of((Double) objectValue);
    }
    if (objectValue instanceof Number) {
      return ValueUtils.of(((Number) objectValue).doubleValue());
    }
    return ValueUtils.of(objectValue.toString());
  }

  /**
   * Temporary code until PLAT-3036 is resolved.
   */
  @Deprecated
  public static Double getOverrideValue(final ValueSnapshot value) {
    if (value.getOverrideValue() == null) {
      return null;
    }
    if (value.getOverrideValue() instanceof Double) {
      return (Double) value.getOverrideValue();
    }
    throw new IllegalArgumentException("PLAT-4046");
  }

  /**
   * Temporary code until PLAT-3036 is resolved.
   */
  @Deprecated
  public static Double getMarketValue(final ValueSnapshot value) {
    if (value.getMarketValue() == null) {
      return null;
    }
    if (value.getMarketValue() instanceof Double) {
      return (Double) value.getMarketValue();
    }
    throw new IllegalArgumentException("PLAT-4046");
  }

}
