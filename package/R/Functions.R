##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of functions available
count.Functions <- function () {
  OpenGammaCall ("Functions_count")
}

# Returns the name of a function
getName.Functions <- function (index) {
  OpenGammaCall ("Functions_getName", as.integer (index))
}

# Returns the parameter names for a function
getParameterNames.Functions <- function (index) {
  OpenGammaCall ("Functions_getParameterNames", as.integer (index))
}

# Returns the parameter flags for a function
getParameterFlags.Functions <- function (index) {
  OpenGammaCall ("Functions_getParameterFlags", as.integer (index))
}

# Invokes a function with the given argument array and returns the result
invoke.Functions <- function (index, args) {
  OpenGammaCall ("Functions_invoke", as.integer (index), args, parent.frame ())
}

# Brings a proxy declaration for a function into scope
.install.Functions <- function (index) {
  name <- getName.Functions (index)
  if (!is.null (name)) {
    LOGDEBUG (paste ("Found function", name))
    argNames <- getParameterNames.Functions (index)
    argFlags <- getParameterFlags.Functions (index)
    if (length (argNames) == length (argFlags)) {
      argDecl <- c ()
      validate <- c ()
      argStrings <- c ()
      if (length (argNames) > 0) {
        for (i in seq (from = 1, to = length (argNames))) {
          flagOptional <- FALSE
          flags <- argFlags[i]
          if (flags >= PARAMETER_FLAG_OPTIONAL) {
            flags <- flags - PARAMETER_FLAG_OPTIONAL
            flagOptional <- TRUE
          }
          if (flagOptional) {
            argDecl <- append (argDecl, paste (argNames[i], "= NULL"))
          } else {
            argDecl <- append (argDecl, argNames[i])
            validate <- append (validate, paste ("if (is.null (", argNames[i], ")) stop (\"Parameter '", argNames[i], "' may not be null\")", sep = ""))
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
        paste ("result <- invoke.Functions (", index, ", list (", argInvoke, "))", sep = ""),
        "if (is.ErrorValue (result)) {",
        paste ("if (result@code == 1) stop (paste (\"Parameter '\", switch (result@index + 1, ", argStrings, "), \"' invalid - \", result@message, sep = \"\"))", sep = ""),
        "stop (result@toString)",
        "} else result",
        "}"), sep = "\n")
      eval (parse (text = cmd))
    } else {
      LOGERROR (paste ("Invalid parameters for", index, "argNames:", argNames, "argFlags:", argFlags))
    }
  } else {
    LOGWARN (paste ("Invalid index", index))
  }
}

# Brings proxy declarations for all available functions into scope
install.Functions <- function () {
  LOGINFO ("Installing functions")
  for (index in seq (from = 0, to = count.Functions () - 1)) .install.Functions (index)
}
