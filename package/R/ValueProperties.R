##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Escape characters in string representation of a ValueProperties
.escape.ValueProperties <- function (str) {
  gsub ("([,= ?]|\\[|\\]|\\\\)", "\\\\\\1", str)
}

# Creates the string representation of a ValueProperties message
.toString.ValueProperties <- function (msg) {
  x <- msg$without
  if (length (x) > 0) {
    properties <- c ()
    for (field in fields.FudgeMsg (x)) {
      properties <- append (properties, .escape.ValueProperties (field$Value))
    }
    if (length (properties) > 0) {
      paste ("INFINITE-{", paste (properties, collapse = ","), "}", sep = "")
    } else {
      "INFINITE"
    }
  } else {
    x <- msg$with
    if (length (x) > 0) {
      str <- c ()
      for (field in fields.FudgeMsg (x)) {
        property <- c (.escape.ValueProperties (field$Name), "=")
        if (is.FudgeMsg (field$Value)) {
          optional <- FALSE
          property <- append (property, "[")
          values <- c ()
          for (value in fields.FudgeMsg (field$Value)) {
            if (value$Name == "optional") {
              optional <- TRUE
            } else {
              values <- append (values, .escape.ValueProperties (value$Value))
            }
          }
          property <- append (property, paste (values, collapse = ","), "]")
          if (optional) {
            property <- append (property, "?")
          }
        } else {
          if (field$Value == "indicator") {
            property <- append (property, "[]")
          } else {
            property <- append (property, .escape.ValueProperties (field$Value))
          }
        }
        str <- append (str, paste (property, collapse = ""))
      }
      paste (str, collapse = ",")
    } else {
      "EMPTY"
    }
  }
}

# Brings declarations for ValueProperties into scope
Install.ValueProperties <- function () {
  object.FudgeMsg ("ValueProperties")
  setMethod ("as.character", signature = "ValueProperties", definition = function (x) { .toString.ValueProperties (x@msg) })
}
