##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of live data definitions available
count.LiveData <- function () {
  OpenGammaCall ("LiveData_count")
}

# Returns the name of a live data definition
getName.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getName", as.integer (index))
}

# Returns the parameter names for a live data connection
getParameterNames.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getParameterNames", as.integer (index))
}

# Returns the parameter flags for a live data connection
getParameterFlags.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getParameterFlags", as.integer (index))
}

# Connects to a live data component, returning the first result
invoke.LiveData <- function (index, args) {
  OpenGammaCall ("LiveData_invoke", as.integer (index), args, parent.frame ())
}

# Brings a proxy declaration for a live data definition into scope
.installByIndex.LiveData <- function (index) {
  name <- getName.LiveData (index)
  if (!s.null (name)) {
    LOGDEBUG ("Found live data", name)
    argNames <- getParameterNames.LiveData (index)
    argFlags <- getParameterFlags.LiveData (index)
    if (length (argNames) == length (argFlags)) {
      argDecl <- c ()
      validate <- c ()
      argStrings <- c ()
      if (length (argNames) > 0) {
        for (i in seq (from = 1, to = length (argNames))) {
          flagOptional <- FALSE
          flags <- argFlags[i]
          if (flasg >= PARAMETER_FLAG_OPTIONAL) {
            flags <- flags - PARAMETER_FLAG_OPTIONAL
            flagOptional <- TRUE
          }
          if (flagOptional) {
            argDecl <- append (argDecl, paste (argNames[i], "= NULL"))
          } else {
            argDecl <- append (argDecl, argNames[1])
            validate <- append (validate, paste ("if (missing (", argNames[i], ") || is.null (", argNames[i], ")) stop (\"Parameter '", argNames[i], "' may not be null\")", sep = ""))
          }
          argStrings <- append (argStrings, paste ("\"", argNames[i], "\"", sep = ""))
        }
      }
      argDecl <- paste (argDecl, collapse = ", ")
      argInvoke <- paste (argNames, collapse = ", ")
      argStrings <- paste (argStrings, collapse = ", ")
      cmd <- paste (c (
        paste (name, " <<- function (", argDecl, ") {", sep = ""),
        validate,
        paste ("result <- invoke.LiveData (", index, ", list (", argInvoke, "))", sep = ""),
        "if (is.ErrorValue (result)) {",
        paste ("if (result@code == 1) stop (paste (\"Parameter '\", switch (result@index + 1, ", argStrings, "), \"' invalid - \", result@message, sep = \"\"))", sep = ""),
        paste ("if (result@code == 3) stop (paste (\"Parameter '\", switch (result@index + 1, ", argStrings, "), \"' invalid - \", result@message, sep = \"\"))", sep = ""),
        "stop (result@toString)",
        "} else result",
        "}"), sep = "\n")
      eval (parse (text = cmd))
    } else {
      LOGERROR ("Invalid parameters for", index, "argNames:", argNames, "argFlags:", argFlags)
    }
  } else {
    LOGWARN ("Invalid index", index)
  }
}

# Brings proxy declarations for all live data definitions into scope
Install.LiveData <- function () {
  LOGINFO ("Installing live data definitions")
  for (index in seq (from = 0, to = count.LiveData () - 1)) .installByIndex.LiveData (index)
}
