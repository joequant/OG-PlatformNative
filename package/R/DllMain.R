##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads the OpenGamma native code package and brings the functions defined in the Java stack into scope
.onLoad <- function (libname, pkgname) {
  library.dynam ("OpenGamma", pkgname)
  LOGINFO ("Installing core objects")
  Install.ErrorValue ()
  Install.FudgeMsg ()
  Install.Functions ()
  Install.LiveData ()
  Install.Procedures ()
  LOGINFO ("Installing local data bindings")
  Install.MarketDataRequirementNames ()
  Install.MarketDataSnapshot ()
  Install.ValueProperties ()
  Install.ValueRequirementNames ()
  Install.ViewClient ()
  Install.ViewComputationResultModel ()
  Install.VolatilityCubeSnapshot ()
  Install.VolatilitySurfaceSnapshot ()
  Install.YieldCurveSnapshot ()
}

# Makes a call into the native code package
OpenGammaCall <- function (method, ...) {
  .Call (method, ..., PACKAGE = "OpenGamma")
}

# Unloads the OpenGamma native code package
.Last.lib <- function (path) {
  library.dynam.unload ("OpenGamma", path)
}
