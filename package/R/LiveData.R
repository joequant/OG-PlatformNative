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

# Brings a proxy declaration for a live data definition into scope
.installByIndex.LiveData <- function (index) {
  LOGFATAL ("Not implemented")
}

# Brings proxy declarations for all live data definitions into scope
Install.LiveData <- function () {
  LOGINFO ("Installing live data definitions")
  for (index in seq (from = 0, to = count.LiveData () - 1)) .installByIndex.LiveData (index)
}
