##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings arbitrary java.lang.Number utils into scope
Install.Number <- function (stub) {
  stub.Number <- stub$begin ("Number")
  for (class in c ("Number", "Byte", "Double", "Float", "Integer", "Long", "Short")) {
    stub.Number$fromFudgeMsg ("msg$value", class)
  }
  stub.Number$end ()
}
