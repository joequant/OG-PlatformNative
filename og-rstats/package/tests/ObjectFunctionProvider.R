##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests a family of methods exported from OG-Language/ObjectFunctionProvider

source ("TestUtil.R")

# Object creation
LOGDEBUG ("EquitySecurity")
sec <- EquitySecurity (name = "Foo", exchange = "Exchange", exchangeCode = "EXCH", companyName = "Bar", currency = "USD")
LOGDEBUG (sec)
ASSERT_EQUAL (OpenGamma::displayName.FudgeMsg (sec), "EquitySecurity")

# Attribute get
LOGDEBUG ("GetEquitySecurityExchange")
exch <- GetEquitySecurityExchange (sec)
LOGDEBUG (exch)
ASSERT_EQUAL (exch, "Exchange")

# Attribute set
LOGDEBUG ("SetEquitySecurityCurrency")
sec <- SetEquitySecurityCurrency (sec, "GBP")
LOGDEBUG (sec)
ASSERT_EQUAL (GetEquitySecurityCurrency (sec), "GBP")

# Object expansion
LOGDEBUG ("ExpandEquitySecurity")
expand <- ExpandEquitySecurity (sec)
LOGDEBUG (expand)
