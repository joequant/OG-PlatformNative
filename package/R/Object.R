##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Declares an object
Install.Object <- function (className, rep) {
  LOGDEBUG (paste ("Declare object", className))
  setClass (className, rep)
  is <- paste ("is", className, sep = ".")
  assert <- paste (".assert", className, sep = ".")
  cmd <- paste (is, " <<- function (x) { is.object (x) && class (x) == \"", className, "\" }", sep = "")
  eval (parse (text = cmd))
  cmd <- paste (assert, " <<- function (x) { if (!", is, " (x)) { stop (\"Cannot apply to non-", className, "\", x) } }", sep = "")
  eval (parse (text = cmd))
}
