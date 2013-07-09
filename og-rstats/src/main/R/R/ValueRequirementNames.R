##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings value requirement names from the Java stack into scope
Install.ValueRequirementNames <- function (stub) {
  func <- find.Functions ("ValueRequirementNames")
  if (func >= 0) {
    stub.ValueRequirementNames <- stub$begin ("ValueRequirementNames", Category.VALUE)
    names <- invoke.Functions (func, list ())
    for (name in names) {
      stub.ValueRequirementNames$const (
        gsub ("\\.+$", "", gsub ("^\\.+", "", gsub ("\\.\\.+", ".", make.names (gsub ("_", ".", name, fixed = TRUE))))),
        paste (name, "constant"),
        "The symbolic constant used within the analytics library to describe calculated values.",
        paste ("\"", OpenGammaCall ("String_escape", name, "\\\""), "\"", sep = ""),
        TRUE)
    }
    stub.ValueRequirementNames$end ()
    LOGDEBUG (length (names), "ValueRequirementNames imported")
  } else {
    LOGDEBUG ("ValueRequirementNames not available")
  }
}
