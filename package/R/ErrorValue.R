##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings declarations for ErrorValue into scope
Install.ErrorValue <- function () {
  Install.Object ("ErrorValue", representation (code = "numeric", index = "numeric", message = "character", toString = "character"))
  setClass ("ErrorValue", representation (code = "numeric", index = "numeric", message = "character", toString = "character"))
  setMethod ("as.character", signature = "ErrorValue", definition = function (x, ...) { x$toString })
}
