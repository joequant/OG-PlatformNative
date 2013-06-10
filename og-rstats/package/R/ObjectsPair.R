##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Convert a FudgeMsg representation of an objects pair to a list
fromFudgeMsg.ObjectsPair <- function (x) {
  first <- NULL
  second <- NULL
  for (field in fields.FudgeMsg (x)) {
    fieldName <- field$Name
    if (length (fieldName) > 0) {
      if (fieldName == "first") {
        first <- field$Value
        if (is.FudgeMsg (first)) {
          first <- toObject.FudgeMsg (first)
        }
      } else {
        if (fieldName == "second") {
          second <- field$Value
          if (is.FudgeMsg (second)) {
            second <- toObject.FudgeMsg (second)
          }
        }
      }
    }
  }
  list (first, second)
}

# Brings objects pair definitions into scope
Install.ObjectsPair <- function (stub) {
  stub.ObjectsPair <- stub$begin ("ObjectsPair", Category.MISC)
  stub.ObjectsPair$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.ObjectsPair (msg)")
  stub.ObjectsPair$end ()
}
