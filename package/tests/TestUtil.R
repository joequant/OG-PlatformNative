##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

LOGDEBUG <- function (x) {
  OpenGamma:::LOGDEBUG (x)
}

LOGINFO <- function (x) {
  OpenGamma:::LOGINFO (x)
}

LOGWARN <- function (x) {
  OpenGamma:::LOGWARN (x)
}

LOGERROR <- function (x) {
  OpenGamma:::LOGERROR (x)
}

LOGFATAL <- function (x) {
  OpenGamma:::LOGFATAL (x)
}

ASSERT <- function (expr) {
  if (expr) {
    LOGDEBUG ("Assertion passed")
  } else {
    LOGFATAL ("Assertion failed")
    stop ("Assertion failed")
  }
}
