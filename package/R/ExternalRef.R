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
from.ExternalRef <- function (value) {
  OpenGammaCall ("ExternalRef_fetch", value)
}
