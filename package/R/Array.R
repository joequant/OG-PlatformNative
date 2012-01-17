##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts the transport form to an arbitrary-dimensional R array
Interop.Array <- function (data) {
  ds <- data[[1]]
  d <- data[2:(1 + ds)]
  array (data[(2 + ds):length (data)], d)
}

# Converts an arbirary-dimensional R array to the transport form
.encode.Array <- function (data) {
  d <- dim (data)
  ds <- length (d)
  c (ds, d, data)
}
