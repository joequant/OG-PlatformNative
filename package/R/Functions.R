##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

Functions_count <- function () {
  .Call ("Functions_count")
}

Functions_getName <- function (index) {
  .Call ("Functions_getName", as.integer (index))
}

Functions_getParameterNames <- function (index) {
  .Call ("Functions_getParameterNames", as.integer (index))
}

Functions_getParameterFlags <- function (index) {
  .Call ("Functions_getParameterFlags", as.integer (index))
}

Functions_invoke <- function (index, args) {
  .Call ("Functions_invoke", as.integer (index), args)
}

Functions_installImpl <- function (index) {
  name <- Functions_getName (index)
  if (!is.null (name)) {
    LOGDEBUG (paste ("Found function", name))
    argNames <- Functions_getParameterNames (index)
    argFlags <- Functions_getParameterFlags (index)
    if (length (argNames) == length (argFlags)) {
      argDecl <- c ()
      if (length (argNames) > 0) {
        for (i in seq (from = 1, to = length (argNames))) {
          flagOptional <- FALSE
          flags <- argFlags[i]
          if (flags > PARAMETER_FLAG_OPTIONAL) {
            flags <- flags - PARAMETER_FLAG_OPTIONAL
            flagOptional <- TRUE
          }
          if (flagOptional) {
            argDecl <- append (argDecl, paste (argNames[i], "= NULL"))
          } else {
            argDecl <- append (argDecl, argNames[i])
          }
        }
      }
      argDecl <- paste (argDecl, sep = ", ")
      argInvoke <- paste (argNames, sep = ", ")
      cmd <- paste (name, " <<- function (", argDecl, ") Functions_invoke (", index, ", list (", argInvoke, "))", sep = "")
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
