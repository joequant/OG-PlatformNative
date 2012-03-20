##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Brings Bloomberg related definitions into scope
Install.Bloomberg <- function (stub) {
  stub.Bloomberg <- stub$begin ("Bloomberg", Category.MARKET_DATA)
  stub.Bloomberg$fromFudgeMsg ("msg[1]", "BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType")
  stub.Bloomberg$end ()
}
