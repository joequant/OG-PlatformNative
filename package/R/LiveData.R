##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

LiveData_count <- function () {
  OpenGammaCall ("LiveData_count")
}

LiveData_getName <- function (index) {
  OpenGammaCall ("LiveData_getName", as.integer (index))
}
