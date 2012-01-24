##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Unpack the values from the surface as a data frame
.values.VolatilitySurfaceSnapshot <- function (msg) {
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

# Brings declarations for VolatilitySurfaceSnapshot into scope
Install.VolatilitySurfaceSnapshot <- function (stub) {
  stub.VolatilitySurfaceSnapshot <- stub$begin ("VolatilitySurfaceSnapshot")
  .object.FudgeMsg (stub.VolatilitySurfaceSnapshot)
  .field.object.FudgeMsg (stub.VolatilitySurfaceSnapshot, "values", ".values.VolatilitySurfaceSnapshot")
  stub.VolatilitySurfaceSnapshot$asDataFrame ("values.volatilitySurfaceSnapshot (x)")
  stub.VolatilitySurfaceSnapshot$fromFudgeMsg ("fromFudgeMsg.VolatilitySurfaceSnapshot (msg)", "ManageableVolatilitySurfaceSnapshot")
  stub.VolatilitySurfaceSnapshot$end ()
}
