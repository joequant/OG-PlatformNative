##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Wraps a ManageableMarketDataSnapshot instance to a MarketDataSnapshot
fromFudgeMsg.ManageableMarketDataSnapshot <- function (x) {
  fromFudgeMsg.MarketDataSnapshot (x)
}

# Decodes a Fudge representation of a market data snapshot to a data frame
fromFudgeMsg.UnstructuredMarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 1)
  if (!is.list (x)) x <- list (x)
  uniqueId <- sapply (x, function (y) { y$valueSpec$uniqueId })
  valueName <- sapply (x, function (y) { y$valueName })
  value <- lapply (x, function (y) { y$value })
  marketValue <- sapply (value, function (y) { v <- y$marketValue; if (length (v) == 0) NA else v })
  overrideValue <- sapply (value, function (y) { v <- y$overrideValue; if (length (v) == 0) NA else v })
  data.frame (ValueName = valueName, Identifier = uniqueId, MarketValue = marketValue, OverrideValue = overrideValue)
}

# Unpack the globalValues field from a snapshot
.globalValues.MarketDataSnapshot <- fromFudgeMsg.UnstructuredMarketDataSnapshot

# Unpack the yieldCurves field from a snapshot
.yieldCurves.MarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 2)
  if (!is.list (x)) x <- list (x)
  curveSnapshots <- lapply (x, function (y) { .values.YieldCurveSnapshot (y$values) })
  x <- field.FudgeMsg (msg, 1)
  if (!is.list (x)) x <- list (x)
  names (curveSnapshots) <- sapply (x, function (y) { paste (y$currency, y$name, sep = "_") })
  curveSnapshots
}

# Unpack the volatilityCubes field from a snapshot
.volatilityCubes.MarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 2)
  if (!is.list (x)) x <- list (x)
  cubeSnapshots <- lapply (x, function (y) { .dataFrames.VolatilityCubeSnapshot (y) })
  x <- field.FudgeMsg (msg, 1)
  if (!is.list (x)) x <- list (x)
  names (cubeSnapshots) <- sapply (x, function (y) { paste (y$currency, y$name, sep = "_") })
  cubeSnapshots
}

# Unpack the volatilitySurfaces field from a snapshot
.volatilitySurfaces.MarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 2)
  if (!is.list (x)) x <- list (x)
  surfaceSnapshots <- lapply (x, function (y) { .values.VolatilitySurfaceSnapshot (y$values) })
  x <- field.FudgeMsg (msg, 1)
  if (!is.list (x)) x <- list (x)
  names (surfaceSnapshots) <- make.names (sapply (x, function (y) { paste (y$target, y$name, y$instrumentType, sep = "_") }))
  surfaceSnapshots
}

# Brings declarations for MarketDataSnapshot into scope
Install.MarketDataSnapshot <- function () {
  object.FudgeMsg ("MarketDataSnapshot")
  .field.MarketDataSnapshot ("uniqueId")
  .field.MarketDataSnapshot ("name")
  .field.MarketDataSnapshot ("basisViewName")
  .field.MarketDataSnapshot ("globalValues", ".globalValues.MarketDataSnapshot")
  .field.MarketDataSnapshot ("yieldCurves", ".yieldCurves.MarketDataSnapshot")
  .field.MarketDataSnapshot ("volatilityCubes", ".volatilityCubes.MarketDataSnapshot")
  .field.MarketDataSnapshot ("volatilitySurfaces", ".volatilitySurfaces.MarketDataSnapshot")
}
