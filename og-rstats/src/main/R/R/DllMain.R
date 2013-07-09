##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Creates the license file within the package
.license.create.package <- function (dir) {
  f <- file (file.path (dir, "LICENSE"), "wt")
  cat ("Please refer to your OpenGamma distribution for license terms", file = f)
  close (f)
}

# Creates the description file within the package
.description.create.package <- function (dir) {
  f <- file (file.path (dir, "DESCRIPTION"), "wt")
  i <- packageDescription ("OpenGamma")
  cat (
    "Package: OG",
    paste ("Version:", i$Version),
    paste ("Date:", Sys.Date ()),
    paste ("Title:", i$Title),
    paste ("Author:", i$Author),
    paste ("Maintainer:", i$Maintainer),
    "Enhances: OpenGamma",
    "Description: Stub library exposing definitions from the OpenGamma module",
    "License: file LICENSE",
    file = f,
    sep = "\n")
  close (f)
}

# Creates the namespace file within the package
.namespace.create.package <- function (dir) {
  f <- file (file.path (dir, "NAMESPACE"), "wt")
  cat (
    "import (OpenGamma)",
    "exportPattern (\"^[^\\\\.]\")",
    file = f,
    sep = "\n")
  close (f)
}

# Creates the man folder within the package
.man.create.package <- function (dir) {
  man <- file.path (dir, "man")
  dir.create (man)
  man
}

# Creates the R folder within the package
.r.create.package <- function (dir) {
  r <- file.path (dir, "R")
  dir.create (r)
  r
}

# Creates the tests folder within the package
.tests.create.package <- function (dir) {
  tests <- file.path (dir, "tests")
  dir.create (tests)
  f <- file (file.path (tests, "Nil.R"), "wt")
  cat (
    "TRUE",
    file = f,
    sep = "\n")
  close (f)
  tests
}

# Escapes a string for use in the Rd format
.escape.Rd <- function (str) {
  OpenGammaCall ("String_escape", str, "\\%")
}

