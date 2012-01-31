##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads a time-series from the database using a Bloomberg ticker

Init ()

# Fetch a timeseries
ticker.ts <- FetchTimeSeries (dataField = "CLOSE", identifier = "OG_SYNTHETIC_TICKER~USDCASHP1M")

# Convert the timeseries to an XTS object (requires XTS to already be loaded)
ticker.xts <- as.xts.TimeSeries (ticker.ts)
