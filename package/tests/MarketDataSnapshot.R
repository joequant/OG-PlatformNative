##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the access to market data snapshots

source ("TestUtil.R")

# Lookup a market data snapshot
LOGDEBUG ("Snapshots")
snapshots <- Snapshots ()
LOGDEBUG (length (snapshots))
snapshotIdentifier <- snapshots[1,1]
LOGDEBUG ("Got snapshot", snapshots[1,2])

# Fetch the snapshot
LOGDEBUG ("FetchSnapshot")
snapshot <- FetchSnapshot (snapshotIdentifier)
LOGDEBUG (snapshot)
ASSERT (is.MarketDataSnapshot (snapshot))

# Verify the ID
LOGDEBUG ("uniqueId.MarketDataSnapshot")
uniqueId <- uniqueId.MarketDataSnapshot (snapshot)
LOGDEBUG (uniqueId)
ASSERT (uniqueId == snapshotIdentifier)

# Test the other methods
LOGDEBUG ("name.MarketDataSnapshot")
print (name.MarketDataSnapshot (snapshot))
LOGDEBUG ("globalValues.MarketDataSnapshot")
print (globalValues.MarketDataSnapshot (snapshot))
LOGDEBUG ("yieldCurves.MarketDataSnapshot")
print (yieldCurves.MarketDataSnapshot (snapshot))
LOGDEBUG ("volatilityCubes.MarketDataSnapshot")
print (volatilityCubes.MarketDataSnapshot (snapshot))
LOGDEBUG ("volatilitySurfaces.MarketDataSnapshot")
print (volatilitySurfaces.MarketDataSnapshot (snapshot))
