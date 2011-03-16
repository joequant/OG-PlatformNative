##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

Procedures_count <- function () {
  .C ("Procedures_count", result = integer (1)) $result
}

Procedures_getName <- function (index) {
  .C ("Procedures_getName", as.integer (index), result = character (1)) $result
}
