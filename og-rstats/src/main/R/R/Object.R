##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests if a value is an instance of the named class
.is.Object <- function (x, className) {
  is.object (x) && (class (x) == className)
}

# Produces a short string describing the value. Calling toString can crash the system if
# big objects are used.
.description <- function (x) {
  if (is.object (x)) {
    class (x)
  } else {
    paste ("non-object", typeof (x))
  }
}

# Asserts that a value is an instance of the named class
.assert.Object <- function (x, className) {
  if (!.is.Object (x, className)) { stop (paste ("Can't apply to non", className, "- type is", .description (x))) }
  invisible (0)
}

# Declares an object
Install.Object <- function (stub.Object, rep) {
  className <- stub.Object$module
  LOGDEBUG ("Declare object", className)
  stub.Object$setClass (rep)
  stub.Object$func (
    "is",
    paste (className, "instance-of test"),
    paste ("Tests whether a value is an instance of the", className, "class"),
    list (x = "The object to test"),
    paste ("OpenGamma:::.is.Object (x, \"", className, "\")", sep = ""))
  stub.Object$func (
    ".assert",
    paste (className, "assertion"),
    paste ("Asserts that a value is an instance of the", className, "class, issuing a 'stop' if it isn't"),
    list (x = "The object to test"),
    paste ("OpenGamma:::.assert.Object (x, \"", className, "\")", sep = ""))
}
