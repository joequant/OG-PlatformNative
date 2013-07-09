##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts a Fudge representation of a multiply currency amount into a labeled vector
fromFudgeMsg.MultipleCurrencyAmount <- function (msg) {
  currencies <- NULL
  amounts <- NULL
  for (field in fields.FudgeMsg (msg)) {
    fieldName <- field$Name
    if (length (fieldName) > 0) {
      fieldValue <- field$Value
      if (fieldName == "currencies") {
        currencies <- sapply (fields.FudgeMsg (fieldValue), function (x) {
          x$Value
        })
      } else {
        if (fieldName == "amounts") {
          amounts <- as.vector (fieldValue)
        }
      }
    }
  }
  names (amounts) <- currencies
  amounts
}

# Brings currency definitions into scope
Install.Currency <- function (stub) {
  stub.Currency <- stub$begin ("Currency", Category.CURRENCY)
  stub.Currency$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.MultipleCurrencyAmount (msg)", "MultipleCurrencyAmount")
  stub.Currency$end ()
}
