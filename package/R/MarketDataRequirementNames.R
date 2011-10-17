##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings market data requirement names from the Java stack into scope
Install.MarketDataRequirementNames <- function () {
  names <- MarketDataRequirementNames ()
  lapply (names, function (name) {
    cmd <- paste ("MarketDataRequirementNames.", make.names (gsub ("_", ".", name)), " <<- \"", gsub ("(\"|\\\\)", "\\\\\\1", name), "\"", sep = "")
    eval (parse (text = cmd))
    0
  })
  LOGDEBUG (length (names), "MarketDataRequirementNames imported")
}
