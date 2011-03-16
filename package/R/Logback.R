##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

LOGLEVEL_DEBUG <- c(0, "Debug:")
LOGLEVEL_INFO <- c(1, "Info:")
LOGLEVEL_WARN <- c(2, "Warn:")
LOGLEVEL_ERROR <- c(3, "Error:")
LOGLEVEL_FATAL <- c(4, "Fatal:")

#.logLevel <- 1
.logLevel <- 0

.log <- function (level, str) {
  if (level[1] >= .logLevel) print (paste (level[2], str))
}

LOGDEBUG <- function (str) { .log (LOGLEVEL_DEBUG, str) }
LOGINFO <- function (str) { .log (LOGLEVEL_INFO, str) }
LOGWARN <- function (str) { .log (LOGLEVEL_WARN, str) }
LOGERROR <- function (str) { .log (LOGLEVEL_ERROR, str) }
LOGFATAL <- function (str) { .log (LOGLEVEL_FATAL, str) }

.setLogLevel <- function (level) {
  .logLevel <<- level[1]
}

setLogLevelDebug <- function () { .setLogLevel (LOGLEVEL_DEBUG) }
setLogLevelInfo <- function () { .setLogLevel (LOGLEVEL_INFO) }
setLogLevelWarn <- function () { .setLogLevel (LOGLEVEL_WARN) }
setLogLevelError <- function () { .setLogLevel (LOGLEVEL_ERROR) }
setLogLevelFatal <- function () { .setLogLevel (LOGLEVEL_FATAL) }
