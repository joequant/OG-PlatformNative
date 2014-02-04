/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.master.position.Deal;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to the core OpenGamma data model types.
 */
public final class CoreModelTypes {

  /**
   * Prevents instantiation.
   */
  private CoreModelTypes() {
  }

  public static final JavaTypeInfo<Counterparty> COUNTERPARTY_ALLOW_NULL = JavaTypeInfo.builder(Counterparty.class).get();

  public static final JavaTypeInfo<Deal> DEAL = JavaTypeInfo.builder(Deal.class).get();

  public static final JavaTypeInfo<ManageableCurveSnapshot> MANAGEABLE_CURVE_SNAPSHOT = JavaTypeInfo.builder(ManageableCurveSnapshot.class).get();

  public static final JavaTypeInfo<ManageableMarketDataSnapshot> MANAGEABLE_MARKET_DATA_SNAPSHOT = JavaTypeInfo.builder(ManageableMarketDataSnapshot.class).get();

  public static final JavaTypeInfo<ManageableSecurity> MANAGEABLE_SECURITY = JavaTypeInfo.builder(ManageableSecurity.class).get();

  public static final JavaTypeInfo<ManageableVolatilityCubeSnapshot> MANAGEABLE_VOLATILITY_CUBE_SNAPSHOT = JavaTypeInfo.builder(ManageableVolatilityCubeSnapshot.class).get();

  public static final JavaTypeInfo<ManageableVolatilitySurfaceSnapshot> MANAGEABLE_VOLATILITY_SURFACE_SNAPSHOT = JavaTypeInfo.builder(ManageableVolatilitySurfaceSnapshot.class).get();

  public static final JavaTypeInfo<ManageableYieldCurveSnapshot> MANAGEABLE_YIELD_CURVE_SNAPSHOT = JavaTypeInfo.builder(ManageableYieldCurveSnapshot.class).get();

  public static final JavaTypeInfo<Portfolio> PORTFOLIO = JavaTypeInfo.builder(Portfolio.class).get();

  public static final JavaTypeInfo<PortfolioNode> PORTFOLIO_NODE = JavaTypeInfo.builder(PortfolioNode.class).get();

  public static final JavaTypeInfo<Position> POSITION = JavaTypeInfo.builder(Position.class).get();

  public static final JavaTypeInfo<Security> SECURITY = JavaTypeInfo.builder(Security.class).get();

  public static final JavaTypeInfo<VolatilityCubeSnapshot> VOLATILITY_CUBE_SNAPSHOT_ALLOW_NULL = JavaTypeInfo.builder(VolatilityCubeSnapshot.class).allowNull().get();

  public static final JavaTypeInfo<VolatilitySurfaceSnapshot> VOLATILITY_SURFACE_SNAPSHOT_ALLOW_NULL = JavaTypeInfo.builder(VolatilitySurfaceSnapshot.class).allowNull().get();

  public static final JavaTypeInfo<YieldCurveSnapshot> YIELD_CURVE_SNAPSHOT_ALLOW_NULL = JavaTypeInfo.builder(YieldCurveSnapshot.class).allowNull().get();

}
