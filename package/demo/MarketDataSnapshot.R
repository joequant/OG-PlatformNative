##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates manipulating a market data snapshot programmatically.

# Shifts every value in the curve by an amount by manipulating direct points
shiftCurveByPoint <- function (curve, amount) {
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

# Modifies the curve by manipulating a vector defining it (amount could be a scalar or a vector)
shiftCurveAsVector <- function (curve, amount) {
  tensor <- GetYieldCurveTensor (curve)
  SetYieldCurveTensor (curve, tensor * amount)
}

# Shifts every point on the surface by an amount by manipulating direct points
shiftSurfaceByPoint <- function (surface, amount) {
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

# Modifies the surface by manipulating a matrix defining it (amount could be a scalar, vector, or matrix)
shiftSurfaceAsVector <- function (surface, amount) {
  tensor <- GetVolatilitySurfaceTensor (surface)
  SetVolatilitySurfaceTensor (surface, tensor * amount)
}

# Find a snapshot base (don't specify a name to grab the first)
snapshotName <- NULL
snapshotIdentifier <- Snapshots (snapshotName)[1,1]
snapshot <- FetchSnapshot (snapshotIdentifier)

# We'll manipulate it (in several stages) to create snapshot2
snapshot2 <- SetSnapshotName (snapshot, "R demonstration")

# Yield curve operations
for (yieldCurveToModify in GetSnapshotYieldCurve (snapshot2)) {
  curve <- GetSnapshotYieldCurve (snapshot2, yieldCurveToModify)
  curve <- shiftCurveByPoint (curve, 0.1)
  curve <- shiftCurveAsVector (curve, 1.2)
  snapshot2 <- SetSnapshotYieldCurve (snapshot2, yieldCurveToModify, curve)
}

# Volatility surface operations
for (volatilitySurfaceToModify in GetSnapshotVolatilitySurface (snapshot2)) {
  surface <- GetSnapshotVolatilitySurface (snapshot2, volatilitySurfaceToModify)
  surface <- shiftSurfaceByPoint (surface, -0.1)
  surface <- shiftSurfaceAsVector (surface, 1.2)
  snapshot2 <- SetSnapshotVolatilitySurface (snapshot2, volatilitySurfaceToModify, surface)
}

# Write the snapshot to the session database
newIdentifier <- StoreSnapshot (snapshot2)

print (paste ("Modified snapshot written as", newIdentifier))

# Set a single point on a named surface
surfaceName <- "CurrencyISO~USD_DEFAULT_IR_FUTURE_OPTION"
surface <- GetSnapshotVolatilitySurface (snapshot, surfaceName)
surface <- SetVolatilitySurfacePoint (surface, "10", "98.75", 1000)
snapshot2 <- SetSnapshotVolatilitySurface (snapshot, surfaceName, surface)
