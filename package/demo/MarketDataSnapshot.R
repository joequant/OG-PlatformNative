##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates manipulating a market data snapshot programmatically.

# Shifts every value in the curve by an amount
shiftCurve <- function (curve, amount) {
  data <- values.YieldCurveSnapshot (curve)
  apply (data, 1, function (x) {
    v <- x["MarketValue"]
    if (!is.na (v)) {
      curve <<- SetYieldCurvePoint (curve, x["ValueName"], x["Identifier"], as.real (v) + amount)
    }
    invisible (0)
  })
  curve
}

# Shifts every point on the surface by an amount
shiftSurface <- function (surface, amount) {
  data <- values.VolatilitySurfaceSnapshot (surface)
  apply (data, 1, function (x) {
    v <- x["MarketValue"]
    if (!is.na (v)) {
      surface <<- SetVolatilitySurfacePoint (surface, x["X"], x["Y"], as.real (v) + amount)
    }
    invisible (0)
  })
  surface
}

# Find a snapshot base
snapshotName <- "test"
snapshotIdentifier <- Snapshots (snapshotName)[1,1]
snapshot <- FetchSnapshot (snapshotIdentifier)

# We'll manipulate it (in several stages) to create snapshot2
snapshot2 <- SetSnapshotName (snapshot, "R demonstration")

# Yield curve operations
for (yieldCurveToModify in GetSnapshotYieldCurve (snapshot2)) {
  curve <- GetSnapshotYieldCurve (snapshot2, yieldCurveToModify)
  curve <- shiftCurve (curve, 0.1)
  snapshot2 <- SetSnapshotYieldCurve (snapshot2, yieldCurveToModify, curve)
}

# Volatility surface operations
for (volatilitySurfaceToModify in GetSnapshotVolatilitySurface (snapshot2)) {
  surface <- GetSnapshotVolatilitySurface (snapshot2, volatilitySurfaceToModify)
  surface <- shiftSurface (surface, -0.1)
  snapshot2 <- SetSnapshotVolatilitySurface (snapshot2, volatilitySurfaceToModify, surface)
}

# Write the snapshot to the session database
newIdentifier <- StoreSnapshot (snapshot2)

print (paste ("Modified snapshot written as", newIdentifier))
