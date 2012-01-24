##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# TODO: Migrate the code from here to a Fudge-R project, or change things elsewhere to use that if one already exists

# Tests if an object is a Fudge message
is.FudgeMsg <- function (x) {
  .is.Object (x, "FudgeMsg")
}

# Asserts an object is a Fudge message
.assert.FudgeMsg <- function (x) {
  .assert.Object (x, "FudgeMsg")
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

# Produce an R representation from an arbitrary message
toObject.FudgeMsg <- function (x, fallbackConstructor = toString) {
  className <- displayName.FudgeMsg (x)
  constructor <- paste ("fromFudgeMsg", className, sep = ".")
  if (existsFunction (constructor)) {
    getFunction (constructor)(x)
  } else {
    LOGWARN ("No constructor", constructor)
    fallbackConstructor (x)
  }
}

# Return a display name based on the class name
displayName.FudgeMsg <- function (x) {
  classNames <- classNames.FudgeMsg (x)
  if (length (classNames) > 0) {
    classNames <- strsplit (classNames[[1]], "\\.")[[1]]
    make.names (classNames[[length (classNames)]])
  } else {
    "FudgeMsg"
  }
}

# Return a string representation of the Fudge message
.toString.FudgeMsg <- function (x) {
  paste (c ("{", paste (sapply (fields.FudgeMsg (x), function (y) { paste (y$Name, y$Ordinal, "=", toString (y$Value)) }), collapse = ", "), "}"), collapse = "")
}

# Prints out a Fudge message with indentation to preserve the structure
.print.FudgeMsg <- function (x, indent) {
  sapply (fields.FudgeMsg (x), function (y) {
    name <- y$Name
    ordinal <- y$Ordinal
    value <- y$Value
    if (length (ordinal) > 0) {
      if (length (name) > 0) {
        ordinal <- paste ("(", ordinal, ")", sep = "")
      }
    }
    if (class (value) == "FudgeMsg") {
      print (paste (indent, name, ordinal, " = {", sep = ""))
      .print.FudgeMsg (value, paste (indent, "  ", sep = ""))
      print (paste (indent, "}", sep = ""))
    } else {
      print (paste (indent, name, ordinal, " = ", toString (value), sep = ""))
    }
  })
  invisible (x)
}

# Prints out a Fudge message with indentation to preserve the structure
print.FudgeMsg <- function (x, ...) {
  .print.FudgeMsg (x, "")
}

# Declares a FudgeMsg based object
.object.FudgeMsg <- function (stub.Object) {
  className <- stub.Object$module
  LOGDEBUG ("Declare FudgeMsg", className)
  Install.Object (stub.Object, representation (msg = "FudgeMsg"))
  stub.Object$setMethod ("toFudgeMsg", "function (x) { x@msg }")
  stub.Object$setMethod ("as.character", "function (x) { toString (x@msg) }")
  stub.Object$fromFudgeMsg (paste ("new (\"", className, "\", msg = msg)", sep = ""))
}

# Declares a field within a FudgeMsg based object
.field.object.FudgeMsg <- function (stub.Object, fieldName, fn = NULL) {
  className <- stub.Object$module
  value <- paste ("x@msg", fieldName, sep = "$")
  if (!is.null (fn)) {
    value <- paste ("OpenGamma:::", fn, " (", value, ")", sep = "")
  }
  stub.Object$func (
    fieldName,
    paste (className, fieldName, "accessor"),
    paste ("Accesses the", fieldName, "field of a", className, "object"),
    list (x = "The object to query"),
    paste (".assert.", className, " (x)\n", value, sep = ""))
}

# Skeleton generic; returns NULL if not provided for an object
toFudgeMsg <- function (x) {
  NULL
}

setClass ("FudgeMsg", representation (message = "externalptr"))
setMethod ("[", signature = "FudgeMsg", definition = function (x, i) { field.FudgeMsg (x, i) })
setMethod ("$", signature = "FudgeMsg", definition = function (x, name) { field.FudgeMsg (x, name) })
setMethod ("as.character", signature = "FudgeMsg", definition = function (x, ...) { .toString.FudgeMsg (x) })
setGeneric ("toFudgeMsg");
setMethod ("toFudgeMsg", signature = "FudgeMsg", definition = function (x) { x })
