##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# TODO: Migrate the code from here to a Fudge-R project, or change things elsewhere to use that if one already exists

# Asserts the parameter is a FudgeMsg instance
.assert.FudgeMsg <- function (x) {
  if (!is.FudgeMsg (x)) {
    stop ("Cannot apply to non-FudgeMsg", x)
  }
}

# Get all the fields of a Fudge message
fields.FudgeMsg <- function (x) {
  .assert.FudgeMsg (x)
  OpenGammaCall ("FudgeMsg_getAllFields", x@message)
}

# Get a named field of a Fudge message
field.FudgeMsg <- function (x, field) {
  fields <- fields.FudgeMsg (x)
  if (length (fields) > 0) {
    if (length (field) > 1) {
      sapply (field, function (y) { .field.FudgeMsg (fields, y) })
    } else {
      .field.FudgeMsg (fields, field)
    }
  } else {
    fields
  }
}

# Returns a vector comparing field names or ordinals
.isField.FudgeMsg <- function (fields, field) {
  if (is.numeric (field)) {
    ordinal <- as.integer (field)
    sapply (fields, function (x) { !is.null (x$Ordinal) && (x$Ordinal == ordinal) })
  } else {
    name <- as.character (field)
    sapply (fields, function (x) { !is.null (x$Name) && (x$Name == name) })
  }
}

# Get a named field from a list of fields (e.g. one returned by fields.FudgeMsg)
.field.FudgeMsg <- function (fields, field) {
  result <- sapply (fields[.isField.FudgeMsg (fields, field)], function (x) { x$Value })
  if (length (result) == 1) {
    result[[1]]
  } else {
    result
  }
}

# Test if a value is a Fudge message
is.FudgeMsg <- function (x) {
  is.object (x) && (class (x) == "FudgeMsg");
}

# Fully expand a Fudge message
expand.FudgeMsg <- function (x) {
  lapply (fields.FudgeMsg (x), function (y) {
    value <- y$Value
    if (is.FudgeMsg (value)) {
      list (Name = y$Name, Ordinal = y$Ordinal, Value = expand.FudgeMsg (value))
    } else {
      y
    }
  })
}

# Return the fully qualified class names
classNames.FudgeMsg <- function (x) {
  x[0]
}

# Return a display name based on the class name
displayName.FudgeMsg <- function (x) {
  classNames <- classNames.FudgeMsg (x)
  if (length (classNames) > 0) {
    classNames <- strsplit (classNames[[1]], "\\.")[[1]]
    classNames[[length (classNames)]]
  } else {
    "FudgeMsg"
  }
}

# Return a string representation of the Fudge message
.toString.FudgeMsg <- function (x) {
  paste (c ("{", paste (sapply (fields.FudgeMsg (x), function (y) { paste (y$Name, y$Ordinal, "=", toString (y$Value)) }), collapse = ", "), "}"), collapse = "")
}

setClass ("FudgeMsg", representation (message = "externalptr"))
setMethod ("[", signature = "FudgeMsg", definition = function (x, i) { field.FudgeMsg (x, i) })
setMethod ("$", signature = "FudgeMsg", definition = function (x, name) { field.FudgeMsg (x, name) })
setMethod ("as.character", signature = "FudgeMsg", definition = function (x, ...) { .toString.FudgeMsg (x) })
toFudgeMsg <- function (x) { NULL }
setGeneric ("toFudgeMsg");
setMethod ("toFudgeMsg", signature = "FudgeMsg", definition = function (x) { x })
