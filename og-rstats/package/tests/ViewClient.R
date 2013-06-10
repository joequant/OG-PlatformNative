##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the access to results from a view client

source ("TestUtil.R")

# Lookup a view; grab the first in the list
LOGDEBUG ("Views")
views <- Views ()
LOGDEBUG (views)
viewIdentifier <- views[1,1]

# Create a "run a single cycle" view client descriptor
LOGDEBUG ("StaticMarketDataViewClient")
viewDescriptor <- StaticMarketDataViewClient (viewIdentifier);
LOGDEBUG (viewDescriptor)
ASSERT (OpenGamma::is.FudgeMsg (viewDescriptor))

# Create a view client
LOGDEBUG ("ViewClient")
viewClient <- ViewClient (viewDescriptor)
LOGDEBUG (viewClient)
ASSERT (is.ViewClient (viewClient))

# Trigger a cycle
LOGDEBUG ("TriggerViewCycle")
TriggerViewCycle (viewClient)

# Get a calculation result
LOGDEBUG ("GetViewResult")
viewResult <- GetViewResult (viewClient, 90000)
LOGDEBUG (viewResult)
ASSERT (is.ViewComputationResultModel (viewResult))

# Get the results
LOGDEBUG ("results.ViewComputationResultModel")
results <- results.ViewComputationResultModel (viewResult)
ASSERT (is.list (results))
for (config in names (results)) {
  LOGDEBUG (results[[config]])
  ASSERT (is.data.frame (results[[config]]))
}
