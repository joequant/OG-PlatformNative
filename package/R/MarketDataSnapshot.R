##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Wraps a ManageableMarketDataSnapshot instance to a MarketDataSnapshot
fromFudgeMsg.ManageableMarketDataSnapshot <- function (msg) {
  fromFudgeMsg.MarketDataSnapshot (msg)
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
.globalValues.MarketDataSnapshot <- function (msg) {
  fromFudgeMsg.UnstructuredMarketDataSnapshot (msg)
}

# Unpack the yieldCurves field from a snapshot
.yieldCurves.MarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 2)
  if (!is.list (x)) x <- list (x)
  curveSnapshots <- lapply (x, function (y) {
    fromFudgeMsg.UnstructuredMarketDataSnapshot (y$values)
  })
  x <- field.FudgeMsg (msg, 1)
  if (!is.list (x)) x <- list (x)
  names (curveSnapshots) <- sapply (x, function (y) { paste (y$currency, y$name, sep = "_") })
  curveSnapshots
}

# Unpack the volatilityCubes field from a snapshot
.volatilityCubes.MarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 2)
  if (!is.list (x)) x <- list (x)
  cubeSnapshots <- lapply (x, function (y) {
    values <- NULL
    otherValues <- NULL
    strikes <- NULL
    for (field in fields.FudgeMsg (y)) {
      fieldName <- field$Name
      if (length (fieldName) > 0) {
        if (fieldName == "values") {
          v <- field$Value
          k <- v[1]
          if (!is.list (k)) k <- list (k)
          swapTenor <- sapply (k, function (z) { z$swapTenor })
          optionExpiry <- sapply (k, function (z) { z$optionExpiry })
          relativeStrike <- sapply (k, function (z) { z$relativeStrike })
          v <- v[2]
          if (!is.list (v)) v <- list (v)
          marketValue <- sapply (v, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
          overrideValue <- sapply (v, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
          values <- data.frame (SwapTenor = swapTenor, OptionExpiry = optionExpiry, RelativeStrike = relativeStrike, MarketValue = marketValue, OverrideValue = overrideValue)
        } else {
          if (fieldName == "otherValues") {
            otherValues <- fromFudgeMsg.UnstructuredMarketDataSnapshot (field$Value)
          } else {
            if (fieldName == "strikes") {
              v <- field$Value
              k <- v[1]
              if (!is.list (k)) k <- list (k)
              first <- sapply (k, function (z) { z$first })
              second <- sapply (k, function (z) { z$second })
              v <- v[2]
              if (!is.list (v)) v <- list (v)
              marketValue <- sapply (v, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
              overrideValue <- sapply (v, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
              strikes <- data.frame (X = first, Y = second, MarketValue = marketValue, OverrideValue = overrideValue)
            }
          }
        }
      }
    }
    list (Values = values, OtherValues = otherValues, Strikes = strikes)
  })
  x <- field.FudgeMsg (msg, 1)
  if (!is.list (x)) x <- list (x)
  names (cubeSnapshots) <- sapply (x, function (y) { paste (y$currency, y$name, sep = "_") })
  cubeSnapshots
}

# Unpack the volatilitySurfaces field from a snapshot
.volatilitySurfaces.MarketDataSnapshot <- function (msg) {
  x <- field.FudgeMsg (msg, 2)
  if (!is.list (x)) x <- list (x)
  surfaceSnapshots <- lapply (x, function (y) {
    v <- y$values
    k <- v[1]
    if (!is.list (k)) k <- list (k)
    first <- sapply (k, function (z) { toString (toObject.FudgeMsg (z$first)) })
    second <- sapply (k, function (z) { toString (toObject.FudgeMsg (z$second)) })
    k <- v[2]
    if (!is.list (k)) k <- list (k)
    marketValue <- sapply (k, function (z) { v <- z$marketValue; if (length (v) == 0) NA else v })
    overrideValue <- sapply (k, function (z) { v <- z$overrideValue; if (length (v) == 0) NA else v })
    data.frame (X = first, Y = second, MarketValue = marketValue, OverrideValue = overrideValue)
  })
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
