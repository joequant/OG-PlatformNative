##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings value requirement names from the Java stack into scope
Install.ValueRequirementNames <- function () {
  names <- ValueRequirementNames ()
  lapply (names, function (name) {
    cmd <- paste ("ValueRequirementNames.", make.names (name), " <<- \"", gsub ("(\"|\\\\)", "\\\\\\1", name), "\"", sep = "")
    eval (parse (text = cmd))
    0
  })
  LOGDEBUG (length (names), "ValueRequirementNames imported")
}
