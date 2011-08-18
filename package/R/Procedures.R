##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of procedures available
count.Procedures <- function () {
  OpenGammaCall ("Procedures_count")
}

# Returns the name of an available procedure
getName.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getName", as.integer (index))
}

# Brings proxy declarations for all available procedures into scope
install.Procedures <- function (index) {
  LOGFATAL ("Not implemented")
}
