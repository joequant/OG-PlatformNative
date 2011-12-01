##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts a unique identifier to an object identifier
unversioned.Identifier <- function (identifier) {
  sapply (strsplit (identifier, "~"), function (x) {
    if (length (x) == 3) {
      paste (x[1], x[2], sep = "~")
    } else {
      paste (x, collapse = "~")
    }
  })
}
