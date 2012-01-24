##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings value requirement names from the Java stack into scope
Install.ValueRequirementNames <- function (stub) {
  func <- find.Functions ("ValueRequirementNames")
  if (func >= 0) {
    stub.ValueRequirementNames <- stub$begin ("ValueRequirementNames")
    names <- invoke.Functions (func, list ())
    for (name in names) {
      stub.ValueRequirementNames$const (
        make.names (gsub ("_", ".", name)),
        paste (name, "constant"),
        "The symbolic constant used within the analytics library to describe calculated values.",
        paste ("\"", gsub ("(\"|\\\\)", "\\\\\\1", name), "\"", sep = ""),
        TRUE)
    }
    stub.ValueRequirementNames$end ()
    LOGDEBUG (length (names), "ValueRequirementNames imported")
  } else {
    LOGDEBUG ("ValueRequirementNames not available")
  }
}
