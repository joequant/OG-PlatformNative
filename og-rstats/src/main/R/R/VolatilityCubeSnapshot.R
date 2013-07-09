##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Unpack the values from the cube into data frames
dataFrames.VolatilityCubeSnapshot <- function (msg) {
  values <- NULL
  otherValues <- NULL
  strikes <- NULL
  for (field in fields.FudgeMsg (msg)) {
    fieldName <- field$Name
    if (length (fieldName) > 0) {
      if (fieldName == "values") {
        values <- values.VolatilityCubeSnapshot (field$Value)
      } else {
        if (fieldName == "otherValues") {
          otherValues <- otherValues.VolatilityCubeSnapshot (field$Value)
        } else {
          if (fieldName == "strikes") {
            strikes <- strikes.VolatilityCubeSnapshot (field$Value)
          }
        }
      }
    }
  }
  list (Values = values, OtherValues = otherValues, Strikes = strikes)
}

# Unpack the values from the cube into a data frame
otherValues.VolatilityCubeSnapshot <- function (v) {
  fromFudgeMsg.UnstructuredMarketDataSnapshot (v)
}

# Unpack the "other values" from the cube into a data frame
strikes.VolatilityCubeSnapshot <- function (v) {
  k <- v[1]
  if (!is.list (k)) k <- list (k)
  first <- sapply (k, function (z) { z$first })
  second <- sapply (k, function (z) { z$second })
  k <- v[2]
  if (!is.list (k)) k <- list (k)
  marketValue <- sapply (k, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
  overrideValue <- sapply (k, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
  data.frame (X = first, Y = second, MarketValue = marketValue, OverrideValue = overrideValue)
}

# Unpack the strikes from the cube into a data frame
values.VolatilityCubeSnapshot <- function (v) {
  k <- v[1]
  if (!is.list (k)) k <- list (k)
  swapTenor <- sapply (k, function (z) { z$swapTenor })
  optionExpiry <- sapply (k, function (z) { z$optionExpiry })
  relativeStrike <- sapply (k, function (z) { z$relativeStrike })
  k <- v[2]
  if (!is.list (k)) k <- list (k)
  marketValue <- sapply (k, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
  overrideValue <- sapply (k, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
  data.frame (SwapTenor = swapTenor, OptionExpiry = optionExpiry, RelativeStrike = relativeStrike, MarketValue = marketValue, OverrideValue = overrideValue)
}

# Brings declarations for VolatilityCubeSnapshot into scope
Install.VolatilityCubeSnapshot <- function (stub) {
  stub.VolatilityCubeSnapshot <- stub$begin ("VolatilityCubeSnapshot", Category.MARKET_DATA)
  .object.FudgeMsg (stub.VolatilityCubeSnapshot)
  .field.object.FudgeMsg (stub.VolatilityCubeSnapshot, "otherValues", "otherValues.VolatilityCubeSnapshot")
  .field.object.FudgeMsg (stub.VolatilityCubeSnapshot, "strikes", "strikes.VolatilityCubeSnapshot")
  .field.object.FudgeMsg (stub.VolatilityCubeSnapshot, "values", "values.VolatilityCubeSnapshot")
  stub.VolatilityCubeSnapshot$fromFudgeMsg ("fromFudgeMsg.VolatilityCubeSnapshot (msg)", "ManageableVolatilityCubeSnapshot")
  stub.VolatilityCubeSnapshot$end ()
}
