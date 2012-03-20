##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings array definitions into scope
Install.Array <- function (stub) {
  stub.Array <- stub$begin ("Array", Category.INTERNAL)
  stub.Array$interop ("ds <- data[[1]]\nd <- data[2:(1 + ds)]\narray (data[(2 + ds):length (data)], d)")
  stub.Array$encode ("d <- dim (data)\nds <- length (d)\nc (ds, d, data)")
  stub.Array$end ()
}
