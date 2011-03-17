##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# TODO: Migrate the code from here to a Fudge-R project, or change things elsewhere to use that if one already exists

# Asserts the msg parameter is a FudgeMsg instance
.assert.FudgeMsg <- function (msg) {
  if (!is.object (msg) || (class (msg) != "FudgeMsg")) {
    stop ("Cannot apply to non-FudgeMsg", msg)
  }
}

# Get all the fields of a Fudge message
fields.FudgeMsg <- function (msg) {
  .assert.FudgeMsg (msg)
  OpenGammaCall ("FudgeMsg_getAllFields", msg@message)
}

# Get a named field of a Fudge message
field.FudgeMsg <- function (msg, field) {
  fields <- fields.FudgeMsg (msg)
  if (length (fields) > 0) {
    if (length (field) > 1) {
      sapply (field, function (x) { .field.FudgeMsg (fields, x) })
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
is.FudgeMsg <- function (obj) {
  is.object (obj) && (class (obj) == "FudgeMsg");
}

# Fully expand a Fudge message
expand.FudgeMsg <- function (msg) {
  lapply (fields.FudgeMsg (msg), function (x) {
    value <- x$Value
    if (is.FudgeMsg (value)) {
      list (Name = x$Name, Ordinal = x$Ordinal, Value = expand.FudgeMsg (value))
    } else {
      x
    }
  })
}

# Return the fully qualified class names
classNames.FudgeMsg <- function (msg) {
  msg[0]
}

# Return a display name based on the class name
displayName.FudgeMsg <- function (msg) {
  classNames <- classNames.FudgeMsg (msg)
  if (length (classNames) > 0) {
    classNames <- strsplit (classNames[[1]], "\\.")
    classNames[[length (classNames)]]
  } else {
    "FudgeMsg"
  }
}

# Return a string representation of the Fudge message
toString.FudgeMsg <- function (msg) {
  paste (c ("{", paste (sapply (fields.FudgeMsg (msg), function (x) { paste (x$Name, x$Ordinal, "=", toString (x$Value)) }), collapse = ", "), "}"), collapse = "")
}

setClass ("FudgeMsg", representation (message = "externalptr"))
setMethod ("[", signature = "FudgeMsg", definition = function (x, i, j, ..., drop) { field.FudgeMsg (x, i) })
setMethod ("$", signature = "FudgeMsg", definition = function (x, name) { field.FudgeMsg (x, name) })
setMethod ("length", signature = "FudgeMsg", definition = function (x) { length (fields.FudgeMsg (x)) } )
setMethod ("toString", signature = "FudgeMsg", definition = function (x) { toString.FudgeMsg (x) })
toFudgeMsg <- function (x) { NULL }
setGeneric ("toFudgeMsg");
setMethod ("toFudgeMsg", signature = "FudgeMsg", definition = function (x) { x })
