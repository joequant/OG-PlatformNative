##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Wraps a ManageableVolatilitySurfaceSnapshot instance to a VolatilitySurfaceSnapshot
fromFudgeMsg.ManageableVolatilitySurfaceSnapshot <- function (x) {
  fromFudgeMsg.VolatilitySurfaceSnapshot (x)
}

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
Install.VolatilitySurfaceSnapshot <- function () {
  object.FudgeMsg ("VolatilitySurfaceSnapshot")
  .field.VolatilitySurfaceSnapshot ("values", ".values.VolatilitySurfaceSnapshot")
}

# Default conversion to data frame
as.data.frame.VolatilitySurfaceSnapshot <- function (x) {
  values.VolatilitySurfaceSnapshot (x)
}
