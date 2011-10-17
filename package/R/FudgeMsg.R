##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# TODO: Migrate the code from here to a Fudge-R project, or change things elsewhere to use that if one already exists

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

# Convert a Fudge message encoding of a map to a data frame
mapToDataFrame.FudgeMsg <- function (x, keyFun = NULL, valueFun = NULL) {
  fields <- fields.FudgeMsg (x)
  fn <- keyFun
  if (is.null (fn)) {
    fn <- function (x) { x }
  }
  keys <- sapply (.field.FudgeMsg (fields, 1), fn)
  if (is.list (keys)) {
    x
  } else {
    fn <- valueFun
    if (is.null (fn)) {
      fn <- function (x) { x }
    }
    values <- sapply (.field.FudgeMsg (fields, 2), fn)
    if (is.list (values)) {
      x
    } else {
      data.frame (Key = keys, Value = values, row.names = "Key")
    }
  }
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

# Declares a FudgeMsg based object
object.FudgeMsg <- function (className) {
  LOGDEBUG ("Declare FudgeMsg", className)
  Install.Object (className, representation (msg = "FudgeMsg"))
  setMethod ("toFudgeMsg", signature = className, definition = function (x) { x@msg })
  setMethod ("as.character", signature = className, definition = function (x) { toString (x@msg) })
  fromFudgeMsg <- paste ("fromFudgeMsg", className, sep = ".")
  field <- paste (".field", className, sep = ".")
  cmd <- paste (fromFudgeMsg, " <<- function (msg) { new (\"", className, "\", msg = msg) }", sep = "")
  eval (parse (text = cmd))
  cmd <- paste (field, " <<- function (name, fn = NULL) { .objectField.FudgeMsg (\"", className, "\", name, fn) }", sep = "")
  eval (parse (text = cmd))
}

# Declares a field within a FudgeMsg based object
.objectField.FudgeMsg <- function (className, fieldName, fn) {
  assert <- paste (".assert", className, sep = ".")
  field <- paste (fieldName, className, sep = ".")
  value <- paste ("x@msg", fieldName, sep = "$")
  if (!is.null (fn)) {
    value <- paste (fn, " (", value, ")", sep = "")
  }
  cmd <- paste (
    paste (field, " <<- function (x) {", sep = ""),
    paste (assert, " (x)", sep = ""),
    value,
    "}",
    sep = "\n")
  eval (parse (text = cmd))
}

# Skeleton generic; returns NULL if not provided for an object
toFudgeMsg <- function (x) {
  NULL
}

# Brings declarations for FudgeMsg into scope
Install.FudgeMsg <- function () {
  Install.Object ("FudgeMsg", representation (message = "externalptr"))
  setMethod ("[", signature = "FudgeMsg", definition = function (x, i) { field.FudgeMsg (x, i) })
  setMethod ("$", signature = "FudgeMsg", definition = function (x, name) { field.FudgeMsg (x, name) })
  setMethod ("as.character", signature = "FudgeMsg", definition = function (x, ...) { .toString.FudgeMsg (x) })
  setGeneric ("toFudgeMsg");
  setMethod ("toFudgeMsg", signature = "FudgeMsg", definition = function (x) { x })
}
