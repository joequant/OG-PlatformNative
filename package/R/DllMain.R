##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

.onLoad <- function (libname, pkgname) {
  library.dynam ("OpenGamma", pkgname)
  Functions_install ()
}

OpenGammaCall <- function (method, ...) {
  .Call (method, ..., PACKAGE = "OpenGamma")
}

.Last.lib <- function (path) {
  library.dynam.unload ("OpenGamma", path)
}
