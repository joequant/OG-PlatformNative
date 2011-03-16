##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

LiveData_count <- function () {
  .C ("LiveData_count", result = integer (1)) $result
}

LiveData_getName <- function (index) {
  .C ("LiveData_getName", as.integer (index), result = character (1)) $result
}
