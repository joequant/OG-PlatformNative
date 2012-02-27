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

# Brings BucketedGreekResultCollection definitions into scope
Install.BucketedGreekResultCollection <- function (stub) {
  stub.BucketedGreekResultCollection <- stub$begin ("BucketedGreekResultCollection")
  stub.BucketedGreekResultCollection$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.BucketedGreekResultCollection (msg)")
  stub.BucketedGreekResultCollection$end ()
}
