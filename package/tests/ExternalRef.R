##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Test external reference values

source ("TestUtil.R")

LOGDEBUG ("as.Intern")
foo <- OpenGamma:::as.ExternalRef ("Foo", "destroy.Foo")
ASSERT_EQUAL (OpenGamma:::from.ExternalRef (foo), "Foo")
LOGDEBUG ("destroy")
foo <- 0
gc ()
