##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Test if a value is an error code
is.ErrorValue <- function (obj) {
  is.object (obj) && (class (obj) == "OpenGammaErrorValue")
}

# Return a string representation of the error
toString.OpenGammaErrorValue <- function (obj) {
  obj$toString
}

setClass ("OpenGammaErrorValue", representation (code = "numeric", index = "numeric", message = "character", toString = "character"))
setMethod ("toString", signature ="OpenGammaErrorValue", definition = function (x) { toString.OpenGammaErrorValue (x) })
