##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Decodes a Fudge representation of a market data snapshot to a data frame. This actually returns
# a list of data frames. The frames are labelled with the scheme from the identifiers that are in
# the identifier column of the frame. This it is easy to pick out the subset of identifiers you
# are familiar with (eg tickers from your preferred data provider) and ignore the rest.
fromFudgeMsg.UnstructuredMarketDataSnapshot <- function (msg) {
  if (length (msg) == 0) {
    NULL
  } else {
    xs <- field.FudgeMsg (msg, 1)
    if (!is.list (xs)) xs <- list (xs)
    result <- list ()
    for (x in xs) {
      identifiers <- list ()
      valueName <- ""
      marketValue <- NA
      overrideValue <- NA
      for (field in fields.FudgeMsg (x)) {
        fieldName <- field$Name
        if (fieldName == "identifiers") {
          for (field2 in fields.FudgeMsg (field$Value)) {
            if (field2$Name == "ID") {
              scheme <- ""
              value <- ""
              for (field3 in fields.FudgeMsg (field2$Value)) {
                fieldName <- field3$Name
                if (fieldName == "Scheme") {
                  scheme <- field3$Value
                } else {
                  if (fieldName == "Value") {
                    value <- field3$Value
                  }
                }
              }
              identifiers[[length (identifiers) + 1]] <- c (scheme, value)
            }
          }
        } else {
          if (fieldName == "valueName") {
            valueName <- field$Value
          } else {
            if (fieldName == "value") {
              for (field2 in fields.FudgeMsg (field$Value)) {
                fieldName <- field2$Name
                if (fieldName == "marketValue") {
                  marketValue <- field2$Value
                } else {
                  if (fieldName == "overrideValue") {
                    overrideValue <- field2$Value
                  }
                }
              }
            }
          }
        }
      }
      for (identifier in identifiers) {
        frame <- result[[identifier[1]]]
        if (is.null (frame)) {
          frame <- list ("ValueName" = c(), "Identifier" = c(), "MarketValue" = c(), "OverrideValue" = c())
        }
        frame$ValueName <- append (frame$ValueName, valueName)
        frame$Identifier <- append (frame$Identifier, identifier[2])
        frame$MarketValue <- append (frame$MarketValue, marketValue)
        frame$OverrideValue <- append (frame$OverrideValue, overrideValue)
        result[[identifier[1]]] <- frame
      }
    }
    lapply (result, function (x) { data.frame (ValueName = x$ValueName, Identifier = x$Identifier, MarketValue = x$MarketValue, OverrideValue = x$OverrideValue) })
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
    names (surfaceSnapshots) <- sapply (x, function (y) { paste (y$target, y$name, y$quoteType, y$quoteUnits, y$instrumentType, sep = "_") })
    surfaceSnapshots
  }
}

# Brings declarations for MarketDataSnapshot into scope
Install.MarketDataSnapshot <- function (stub) {
  stub.MarketDataSnapshot <- stub$begin ("MarketDataSnapshot", Category.MARKET_DATA)
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
