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
object.ExternalRef <- function (stub.ExternalRef) {
  className <- stub.ExternalRef$module
  LOGDEBUG ("Declare ExternalRef", className)
  Install.Object (stub.ExternalRef, representation (ref = "externalptr"))
  stub.ExternalRef$setMethod ("as.character", "function (x, ...) { toString (from.ExternalRef (x@ref)) }")
  stub.ExternalRef$interop (paste ("new (\"", className, "\", ref = as.ExternalRef (data, \"destroy.", className, "\"))", sep = ""))
}
