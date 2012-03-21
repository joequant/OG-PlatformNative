##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings market data requirement names from the Java stack into scope
Install.MarketDataRequirementNames <- function (stub) {
  func <- find.Functions ("MarketDataRequirementNames")
  if (func >= 0) {
    stub.MarketDataRequirementNames <- stub$begin ("MarketDataRequirementNames", Category.MARKET_DATA)
    names <- invoke.Functions (func, list ())
    for (name in names) {
      stub.MarketDataRequirementNames$const (
        make.names (gsub ("_", ".", name)),
        paste (name, "constant"),
        "The symbolic constant used within the analytics library to specify market data requirements.",
        paste ("\"", gsub ("(\"|\\\\)", "\\\\\\1", name), "\"", sep = ""),
        TRUE)
    }
    LOGDEBUG (length (names), "MarketDataRequirementNames imported")
    stub.MarketDataRequirementNames$end ()
  } else {
    LOGDEBUG ("MarketDataRequirementNames not available")
  }
}
