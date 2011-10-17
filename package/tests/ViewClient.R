##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the access to results from a view client

source ("TestUtil.R")

# Lookup a view
LOGDEBUG ("Views")
views <- Views ("Equity Option Test View 1")
LOGDEBUG (views)
viewIdentifier <- views[1,1]
ASSERT (views[1,2] == "Equity Option Test View 1")

# Create a "run a single cycle" view client descriptor
LOGDEBUG ("StaticMarketDataViewClient")
viewDescriptor <- StaticMarketDataViewClient (viewIdentifier);
LOGDEBUG (viewDescriptor)
ASSERT (is.character (viewDescriptor))

# Create a view client
LOGDEBUG ("ViewClient")
viewClient <- ViewClient (viewDescriptor)
LOGDEBUG (viewClient)
ASSERT (is.ViewClient (viewClient))

# Get a calculation result
LOGDEBUG ("GetViewResult")
viewResult <- GetViewResult (viewClient, 30000)
LOGDEBUG (viewResult)
ASSERT (is.ViewComputationResultModel (viewResult))

# Get the results
LOGDEBUG ("results.ViewComputationResultModel")
results <- results.ViewComputationResultModel (viewResult)
ASSERT (is.list (results))
LOGDEBUG (results$Default)
ASSERT (is.data.frame (results$Default))
