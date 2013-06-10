##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Escape characters in string representation of a ValueProperties
.escape.ValueProperties <- function (str) {
  OpenGammaCall ("String_escape", str, "\\,= ?[]")
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
        property <- rep ("", 6)
        property[1] <- .escape.ValueProperties (field$Name)
        property[2] <- "="
        if (is.FudgeMsg (fieldValue)) {
          property[3] <- "["
          values <- c ()
          for (value in fields.FudgeMsg (fieldValue)) {
            if (value$Name == "optional") {
              property[6] <- "?"
            } else {
              values <- append (values, value$Value)
            }
          }
          property[4] <- paste (.escape.ValueProperties (values), collapse = ",")
          property[5] <- "]"
        } else {
          if (fieldValue == "indicator") {
            property[3] <- "[]"
          } else {
            property[4] <- .escape.ValueProperties (fieldValue)
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
  # [RSTAT-24] implement this
  TRUE
}

# The empty property set
empty.ValueProperties <- "EMPTY"

# The infinite property set
infinite.ValueProperties <- "INFINITE"

# Parse a string representation into a ValueProperties object
parse.ValueProperties <- function (propertyString) {
  # [RSTAT-23] this implementation is broken; implement properly
  properties <- list ()
  for (property in strsplit (propertyString, ",")[[1]]) {
    property.a <- strsplit (property, "=")[[1]]
    properties[property.a[1]] <- property.a[2]
  }
  properties
}

# Brings declarations for ValueProperties into scope
Install.ValueProperties <- function (stub) {
  stub.ValueProperties <- stub$begin ("ValueProperties", Category.VALUE)
  .object.FudgeMsg (stub.ValueProperties)
  stub.ValueProperties$setMethod ("as.character", "function (x) { OpenGamma:::.toString.ValueProperties (x@msg) }")
  stub.ValueProperties$const ("empty", "The \"empty\" value property set", "The empty value property set can be satisfied by any properties, and can satisfy none (other than the empty set).", "OpenGamma:::empty.ValueProperties")
  stub.ValueProperties$const ("infinite", "The \"infinite\" value property set", "The infinite value property set can satisfy any property constraints, and can only be satisfied by the infinite set.", "OpenGamma:::infinite.ValueProperties")
  stub.ValueProperties$func (
    "satisfiedBy",
    "Test the satisfied-by relationship between two value property sets",
    "Tests if one set of value properties can satisfy the constraints described yb another. Returns TRUE if A is satisfied by B (or equally worded as if B satisfies A) and FALSE otherwise. A and B may be either strung representations of value properties or instances of the ValueProperties object type.",
    list (a = "The constaints to test satisfaction of", b = "The properties satisfy the constraints with"),
    "OpenGamma:::satisfiedBy.ValueProperties (a, b)")
  stub.ValueProperties$func (
    "parse",
    "Create a ValueProperties object from a string representation",
    "Parses the string representation and returns the equivalent ValueProperties object.",
    list (propertyString = "The string to parse"),
    "OpenGamma:::parse.ValueProperties (propertyString)")
  stub.ValueProperties$end ()
}
