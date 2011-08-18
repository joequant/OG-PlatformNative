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

#.log.level <- 1
.log.level <- 0

.log <- function (level, str) {
  if (level[1] >= .log.level) print (paste (level[2], str))
}

LOGDEBUG <- function (str) { .log (LOGLEVEL_DEBUG, str) }
LOGINFO <- function (str) { .log (LOGLEVEL_INFO, str) }
LOGWARN <- function (str) { .log (LOGLEVEL_WARN, str) }
LOGERROR <- function (str) { .log (LOGLEVEL_ERROR, str) }
LOGFATAL <- function (str) { .log (LOGLEVEL_FATAL, str) }

.set.log.level <- function (level) {
  .log.level <<- level[1]
}

debug.log.level <- function () { .set.log.level (LOGLEVEL_DEBUG) }
info.log.level <- function () { .set.log.level (LOGLEVEL_INFO) }
warn.log.level <- function () { .set.log.level (LOGLEVEL_WARN) }
error.log.level <- function () { .set.log.level (LOGLEVEL_ERROR) }
fatal.log.level <- function () { .set.log.level (LOGLEVEL_FATAL) }
