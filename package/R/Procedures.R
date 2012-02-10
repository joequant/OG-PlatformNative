##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of procedures available
count.Procedures <- function () {
  OpenGammaCall ("Procedures_count")
}

# Returns the category of an available procedure
getCategory.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getCategory", as.integer (index))
}

# Returns the description of an available procedure
getDescription.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getDescription", as.integer (index))
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

# Returns the parameter descriptions for a procedure
getParameterDescriptions.Procedures <- function (index) {
  OpenGammaCall ("Procedures_getParameterDescriptions", as.integer (index))
}

# Invokes a procedure with the given argument array and returns the result
invoke.Procedures <- function (index, args) {
  OpenGammaCall ("Procedures_invoke", as.integer (index), args, parent.frame ())
}

# Brings a proxy declaration for a procedure into scope
.installByIndex.Procedures <- function (stub.Procedures, index) {
  name <- getName.Procedures (index)
  if (!is.null (name)) {
    LOGDEBUG ("Found procedure", name)
    cat <- getCategory.Procedures (index)
    if (is.null (cat)) {
      cat <- ""
    }
    description <- getDescription.Procedures (index)
    if (is.null (description)) {
      description <- paste ("The", name, "procedure.")
    }
    argNames <- getParameterNames.Procedures (index)
    argFlags <- getParameterFlags.Procedures (index)
    argDescriptions <- getParameterDescriptions.Procedures (index)
    if ((length (argNames) == length (argFlags)) && (length (argNames) == length (argDescriptions))) {
      params <- list ()
      argStrings <- c ()
      body <- c ()
      if (length (argNames) > 0) {
        for (i in seq (from = 1, to = length (argNames))) {
          argDescription <- argDescriptions[i]
          if (is.na (argDescription)) {
            argDescription <- paste ("Parameter", i)
          }
          flagOptional <- FALSE
          flags <- argFlags[i]
          if (flags >= PARAMETER_FLAG_OPTIONAL) {
            flags <- flags - PARAMETER_FLAG_OPTIONAL
            flagOptional <- TRUE
          }
          if (flagOptional) {
            params[[argNames[i]]] <- paste ("?", argDescription, sep = "")
          } else {
            params[[argNames[i]]] <- argDescription
            body <- append (body, paste ("if (missing (", argNames[i], ") || is.null (", argNames[i], ")) stop (paste (\"Parameter '\", ", paste (".args", name, sep = "."), " (", i, "), \"' may not be null\", sep = \"\"))", sep = ""))
          }
          argStrings <- append (argStrings, paste ("\"", argNames[i], "\"", sep = ""))
        }
      }
      body <- append (body, paste ("result <- OpenGamma:::invoke.Procedures (", index, ", list (", paste (argNames, collapse = ", "), "))", sep = ""))
      body <- append (body, paste ("if (OpenGamma:::is.ErrorValue (result)) { stop (OpenGamma:::.message.ErrorValue (result, .args.", name, ")) } else { invisible (result) }", sep = ""))
      stub.Procedures$func (
        paste (".args", name, sep = "."),
        paste (name, "argument names"),
        "Returns the text name for the argument.",
        list (i = "The argument index"),
        paste ("switch (i, ", paste (argStrings, collapse = ", "), ")", sep = ""),
        FALSE)
      stub.Procedures$func (
        name,
        paste (cat, "procedure"),
        description,
        params,
        paste (body, collapse = "\n"),
        FALSE)
      c (index, name, argNames, toString (argFlags))
    } else {
      LOGERROR ("Invalid parameters for", index, "argNames:", argNames, "argFlags:", argFlags)
      c ()
    }
  } else {
    LOGWARN ("Invalid index", index)
    c ()
  }
}

# Brings proxy declarations for all available procedures into scope
Install.Procedures <- function (stub) {
  count <- count.Procedures ()
  LOGINFO ("Declaring", count, "procedures")
  stub.Procedures <- stub$begin ("Procedures")
  stub.Procedures$metadata ("count", count)
  description <- c ()
  for (index in seq (from = 0, to = count - 1)) {
    description <- c (description, .installByIndex.Procedures (stub.Procedures, index))
  }
  stub.Procedures$metadata ("description", description)
  stub.Procedures$end ()
}

# Checks the proxy declarations for the procedures match those currently exported
Verify.Procedures <- function () {
  server.count <- count.Procedures ()
  og.count <- OG:::.count.Procedures
  if (server.count == og.count) {
    server <- c ()
    for (index in seq (from = 0, to = server.count - 1)) {
      server <- c (server, index, getName.Procedures (index), getParameterNames.Procedures (index), toString (getParameterFlags.Procedures (index)))
    }
    og <- OG:::.description.Procedures
    if (length (server) == length (og)) {
      if (length (which ((server == og) == FALSE)) == 0) {
        LOGDEBUG ("Procedure metadata matches current Java stack")
        TRUE
      } else {
        LOGDEBUG ("Procedure metadata differs")
        FALSE
      }
    } else {
      LOGDEBUG ("Procedure metadata length differs, expected", length (server), "found", length (og))
      FALSE
    }
  } else {
    LOGDEBUG ("Different number of procedures in stub library, expected", server.count, "found", og.count)
    FALSE
  }
}
