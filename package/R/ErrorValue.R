##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests if a value is an ErrorValue instance
is.ErrorValue <- function (x) {
  .is.Object (x, "ErrorValue")
}

# Asserts a value is an ErrorValue instance
.assert.ErrorValue <- function (x) {
  .assert.Object (x, "ErrorValue")
}

# Generates an error message string
.message.ErrorValue <- function (err, args) {
  .assert.ErrorValue (err)
  if (err@code == 1) {
    paste ("Parameter '", args (err@index + 1), "' invalid - ", err@message, sep = "")
  } else if (err@code == 3) {
    paste ("Parameter '", args (err@index + 1), "' invalid - ", err@message, sep = "")
  } else {
    err@message
  }
}

setClass ("ErrorValue", representation (code = "numeric", index = "numeric", message = "character", toString = "character"))
setMethod ("as.character", signature = "ErrorValue", definition = function (x, ...) { x$toString })
