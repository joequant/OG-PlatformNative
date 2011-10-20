##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts a Fudge message representation of java.lang.Number to R
fromFudgeMsg.Number <- function (x) {
  x$value
}

fromFudgeMsg.Byte <- fromFudgeMsg.Number
fromFudgeMsg.Double <- fromFudgeMsg.Number
fromFudgeMsg.Float <- fromFudgeMsg.Number
fromFudgeMsg.Integer <- fromFudgeMsg.Number
fromFudgeMsg.Long <- fromFudgeMsg.Number
fromFudgeMsg.Short <- fromFudgeMsg.Number
