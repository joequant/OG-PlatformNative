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
  with <- NULL
  without <- NULL
  for (field in fields.FudgeMsg (msg)) {
    fieldName <- field$Name
    if (fieldName == "without") {
      without <- field$Value
    } else {
      if (fieldName == "with") {
        with <- field$Value
      }
    }
  }
  if (!is.null (without)) {
    properties <- c ()
    for (field in fields.FudgeMsg (without)) {
      properties <- append (properties, .escape.ValueProperties (field$Value))
    }
    if (length (properties) > 0) {
      paste ("INFINITE-{", paste (properties, collapse = ","), "}", sep = "")
    } else {
      "INFINITE"
    }
  } else {
    if (!is.null (with)) {
      str <- c ()
      for (field in fields.FudgeMsg (with)) {
        fieldValue <- field$Value
        property <- c (.escape.ValueProperties (field$Name), "=")
        if (is.FudgeMsg (fieldValue)) {
          optional <- FALSE
          property <- append (property, "[")
          values <- c ()
          for (value in fields.FudgeMsg (fieldValue)) {
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
          if (fieldValue == "indicator") {
            property <- append (property, "[]")
          } else {
            property <- append (property, .escape.ValueProperties (fieldValue))
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

# Tests if one value property set can "satisfy" another
satisfiedBy.ValueProperties <- function (a, b) {
  # TODO: implement this properly
  TRUE
}

# The "empty" property set
empty.ValueProperties <- "EMPTY"

# The "infinite" property set
infinite.ValueProperties <- "INFINITE"

# Parse a string representation into a ValueProperties object
parse.ValueProperties <- function (propertyString) {
  # TODO
  propertyString
}

# Brings declarations for ValueProperties into scope
Install.ValueProperties <- function () {
  object.FudgeMsg ("ValueProperties")
  setMethod ("as.character", signature = "ValueProperties", definition = function (x) { .toString.ValueProperties (x@msg) })
}
