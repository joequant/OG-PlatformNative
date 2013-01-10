##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads a time-series from the database using a ticker

Init ()

# Fetch a timeseries
ticker.id <- "OG_SYNTHETIC_TICKER~USDCASHP1M"
ticker.ts <- FetchTimeSeries (dataField = "CLOSE", identifier = ticker.id)
if (is.null (ticker.ts)) {
  ticker.id <- "BLOOMBERG_TICKER~US0001M Index"
  ticker.ts <- FetchTimeSeries (dataField = "PX_LAST", identifier = ticker.id)
  if (is.null (ticker.ts)) {
    stop ("Time series '", ticker.id, "' not found")
  }
}

# Convert the timeseries to an XTS object (requires XTS to already be loaded)
ticker.xts <- as.xts.TimeSeries (ticker.ts)