# Creates the outline package structure
.create.package <- function (dir) {
  info <- list ()
  .license.create.package (dir)
  .description.create.package (dir)
  .namespace.create.package (dir)
  info$man <- .man.create.package (dir)
  info$R <- .r.create.package (dir)
  info$tests <- .tests.create.package (dir)
  info$begin <- function (module, classification) {
    info$module <- module
    info$src <- file (file.path (info$R, paste (module, "R", sep = ".")), "wt")
    info$setClass <- function (rep) {
      cat (
        paste ("setClass (\"", info$module, "\", representation (", paste (sapply (names (rep), function (x) { paste (x, " = \"", rep[x], "\"", sep = "") }), collapse = ", "), "))\n", sep = ""),
        file = info$src,
        sep = "")
    }
    info$setMethod <- function (name, def) {
      cat (
        paste ("setMethod (\"", name, "\", signature = \"", info$module, "\", definition = ", def, ")\n", sep = ""),
        file = info$src,
        sep = "")
    }
    info$func <- function (name, title, descr, params, body, suffix = TRUE) {
      if (suffix) {
        x <- paste (name, module, sep = ".")
      } else {
        x <- name
      }
      cat (
        paste (x, " <- function (", paste (sapply (names (params), function (x) {
          if (substring (params[x], 1, 1) == "?") {
            paste (x, "NULL", sep = " = ")
          } else {
            x
          }
        }), collapse = ", "), ") {", sep = ""),
        body,
        "}",
        file = info$src,
        sep = "\n")
      man <- file (file.path (info$man, paste (x, "Rd", sep = ".")), "wt")
      cat (
        paste ("\\name{", x, "}%", classification, sep = ""),
        paste ("\\alias{", x, "}", sep = ""),
        paste ("\\title{", .escape.Rd (title), "}", sep = ""),
        paste ("\\description{", .escape.Rd (descr), "}", sep = ""),
        "\\arguments{",
        paste (sapply (names (params), function (x) {
          if (substring (params[[x]], 1, 1) == "?") {
            d <- substring (params[[x]], 2)
            meta <- "%optional"
          } else {
            d <- params[[x]]
            meta <- ""
          }
          paste ("\\item{", x, "}{", .escape.Rd (d), "}", meta, sep = "")
        }), collapse = "\n"),
        "}",
        file = man,
        sep = "\n")
      close (man)
    }
    info$const <- function (name, title, descr, defn, reverse = FALSE) {
      if (reverse) {
        x <- paste (module, name, sep = ".")
      } else {
        x <- paste (name, module, sep = ".")
      }
      cat (
        paste (x, " <- ", defn, "\n", sep = ""),
        file = info$src,
        sep = "")
      man <- file (file.path (info$man, paste (x, "Rd", sep = ".")), "wt")
      cat (
        paste ("\\name{", x, "}%CONST_", classification, sep = ""),
        paste ("\\alias{", x, "}", sep = ""),
        paste ("\\title{", .escape.Rd (title), "}", sep = ""),
        paste ("\\description{", .escape.Rd (descr), "}", sep = ""),
        file = man,
        sep = "\n")
      close (man)
    }
    info$asDataFrame <- function (body) {
      info$func (
        "as.data.frame",
        paste (module, "to data frame conversion"),
        paste ("Returns a data frame representation of a", module, "object"),
        list (x = "The object to convert",
              row.names = "See as.data.frame method for details",
              optional = "See as.data.frame method for details",
              "..." = "Ignored"),
        body)
    }
    info$interop <- function (body, className = module) {
      info$func (
        paste ("Interop", className, sep = "."),
        "Internal conversion function",
        "Converts a transport representation of the data to an R object instance.",
        list (data = "The transport object"),
        body,
        FALSE)
    }
    info$encode <- function (body, className = module) {
      info$func (
        paste ("Encode", className, sep = "."),
        "Internal conversion function",
        "Converts an R object instance to a transport representation.",
        list (data = "The R object"),
        body,
        FALSE)
    }
    info$fromFudgeMsg <- function (body, className = module) {
      info$func (
        paste ("fromFudgeMsg", className, sep = "."),
        "Internal conversion function",
        "Converts a Fudge message representation to an R object instance.",
        list (msg = "The Fudge message"),
        body,
        FALSE)
    }
    info$metadata <- function (name, value, className = module) {
      cat (
        paste (".", name, ".", className, " <- c(", sep = ""),
        paste ("\"", value, "\"", sep = "", collapse = ", "),
        ")\n",
        file = info$src,
        sep = "")
    }
    info$end <- function () {
      close (info$src)
    }
    info
  }
  info
}

# Creates the stub import package
.build.package <- function (dir) {
  LOGINFO ("Building import package")
  stub <- .create.package (dir)
  LOGDEBUG ("Declaring core objects")
  Install.Functions (stub)
  Install.LiveData (stub)
  Install.Procedures (stub)
  LOGDEBUG ("Declaring local data bindings")
  Install.Array (stub)
  Install.Bloomberg (stub)
  Install.Currency (stub)
  Install.DoubleLabelledMatrix2D (stub)
  Install.MarketDataRequirementNames (stub)
  Install.MarketDataSnapshot (stub)
  Install.NotCalculatedSentinel (stub)
  Install.Number (stub)
  Install.ObjectsPair (stub)
  Install.PDEResults (stub)
  Install.Tenor (stub)
  Install.TimeSeries (stub)
  Install.ValueProperties (stub)
  Install.ValueRequirementNames (stub)
  Install.ViewClient (stub)
  Install.ViewComputationResultModel (stub)
  Install.VolatilityCubeSnapshot (stub)
  Install.VolatilitySurfaceSnapshot (stub)
  Install.YieldCurveSnapshot (stub)
}

