##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the encoding and retrieval of time-series

source ("TestUtil.R")

# Fetch a time-series

LOGDEBUG ("FetchTimeSeries by UniqueId")
timeSeries <- FetchTimeSeries (identifier = "DbHts~1000")
LOGDEBUG (timeSeries)
ASSERT (is.ts (timeSeries))

LOGDEBUG ("FetchTimeSeries by Identifier")
timeSeries <- FetchTimeSeries (identifier = "BLOOMBERG_BUID~IX653295-0", dataProvider = "CME", dataSource = "BLOOMBERG", dataField = "PX_LAST")
LOGDEBUG (timeSeries)
ASSERT (is.ts (timeSeries))

