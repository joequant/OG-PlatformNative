##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads the OpenGamma native code package and brings the functions defined in the Java stack into scope
.onLoad <- function (libname, pkgname) {
  library.dynam ("OpenGamma", pkgname)
  install.Functions ()
  install.LiveData ()
  install.Procedures ()
}

# Makes a call into the native code package
OpenGammaCall <- function (method, ...) {
  .Call (method, ..., PACKAGE = "OpenGamma")
}

# Unloads the OpenGamma native code package
.Last.lib <- function (path) {
  library.dynam.unload ("OpenGamma", path)
}
