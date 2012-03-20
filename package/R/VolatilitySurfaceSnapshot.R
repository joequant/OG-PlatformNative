##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Unpack the values from the surface as a data frame
values.VolatilitySurfaceSnapshot <- function (msg) {
  k <- msg[1]
  if (!is.list (k)) k <- list (k)
  first <- sapply (k, function (z) { toString (toObject.FudgeMsg (z$first)) })
  second <- sapply (k, function (z) { toString (toObject.FudgeMsg (z$second)) })
  k <- msg[2]
  if (!is.list (k)) k <- list (k)
  marketValue <- sapply (k, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
  overrideValue <- sapply (k, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
  data.frame (X = first, Y = second, MarketValue = marketValue, OverrideValue = overrideValue)
}

# Construct a surface snapshot from data supplied in vectors
fromVectors.VolatilitySurfaceSnapshot <- function (xc, x, yc, y, marketValue, overrideValue) {
  surface <- SnapshotVolatilitySurface ()
  for (i in 1:length (marketValue)) {
    surface <- SetVolatilitySurfacePoint (
      snapshot = surface,
      x = x[((i - 1) %/% length (y)) + 1],
      y = y[((i - 1) %% length (y)) + 1],
      marketValue = marketValue[i],
      overrideValue = overrideValue[i],
      xc = xc,
      yc = yc)
  }
  surface
}

# Brings declarations for VolatilitySurfaceSnapshot into scope
Install.VolatilitySurfaceSnapshot <- function (stub) {
  stub.VolatilitySurfaceSnapshot <- stub$begin ("VolatilitySurfaceSnapshot", Category.MARKET_DATA)
  .object.FudgeMsg (stub.VolatilitySurfaceSnapshot)
  .field.object.FudgeMsg (stub.VolatilitySurfaceSnapshot, "values", "values.VolatilitySurfaceSnapshot")
  stub.VolatilitySurfaceSnapshot$asDataFrame ("values.volatilitySurfaceSnapshot (x)")
  stub.VolatilitySurfaceSnapshot$fromFudgeMsg ("fromFudgeMsg.VolatilitySurfaceSnapshot (msg)", "ManageableVolatilitySurfaceSnapshot")
  stub.VolatilitySurfaceSnapshot$func (
    "fromVectors",
    "Volatility surface constructor",
    "Creates a volatility surface from vectors containing the keys and values",
    list (xc = "The class/type of X key values (e.g. \"TENOR\")",
          x = "The X key values",
          yc = "The class/type of Y key values (e.g. \"INTEGER_FXVOLQUOTETYPE_PAIR\")",
          y = "The Y key values",
          marketValue = "The market data value points",
          overrideValue = "?The override value points (omit to not set)"),
    "OpenGamma:::fromVectors.VolatilitySurfaceSnapshot (as.character (xc), as.vector (x), as.character (yc), as.vector (y), as.vector (marketValue), as.vector (overrideValue))")
  stub.VolatilitySurfaceSnapshot$end ()
}
