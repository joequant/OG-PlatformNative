##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Returns the number of live data definitions available
count.LiveData <- function () {
  OpenGammaCall ("LiveData_count")
}

# Returns the category of a live data definition
getCategory.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getCategory", as.integer (index))
}

# Returns the description of a live data definition
getDescription.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getDescription", as.integer (index))
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

# Returns the parameter descriptions for a live data connection
getParameterDescriptions.LiveData <- function (index) {
  OpenGammaCall ("LiveData_getParameterDescriptions", as.integer (index))
}

# Connects to a live data component, returning the first result
invoke.LiveData <- function (index, args) {
  OpenGammaCall ("LiveData_invoke", as.integer (index), args, parent.frame ())
}

# TODO: the live data needs a bit more thought - return a stub object which represents the connection and
# has a method to query the latest value plus something like a "select" call which can take a set of them
# and block until at a threshold number of them have changed.

# Brings a proxy declaration for a live data definition into scope
.installByIndex.LiveData <- function (stub.LiveData, index) {
  name <- getName.LiveData (index)
  if (!is.null (name)) {
    LOGDEBUG ("Found live data", name)
    cat <- getCategory.LiveData (index)
    if (is.null (cat)) {
      cat <- ""
    }
    description <- getDescription.LiveData (index)
    if (is.null (description)) {
      description <- paste ("The", name, "live data connection.")
    }
    argNames <- getParameterNames.LiveData (index)
    argFlags <- getParameterFlags.LiveData (index)
    argDescriptions <- getParameterDescriptions.LiveData (index)
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
      body <- append (body, paste ("result <- OpenGamma:::invoke.LiveData (", index, ", list (", paste (argNames, collapse = ", "), "))", sep = ""))
      body <- append (body, paste ("if (OpenGamma:::is.ErrorValue (result)) { stop (OpenGamma:::.message.ErrorValue (result@code, .args.", name, ")) } else { result }", sep = ""))
      stub.LiveData$func (
        paste (".args", name, sep = "."),
        paste (name, "argument names"),
        "Returns the text name for the argument.",
        list (i = "The argument index"),
        paste ("switch (i, ", paste (argStrings, collapse = ", "), ")", sep = ""),
        FALSE)
      stub.LiveData$func (
        name,
        paste (cat, "live data connection"),
        description,
        params,
        paste (body, collapse = "\n"),
        FALSE)
    } else {
      LOGERROR ("Invalid parameters for", index, "argNames:", argNames, "argFlags:", argFlags)
    }
  } else {
    LOGWARN ("Invalid index", index)
  }
}

# Brings proxy declarations for all live data definitions into scope
Install.LiveData <- function (stub) {
  count <- count.LiveData ()
  LOGINFO ("Declaring", count, "live data connections")
  stub.LiveData <- stub$begin ("LiveData")
  for (index in seq (from = 0, to = count - 1)) .installByIndex.LiveData (stub.LiveData, index)
  stub.LiveData$end ()
}
