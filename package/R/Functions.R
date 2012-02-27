##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of functions available
count.Functions <- function () {
  OpenGammaCall ("Functions_count")
}

# Returns the category of a function
getCategory.Functions <- function (index) {
  OpenGammaCall ("Functions_getCategory", as.integer (index))
}

# Returns the description of a function
getDescription.Functions <- function (index) {
  OpenGammaCall ("Functions_getDescription", as.integer (index))
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

# Returns the parameter descriptions for a function
getParameterDescriptions.Functions <- function (index) {
  OpenGammaCall ("Functions_getParameterDescriptions", as.integer (index))
}

# Invokes a function with the given argument array and returns the result
invoke.Functions <- function (index, args) {
  OpenGammaCall ("Functions_invoke", as.integer (index), args, parent.frame ())
}

# Looks up a function index by name
find.Functions <- function (name) {
  count <- count.Functions ()
  result <- -1
  for (i in seq (from = 0, to = count - 1)) {
    n <- getName.Functions (i)
    if (n == name) {
      result <- i
      break
    }
  }
  result
}

# Brings a proxy declaration for a function into scope
.installByIndex.Functions <- function (stub.Functions, index) {
  name <- getName.Functions (index)
  if (!is.null (name)) {
    LOGDEBUG ("Found function", name)
    cat <- getCategory.Functions (index)
    if (is.null (cat)) {
      cat <- ""
    }
    description <- getDescription.Functions (index)
    if (is.null (description)) {
      description <- paste ("The", name, "function.")
    }
    argNames <- getParameterNames.Functions (index)
    argFlags <- getParameterFlags.Functions (index)
    argDescriptions <- getParameterDescriptions.Functions (index)
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
      body <- append (body, paste ("result <- OpenGamma:::invoke.Functions (", index, ", list (", paste (argNames, collapse = ", "), "))", sep = ""))
      body <- append (body, paste ("if (OpenGamma:::is.ErrorValue (result)) { stop (OpenGamma:::.message.ErrorValue (result, .args.", name, ")) } else { result }", sep = ""))
      stub.Functions$func (
        paste (".args", name, sep = "."),
        paste (name, "argument names"),
        "Returns the text name for the argument.",
        list (i = "The argument index"),
        paste ("switch (i, ", paste (argStrings, collapse = ", "), ")", sep = ""),
        FALSE)
      stub.Functions$func (
        name,
        paste (cat, "function"),
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

# Brings proxy declarations for all available functions into scope
Install.Functions <- function (stub) {
  count <- count.Functions ()
  LOGINFO ("Declaring", count, "functions")
  stub.Functions <- stub$begin ("Functions")
  stub.Functions$metadata ("count", toString (count))
  description <- c ()
  for (index in seq (from = 0, to = count - 1)) {
    description <- c (description, .installByIndex.Functions (stub.Functions, index))
  }
  stub.Functions$metadata ("description", description)
  stub.Functions$end ()
}

# Checks the proxy declarations for the functions match those currently exported
Verify.Functions <- function () {
  server.count <- count.Functions ()
  og.count <- OG:::.count.Functions
  if (server.count == og.count) {
    server <- c ()
    for (index in seq (from = 0, to = server.count - 1)) {
      server <- c (server, index, getName.Functions (index), getParameterNames.Functions (index), toString (getParameterFlags.Functions (index)))
    }
    og <- OG:::.description.Functions
    if (length (server) == length (og)) {
      if (length (which ((server == og) == FALSE)) == 0) {
        LOGDEBUG ("Function metadata matches current Java stack")
        TRUE
      } else {
        LOGDEBUG ("Function metadata differs")
        FALSE
      }
    } else {
      LOGDEBUG ("Function metadata length differs, expected", length (server), "found", length (og))
      FALSE
    }
  } else {
    LOGDEBUG ("Different number of functions in stub library, expected", server.count, "found", og.count)
    FALSE
  }
}
