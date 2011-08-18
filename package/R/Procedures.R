##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

Procedures_count <- function () {
  OpenGammaCall ("Procedures_count")
}

Procedures_getName <- function (index) {
  OpenGammaCall ("Procedures_getName", as.integer (index))
}
