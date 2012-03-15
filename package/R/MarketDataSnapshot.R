##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Decodes a Fudge representation of a market data snapshot to a data frame
fromFudgeMsg.UnstructuredMarketDataSnapshot <- function (msg) {
  if (length (msg) == 0) {
    NULL
  } else {
    x <- field.FudgeMsg (msg, 1)
    if (!is.list (x)) x <- list (x)
    uniqueId <- sapply (x, function (y) { y$valueSpec$uniqueId })
    valueName <- sapply (x, function (y) { y$valueName })
    value <- lapply (x, function (y) { y$value })
    marketValue <- sapply (value, function (y) { v <- y$marketValue; if (length (v) == 0) NA else v })
    overrideValue <- sapply (value, function (y) { v <- y$overrideValue; if (length (v) == 0) NA else v })
    data.frame (ValueName = valueName, Identifier = uniqueId, MarketValue = marketValue, OverrideValue = overrideValue)
  }
}

# Unpack the globalValues field from a snapshot
globalValues.MarketDataSnapshot <- function (msg) {
  if (length (msg) == 0) {
    list ()
  } else {
    fromFudgeMsg.UnstructuredMarketDataSnapshot (msg)
  }
}

# Unpack the yieldCurves field from a snapshot
yieldCurves.MarketDataSnapshot <- function (msg) {
  if (length (msg) == 0) {
    list ()
  } else {
    x <- field.FudgeMsg (msg, 2)
    if (!is.list (x)) x <- list (x)
    curveSnapshots <- lapply (x, function (y) { values.YieldCurveSnapshot (y$values) })
    x <- field.FudgeMsg (msg, 1)
    if (!is.list (x)) x <- list (x)
    names (curveSnapshots) <- sapply (x, function (y) { paste (y$currency, y$name, sep = "_") })
    curveSnapshots
  }
}

# Unpack the volatilityCubes field from a snapshot
volatilityCubes.MarketDataSnapshot <- function (msg) {
  if (length (msg) == 0) {
    list ()
  } else {
    x <- field.FudgeMsg (msg, 2)
    if (!is.list (x)) x <- list (x)
    cubeSnapshots <- lapply (x, function (y) { dataFrames.VolatilityCubeSnapshot (y) })
    x <- field.FudgeMsg (msg, 1)
    if (!is.list (x)) x <- list (x)
    names (cubeSnapshots) <- sapply (x, function (y) { paste (y$currency, y$name, sep = "_") })
    cubeSnapshots
  }
}

# Unpack the volatilitySurfaces field from a snapshot
volatilitySurfaces.MarketDataSnapshot <- function (msg) {
  if (length (msg) == 0) {
    list ()
  } else {
    x <- field.FudgeMsg (msg, 2)
    if (!is.list (x)) x <- list (x)
    surfaceSnapshots <- lapply (x, function (y) { values.VolatilitySurfaceSnapshot (y$values) })
    x <- field.FudgeMsg (msg, 1)
    if (!is.list (x)) x <- list (x)
    names (surfaceSnapshots) <- make.names (sapply (x, function (y) { paste (y$target, y$name, y$quoteType, y$quoteUnits, y$instrumentType, sep = "_") }))
    surfaceSnapshots
  }
}

# Brings declarations for MarketDataSnapshot into scope
Install.MarketDataSnapshot <- function (stub) {
  stub.MarketDataSnapshot <- stub$begin ("MarketDataSnapshot")
  .object.FudgeMsg (stub.MarketDataSnapshot)
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "uniqueId")
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "name")
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "basisViewName")
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "globalValues", "globalValues.MarketDataSnapshot")
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "yieldCurves", "yieldCurves.MarketDataSnapshot")
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "volatilityCubes", "volatilityCubes.MarketDataSnapshot")
  .field.object.FudgeMsg (stub.MarketDataSnapshot, "volatilitySurfaces", "volatilitySurfaces.MarketDataSnapshot")
  stub.MarketDataSnapshot$fromFudgeMsg ("fromFudgeMsg.MarketDataSnapshot (msg)", "ManageableMarketDataSnapshot")
  stub.MarketDataSnapshot$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.UnstructuredMarketDataSnapshot (msg)", "UnstructuredMarketDataSnapshot")
  stub.MarketDataSnapshot$end ()
}
