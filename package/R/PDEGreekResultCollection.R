##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts a Fudge representation of a PDE Greek Result Collection into a data frame
fromFudgeMsg.PDEGreekResultCollection <- function (msg) {
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

# Brings PDEGreekResultCollection definitions into scope
Install.PDEGreekResultCollection <- function (stub) {
  stub.PDEGreekResultCollection <- stub$begin ("PDEGreekResultCollection")
  stub.PDEGreekResultCollection$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.PDEGreekResultCollection (msg)")
  stub.PDEGreekResultCollection$end ()
}
