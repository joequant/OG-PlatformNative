##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads a time-series from the database using a Bloomberg ticker

# Fetch a timeseries
ticker.ts <- FetchTimeSeries (dataField = "PX_LAST", identifier = "BLOOMBERG_TICKER~US0001M Index")

# Convert the timeseries to an XTS object (requires XTS to already be loaded)
ticker.xts <- as.xts.TimeSeries (ticker.ts)
