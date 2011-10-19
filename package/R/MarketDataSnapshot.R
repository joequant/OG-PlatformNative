##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Wraps a ManageableMarketDataSnapshot instance to a MarketDataSnapshot
fromFudgeMsg.ManageableMarketDataSnapshot <- function (msg) {
  fromFudgeMsg.MarketDataSnapshot (msg)
}

# Unpack the globalValues field from a snapshot
.globalValues.MarketDataSnapshot <- function (msg) {
  # TODO
  msg
}

# Unpack the yieldCurves field from a snapshot
.yieldCurves.MarketDataSnapshot <- function (msg) {
  curveSnapshots <- lapply (field.FudgeMsg (msg, 2), function (x) {
    y <- field.FudgeMsg (x$values, 1)
    valueSpec <- lapply (y, function (z) { z$valueSpec })
    uniqueId <- sapply (valueSpec, function (z) { z$uniqueId })
    valueName <- sapply (y, function (z) { z$valueName })
    value <- lapply (y, function (z) { z$value })
    marketValue <- sapply (value, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
    overrideValue <- sapply (value, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
    data.frame (ValueName = valueName, Identifier = uniqueId, MarketValue = marketValue, OverrideValue = overrideValue)
  })
  names (curveSnapshots) <- sapply (field.FudgeMsg (msg, 1), function (x) { paste (x$currency, x$name, sep = "_") })
  curveSnapshots
}

# Unpack the volatilityCubes field from a snapshot
.volatilityCubes.MarketDataSnapshot <- function (msg) {
  # TODO
  msg
}

# Unpack the volatilitySurfaces field from a snapshot
.volatilitySurfaces.MarketDataSnapshot <- function (msg) {
  # TODO
  msg
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