# Creates and installs the stub import package
.install.package <- function () {
  tmp <- file.path (tempdir (), "package")
  dir.create (tmp)
  .build.package (tmp)
  LOGINFO ("Installing import package", tmp)
  # Clear the R_TESTS environment variable
  test <- Sys.getenv ("R_TESTS")
  if (!is.na (test)) {
    Sys.unsetenv ("R_TESTS")
  }
  # Silent package install
  silent <- !environmentIsLocked (baseenv ())
  if (silent) {
    assign ("system.default", base::system, baseenv ())
    assign ("system.quiet", function (...) { system.default (ignore.stdout = TRUE, ...) }, baseenv ())
    try (assignInNamespace ("system", system.quiet, "base", baseenv()), silent = TRUE)
  }
  install.packages (pkgs = tmp, repos = NULL, type = "source", INSTALL_opts = "--no-multiarch")
  if (silent) {
    try (assignInNamespace ("system", system.default, "base"), silent = TRUE)
  }
  # Restore the environment
  if (!is.na (test)) {
    Sys.setenv ("R_TESTS" = test)
  }
}

# Looks up the details for the stub import package
.og.package <- function () {
  w <- getOption ("warn")
  options(warn = -1)
  og <- packageDescription ("OG")
  options (warn = w)
  og
}

# Removes the stub import package
.destroy.package <- function () {
  og <- .og.package ()
  if (is.list (og)) {
    LOGINFO ("Deleting stub library")
    pkg <- strsplit (attr (og, "file"), .Platform$file.sep)[[1]]
    pkg <- paste (head (pkg, length (pkg) - 2), collapse = .Platform$file.sep)
    LOGDEBUG ("Deleting folder", pkg)
    unlink (pkg, recursive = TRUE)
  }
}

# Loads the OpenGamma native code package and brings the functions defined in the Java stack into scope
.onLoad <- function (libname, pkgname) {
  LOGINFO ("Loading OpenGamma namespace")
  library.dynam ("OpenGamma", pkgname, .libPaths ())
  if ("OG" %in% loadedNamespaces ()) {
    LOGDEBUG ("Flagging OG as pre-loaded")
    OpenGammaCall ("DllMain_setPreload")
  }
  if (OpenGammaCall ("DllMain_check")) {
    LOGINFO ("OpenGamma namespace loaded")
  } else {
    LOGFATAL ("Could not initialise the OpenGamma R plugin. Is the service running? Is the back-end OpenGamma server available?")
  }
}

# Initialises the OpenGamma package(s)
Init <- function (cached.stub = getOption ("opengamma.cache.stub")) {
  init <- TRUE
  if ("OG" %in% loadedNamespaces ()) {
    if (OpenGammaCall ("DllMain_isPreload")) {
      LOGINFO ("OpenGamma namespaces pre-loaded")
      unloadNamespace ("OG")
    } else {
      LOGINFO ("OpenGamma namespaces already initialised")
      init <- FALSE
    }
  }
  if (init) {
    og <- .og.package ()
    if (!is.list (og)) {
      LOGINFO ("Stub OG package not available")
      cached.stub <- FALSE
    }
    if (is.null (cached.stub)) {
      opengamma.built <- strsplit (packageDescription ("OpenGamma")$Built, ";")[[1]][3]
      og.built <- strsplit (packageDescription ("OG")$Built, ";")[[1]][3]
      if (opengamma.built <= og.built) {
        cached.stub <- TRUE
        if (!Verify.Functions ()) {
          LOGINFO ("Stub Functions invalid")
          cached.stub <- FALSE
        }
        if (!Verify.LiveData ()) {
          LOGINFO ("Stub LiveData invalid")
          cached.stub <- FALSE
        }
        if (!Verify.Procedures ()) {
          LOGINFO ("Stub Procedures invalid")
          cached.stub <- FALSE
        }
      } else {
        LOGINFO ("Stub OG package older than main OpenGamma package")
        cached.stub <- FALSE
      }
    }
    if (!cached.stub) {
      .install.package ()
    }
    require ("OG")
  } else {
    TRUE
  }
}

# Makes a call into the native code package
OpenGammaCall <- function (method, ...) {
  .Call (method, ..., PACKAGE = "OpenGamma")
}

# Unloads the OpenGamma native code package
.Last.lib <- function (path) {
  library.dynam.unload ("OpenGamma", path)
  .destroy.package ()
}
