##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

setClass (
  "FudgeMsg",
  representation (
    message = "externalptr"
  )
)

# Asserts the msg parameter is a FudgeMsg instance
.assert.FudgeMsg <- function (msg) {
  if (!is.object (msg) || (class (msg) != "FudgeMsg")) {
    stop ("Cannot apply to non-FudgeMsg", msg)
  }
}

# Get all the fields of a Fudge message
fields.FudgeMsg <- function (msg) {
  .assert.FudgeMsg (msg)
  .Call ("FudgeMsg_getAllFields", msg@message)
}

# Get a named field of a Fudge message
field.FudgeMsg <- function (msg, field) {
  fields <- fields.FudgeMsg (msg)
  if (length (fields) > 0) {
    result <- c ()
    if (is.numeric (field)) {
      ordinal <- as.integer (field)
      for (i in seq (from = 1, to = length (fields))) {
        if (fields[[i]]$Ordinal == ordinal) {
          result <- append (result, fields[[i]]$Value)
        }
      }
    } else {
      name <- as.character (field)
      for (i in seq (from = 1, to = length (fields))) {
        if (fields[[i]]$Name == name) {
          result <- append (result, fields[[i]]$Value)
        }
      }
    }
    result
  } else {
    fields
  }
}

# Fully expand a Fudge message
expand.FudgeMsg <- function (msg) {
  fields <- allFields.FudgeMsg (msg)
  if (length (fields) > 0) {
    for (i in seq (from = 1, to = length (fields))) {
      fieldValue <- fields[[i]]$Value;
      if (is.object (fieldValue) && (class (fieldValue) == "FudgeMsg")) {
        fields[[i]]$Value = expand.FudgeMsg (fieldValue)
      }
    }
  }
  fields
}

# Return the fully qualified class names
classNames.FudgeMsg <- function (msg) {
  field.FudgeMsg (msg, 0)
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
