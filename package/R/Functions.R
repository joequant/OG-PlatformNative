##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

Functions_count <- function () {
  OpenGammaCall ("Functions_count")
}

Functions_getName <- function (index) {
  OpenGammaCall ("Functions_getName", as.integer (index))
}

Functions_getParameterNames <- function (index) {
  OpenGammaCall ("Functions_getParameterNames", as.integer (index))
}

Functions_getParameterFlags <- function (index) {
  OpenGammaCall ("Functions_getParameterFlags", as.integer (index))
}

Functions_invoke <- function (index, args) {
  OpenGammaCall ("Functions_invoke", as.integer (index), args)
}

Functions_installImpl <- function (index) {
  name <- Functions_getName (index)
  if (!is.null (name)) {
    LOGDEBUG (paste ("Found function", name))
    argNames <- Functions_getParameterNames (index)
    argFlags <- Functions_getParameterFlags (index)
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
        paste ("result <- Functions_invoke (", index, ", list (", argInvoke, "))", sep = ""),
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

Functions_install <- function () {
  LOGINFO ("Installing function")
  for (index in seq (from = 0, to = Functions_count () - 1)) Functions_installImpl (index)
}
