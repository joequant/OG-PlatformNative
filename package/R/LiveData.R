##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of live data definitions available
count.LiveData <- function () {
  OpenGammaCall ("LiveData_count")
}

# Returns the name of a live data definition
getName.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getName", as.integer (index))
}

# Brings proxy declarations for all live data definitions into scope
install.LiveData <- function () {
  LOGFATAL ("Not implemented")
}
