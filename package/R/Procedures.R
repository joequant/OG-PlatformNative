##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of procedures available
count.Procedures <- function () {
  OpenGammaCall ("Procedures_count")
}

# Returns the name of an available procedure
getName.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getName", as.integer (index))
}

# Returns the parameter names for a procedure
getParameterNames.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getParameterNames", as.integer (index))
}

# Returns the parameter flags for a procedure
getParameterFlags.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getParameterFlags", as.integer (index))
}

# Invokes a procedure with the given argument array and returns the result
invoke.Procedures <- function (index, args) {
  OpenGammaCall ("Procedures_invoke", as.integer (index), args, parent.frame ())
}

# Brings a proxy declaration for a procedure into scope
.installByIndex.Procedures <- function (index) {
  name <- getName.Procedures (index)
  if (!is.null (name)) {
    LOGDEBUG (paste ("Found procedure", name))
    argNames <- getParameterNames.Procedures (index)
    argFlags <- getParameterFlags.Procedures (index)
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
        paste ("result <- invoke.Procedures (", index, ", list (", argInvoke, "))", sep = ""),
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

# Brings proxy declarations for all available procedures into scope
Install.Procedures <- function () {
  LOGINFO ("Installing procedures")
  for (index in seq (from = 0, to = count.Procedures () - 1)) .installByIndex.Procedures (index)
}
