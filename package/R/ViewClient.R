##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Creates a view client wrapper
interop.ViewClient <- function (uniqueId) {
  new ("ViewClient", uniqueId = as.ExternalRef (uniqueId, "destroy.ViewClient"))
}

# Returns the unique identifier as a string representation of the view client
.toString.ViewClient <- function (vc) {
  from.ExternalRef (vc@uniqueId)
}

setClass ("ViewClient", representation (uniqueId = "externalptr"))
setMethod ("as.character", signature = "ViewClient", definition = function (x, ...) { .toString.ViewClient (x) })
