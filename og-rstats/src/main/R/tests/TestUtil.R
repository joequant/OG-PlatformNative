##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

LOGDEBUG <- function (...) {
  OpenGamma:::LOGDEBUG (...)
}

LOGINFO <- function (...) {
  OpenGamma:::LOGINFO (...)
}

LOGWARN <- function (...) {
  OpenGamma:::LOGWARN (...)
}

LOGERROR <- function (...) {
  OpenGamma:::LOGERROR (...)
}

LOGFATAL <- function (...) {
  OpenGamma:::LOGFATAL (...)
}

ASSERT <- function (expr) {
  if (expr) {
    LOGDEBUG ("Assertion passed")
  } else {
    LOGFATAL ("Assertion failed")
    stop ("Assertion failed")
  }
}

ASSERT_EQUAL <- function (actual, expected) {
  if ((length (actual) == length (expected)) && (actual == expected)) {
    LOGDEBUG ("Assertion passed")
  } else {
    LOGFATAL ("Assertion failed")
    LOGERROR ("Expected", expected)
    LOGERROR ("Actual", actual)
    stop ("Assertion failed")
  }
}

OpenGamma::Init ()
