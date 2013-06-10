##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the encoding and retrieval of time-series

source ("TestUtil.R")

# Fetch a time-series by unique identifier
LOGDEBUG ("FetchTimeSeries by UniqueId")
timeSeries <- FetchTimeSeries (identifier = "DbHts~1000")
LOGDEBUG (timeSeries)
ASSERT (is.ts (timeSeries))

# Fetch a time-series by identifier; try a handful of schemes - which ones work depend on the back end time series database
LOGDEBUG ("FetchTimeSeries by Identifier")
timeSeries.opengamma<- FetchTimeSeries (identifier = "OG_SYNTHETIC_TICKER~GBPSWAPP1Y", dataProvider = "OG_DATA_PROVIDER", dataSource = "OG_DATA_SOURCE", dataField = "CLOSE")
LOGDEBUG (timeSeries.opengamma)
timeSeries.bbg <- FetchTimeSeries (identifier = "BLOOMBERG_BUID~IX653295-0", dataProvider = "CME", dataSource = "BLOOMBERG", dataField = "PX_LAST")
LOGDEBUG (timeSeries.bbg)
ASSERT (is.ts (timeSeries.opengamma) || is.ts (timeSeries.bbg))

# Fetch a time-series without specifying source/provider; type a handful of schemes - which ones work depend on the back end time series database
LOGDEBUG ("FetchTimeSeries with default resolution")
timeSeries.opengamma <- FetchTimeSeries (identifier = "OG_SYNTHETIC_TICKER~GBPSWAPP1Y", dataField = "CLOSE")
LOGDEBUG (timeSeries.opengamma)
timeSeries.bbg <- FetchTimeSeries (identifier = "BLOOMBERG_TICKER~USSW1 Curncy", dataField = "PX_LAST")
ASSERT (is.ts (timeSeries.opengamma) || is.ts (timeSeries.bbg))
