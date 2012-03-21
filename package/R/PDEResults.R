##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts a Fudge representation of a Bucketed Greek Result Collection into a list of bucketed greeks
fromFudgeMsg.BucketedGreekResultCollection <- function (msg) {
  expiries <- NULL
  result <- list ()
  for (field in fields.FudgeMsg (msg)) {
    fieldName <- field$Name
    if (length (fieldName) > 0) {
      fieldValue <- field$Value
      if (fieldName == "expiriesField") {
        expiries <- as.vector (fieldValue)
      } else {
        if (is.FudgeMsg (fieldValue)) {
          values <- c ()
          fields2 <- fields.FudgeMsg (fieldValue)
          for (field2 in fields2) {
            values <- c (values, field2$Value)
          }
          result[[substr (fieldName, 0, nchar (fieldName) - 5)]] <- matrix (values, ncol = length (fields2))
        }
      }
    }
  }
  lapply (result, function (x) {
    y <- data.frame (x)
    colnames (y) <- expiries
    y
  })
}

# Converts a Fudge representation of a ForexLocalVolatilityPDEPresentValueResultCollection into a data frame
fromFudgeMsg.ForexLocalVolatilityPDEPresentValueResultCollection <- function (msg) {
  strikes <- NULL
  values <- c ()
  values <- c ()
  for (field in fields.FudgeMsg (msg)) {
    fieldName <- field$Name
    if (length (fieldName) > 0) {
      fieldValue <- as.vector (field$Value)
      if (fieldName == "strikes") {
        strikes <- fieldValue
      } else {
        values <- c (values, fieldValue)
        names <- c (names, fieldName)
      }
    }
  }
  values <- matrix (values, length (strikes))
  values <- data.frame (values, row.names = strikes)
  colnames (values) <- names
  values
}

# Converts a Fudge representation of a PDE Result Collection into a data frame
fromFudgeMsg.PDEResultCollection <- function (msg) {
  strikes <- NULL
  values <- c ()
  names <- c ()
  for (field in fields.FudgeMsg (msg)) {
    fieldName <- field$Name
    if (length (fieldName) > 0) {
      fieldValue <- as.vector (field$Value)
      if (fieldName == "strikesField") {
        strikes <- fieldValue
      } else {
        values <- c (values, fieldValue)
        names <- c (names, substr (fieldName, 0, nchar (fieldName) - 5))
      }
    }
  }
  values <- matrix (values, length (strikes))
  values <- data.frame (values, row.names = strikes)
  colnames (values) <- names
  values
}

# Brings PDEResults definitions into scope
Install.PDEResults <- function (stub) {
  stub.PDEResults <- stub$begin ("PDEResults", Category.MISC)
  stub.PDEResults$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.BucketedGreekResultCollection (msg)", "BucketedGreekResultCollection")
  stub.PDEResults$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.ForexLocalVolatilityPDEPresentValueResultCollection (msg)", "ForexLocalVolatilityPDEPresentValueResultCollection")
  stub.PDEResults$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.PDEResultCollection (msg)", "PDEResultCollection")
  stub.PDEResults$end ()
}
