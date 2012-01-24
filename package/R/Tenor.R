##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings Tenor definitions into scope
Install.Tenor <- function (stub) {
  stub.Tenor <- stub$begin ("Tenor")
  stub.Tenor$fromFudgeMsg ("msg$tenor")
  stub.Tenor$end ()
}
