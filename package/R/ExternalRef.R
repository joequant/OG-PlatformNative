##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Creates an external reference value, registering a Java procedure for its destructor
as.ExternalRef <- function (value, destructorName) {
  OpenGammaCall ("ExternalRef_create", value, destructorName)
}

# Returns the R value component of an external reference value
from.ExternalRef <- function (ref) {
  OpenGammaCall ("ExternalRef_fetch", ref)
}

# Declares an external reference based object
object.ExternalRef <- function (className) {
  LOGDEBUG (paste ("Declare ExternalRef", className))
  Install.Object (className, representation (ref = "externalptr"))
  setMethod ("as.character", signature = className, definition = function (x, ...) { toString (from.ExternalRef (x@ref)) })
  interop <- paste ("Interop", className, sep = ".")
  destroy <- paste ("destroy", className, sep = ".")
  cmd <- paste (interop, " <<- function (ref) { new (\"", className, "\", ref = as.ExternalRef (ref, \"", destroy, "\")) }", sep = "")
  eval (parse (text = cmd))
}
