##
 # Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Functions that weren't executed because of missing inputs
missingInputs.NotCalculatedSentinel <- "Missing inputs"

# Functions that weren't executed because of missing market data
missingMarketData.NotCalculatedSentinel <- "Missing market data"

# Functions that were executed but failed to produce one or more results
evaluationError.NotCalculatedSentinel <- "Evaluation error"

# Functions that weren't executed because of blacklist suppression
suppressed.NotCalculatedSentinel <- "Suppressed"

# Converts a Fudge representation of a placeholder value to a string
fromFudgeMsg.NotCalculatedSentinel <- function (msg) {
  enum <- msg[1]
  if (enum == "MISSING_INPUTS") {
    missingInputs.NotCalculatedSentinel
  } else {
    if (enum == "EVALUATION_ERROR") {
      evaluationError.NotCalculatedSentinel
    } else {
      if (enum == "SUPPRESSED") {
        suppressed.NotCalculatedSentinel
      } else {
        if (enum == "MISSING_MARKET_DATA") {
          missingMarketData.NotCalculatedSentinel
        } else {
          stop (paste ("Invalid ENUM value", enum))
        }
      }
    }
  }
}

# Brings declarations for NotCalculatedSentinel into scope
Install.NotCalculatedSentinel <- function (stub) {
  stub.MissingInput <- stub$begin ("MissingInput", Category.VALUE)
  stub.MissingInput$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.NotCalculatedSentinel (msg)")
  stub.MissingInput$end ()
  stub.MissingOutput <- stub$begin ("MissingOutput", Category.VALUE)
  stub.MissingOutput$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.NotCalculatedSentinel (msg)")
  stub.MissingOutput$end ()
}
