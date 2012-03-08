##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the access to market data snapshots

source ("TestUtil.R")

# Create a snapshot
LOGDEBUG ("Create snapshot")
snapshot <- Snapshot ()
snapshot <- SetSnapshotName (snapshot, "R-Test Snapshot")

# Modify global values within the snapshot
LOGDEBUG ("Set global values")
snapshot <- SetSnapshotGlobalValue (snapshot, MarketDataRequirementNames.Market.Value, "OG_SYNTHETIC_TICKER~GBPSWAP1Y", marketValue = 0.5)
snapshot <- SetSnapshotGlobalValue (snapshot, MarketDataRequirementNames.Market.Value, "OG_SYNTHETIC_TICKER~USDSWAP1Y", marketValue = 0.5)

# Create yield curves within the snapshot
LOGDEBUG ("Set yield curves")
curve <- SnapshotYieldCurve ()
tickers <- list (
  "OG_SYNTHETIC_TICKER~GBPSWAP1Y" = 0.01,
  "OG_SYNTHETIC_TICKER~GBPSWAP2Y" = 0.02,
  "OG_SYNTHETIC_TICKER~GBPSWAP3Y" = 0.03,
  "OG_SYNTHETIC_TICKER~GBPSWAP4Y" = 0.04,
  "OG_SYNTHETIC_TICKER~GBPSWAP5Y" = 0.05)
for (ticker in names (tickers)) {
  curve <- SetYieldCurvePoint (curve, MarketDataRequirementNames.Market.Value, ticker, marketValue = tickers[[ticker]])
}
snapshot <- SetSnapshotYieldCurve (snapshot, "GBP_DEFAULT", curve)
tickers <- list (
  "OG_SYNTHETIC_TICKER~USDSWAP1Y" = 0.01,
  "OG_SYNTHETIC_TICKER~USDSWAP2Y" = 0.02,
  "OG_SYNTHETIC_TICKER~USDSWAP3Y" = 0.03,
  "OG_SYNTHETIC_TICKER~USDSWAP4Y" = 0.04,
  "OG_SYNTHETIC_TICKER~USDSWAP5Y" = 0.05)
for (ticker in names (tickers)) {
  curve <- SetYieldCurvePoint (curve, MarketDataRequirementNames.Market.Value, ticker, marketValue = tickers[[ticker]])
}
snapshot <- SetSnapshotYieldCurve (snapshot, "USD_DEFAULT", curve)

# Create volatility surfaces within the snapshot
LOGDEBUG ("Set volatility surfaces")
surface <- SnapshotVolatilitySurface ()
for (x in c ("P1Y", "P2Y", "P3Y")) {
  for (y in c ("0, ATM", "15, BUTTERFLY", "25, RISK_REVERSAL")) {
    surface <- SetVolatilitySurfacePoint (surface, x, y, marketValue = 0.005, xc = "TENOR", yc = "INTEGER_FXVOLQUOTETYPE_PAIR")
  }
}
snapshot <- SetSnapshotVolatilitySurface (snapshot, "Test~Test_DEFAULT_MarketStrangleRiskReversal_SWAPTION", surface)

# Create volatility cubes within the snapshot
LOGDEBUG ("Set volatility Cubes")
cube <- SnapshotVolatilityCube ()
for (swapTenor in c ("P1Y", "P2Y", "P3Y")) {
  for (optionExpiry in c ("P1Y", "P2Y", "P3Y")) {
    for (relativeStrike in c (-0.5, 0, 0.5)) {
      cube <- SetVolatilityCubePoint (cube, swapTenor, optionExpiry, relativeStrike, marketValue = 0.5)
    }
  }
}
snapshot <- SetSnapshotVolatilityCube (snapshot, "GBP_TEST", cube)

# Store the snapshot into the user database
LOGDEBUG ("Store snapshot")
snapshot.identifier <- StoreSnapshot (snapshot)
LOGINFO ("Snapshot written", snapshot.identifier)

# Retrieve the list of available snapshots
LOGDEBUG ("Query snapshots")
snapshots <- Snapshots ()
LOGINFO (length (snapshots), "snapshot(s) in database")

# Verify the one that we wrote is in that list
ASSERT (snapshot.identifier %in% snapshots[,1])

# Retrieve the snapshot by identifier
LOGDEBUG ("Fetch snapshot")
snapshot <- FetchSnapshot (snapshot.identifier)
ASSERT (is.MarketDataSnapshot (snapshot))

# Verify the contents
ASSERT_EQUAL (uniqueId.MarketDataSnapshot (snapshot), snapshot.identifier)
ASSERT_EQUAL (name.MarketDataSnapshot (snapshot), "R-Test Snapshot")
ASSERT_EQUAL (GetSnapshotName (snapshot), "R-Test Snapshot")
value <- GetSnapshotGlobalValue (snapshot, MarketDataRequirementNames.Market.Value, "OG_SYNTHETIC_TICKER~GBPSWAP1Y")
ASSERT_EQUAL (length (value), 2)
ASSERT (is.na (value[1]))
ASSERT_EQUAL (value[2], 0.5)
curve <- GetSnapshotYieldCurve (snapshot, "GBP_DEFAULT")
ASSERT (is.YieldCurveSnapshot (curve))
surface <- GetSnapshotVolatilitySurface (snapshot, "Test~Test_DEFAULT_MarketStrangleRiskReversal_SWAPTION")
ASSERT (is.VolatilitySurfaceSnapshot (surface))
cube <- GetSnapshotVolatilityCube (snapshot, "GBP_TEST")
ASSERT (is.VolatilityCubeSnapshot (cube))

# Run through the "pretty-printing" methods
LOGDEBUG ("globalValues.MarketDataSnapshot")
print (globalValues.MarketDataSnapshot (snapshot))
LOGDEBUG ("yieldCurves.MarketDataSnapshot")
print (yieldCurves.MarketDataSnapshot (snapshot))
LOGDEBUG ("volatilitySurfaces.MarketDataSnapshot")
print (volatilitySurfaces.MarketDataSnapshot (snapshot))
LOGDEBUG ("volatilityCubes.MarketDataSnapshot")
print (volatilityCubes.MarketDataSnapshot (snapshot))

