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

.default.log.level <- function () {
  log.level <- Sys.getenv ("OPENGAMMA_R_LOGLEVEL")
  if (log.level == "") {
    LOGLEVEL_INFO[1]
  } else {
    log.level
  }
}
.log.level <- .default.log.level ()

.log <- function (level, ...) {
  if (level[1] >= .log.level) print (paste (level[2], paste (...)))
}

LOGDEBUG <- function (...) { .log (LOGLEVEL_DEBUG, ...) }
LOGINFO <- function (...) { .log (LOGLEVEL_INFO, ...) }
LOGWARN <- function (...) { .log (LOGLEVEL_WARN, ...) }
LOGERROR <- function (...) { .log (LOGLEVEL_ERROR, ...) }
LOGFATAL <- function (...) { .log (LOGLEVEL_FATAL, ...) }

.set.log.level <- function (level) {
  .log.level <<- level[1]
}

debug.log.level <- function () { .set.log.level (LOGLEVEL_DEBUG) }
info.log.level <- function () { .set.log.level (LOGLEVEL_INFO) }
warn.log.level <- function () { .set.log.level (LOGLEVEL_WARN) }
error.log.level <- function () { .set.log.level (LOGLEVEL_ERROR) }
fatal.log.level <- function () { .set.log.level (LOGLEVEL_FATAL) }
