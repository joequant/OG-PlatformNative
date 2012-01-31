##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads the time-series for yield curve data points to construct a 3D "curve over time" graph.

Init ()

# Curve tickers
tickers <- c ("USDCASHP1D", "USDCASHP1M", "USDCASHP2M", "USDCASHP3M", "USDCASHP4M", "USDCASHP5M", "USDCASHP6M", "USDCASHP7M", "USDCASHP8M", "USDCASHP9M", "USDCASHP10M", "USDCASHP11M", "USDCASHP12M", "USDSWAPP1Y", "USDSWAPP2Y", "USDSWAPP3Y", "USDSWAPP4Y", "USDSWAPP5Y", "USDSWAPP6Y", "USDSWAPP7Y", "USDSWAPP8Y", "USDSWAPP9Y", "USDSWAPP10Y", "USDSWAPP15Y", "USDSWAPP20Y", "USDSWAPP25Y", "USDSWAPP30Y", "USDSWAPP40Y", "USDSWAPP50Y", "USDSWAPP80Y")
tickers.field <- "CLOSE"
tickers.scheme <- "OG_SYNTHETIC_TICKER"

# TODO: should query the curve definitions in the system to get these tickers

# Fetch timeseries
timeseries <- lapply (tickers, function (x) { FetchTimeSeries (dataField = tickers.field, identifier = paste (tickers.scheme, x, sep = "~")) })
# TODO: should use the range truncated form to just get a couple of years of data

# Extend start of shorter timeseries so curve starts in same place
timeseries.start <- min (sapply (timeseries, function (x) { start (x)[1] }))
timeseries <- lapply (timeseries, function (x) {
  s <- start (x)[1]
  if (s > timeseries.start) {
    c (rep (NA, times = s - timeseries.start), as.double (x))
  } else {
    as.double (x)
  }
})
timeseries.length <- max (sapply (timeseries, length))

# HACK to truncate the timeseries to 365 pts (1 year)
if (timeseries.length > 365) {
  timeseries.start <- timeseries.start + (timeseries.length - 365)
  timeseries.length <- 365
}

# Convert to a matrix - time is in rows, each column is a time-series
timeseries <- sapply (timeseries, function (x) {
  l <- length (x)
  if (l < timeseries.length) {
    c (x, rep (NA, times = timeseries.length - l))
  } else {
    # HACK to truncate the timeseries
    tail (x, timeseries.length)
  }
})

# Convert to XTS timeseries
timeseries.dates <- as.Date (sapply (index (timeseries), function (x) { x + timeseries.start }), origin = "1970-01-01")
timeseries <- xts (timeseries, order.by = timeseries.dates)
colnames (timeseries) <- tickers
