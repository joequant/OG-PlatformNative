##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings declarations for ViewClient into scope
Install.ViewClient <- function (stub) {
  stub.ViewClient <- stub$begin ("ViewClient")
  object.ExternalRef (stub.ViewClient)
  stub.ViewClient$end ()
}
