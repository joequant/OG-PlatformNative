##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Loads the time-series for yield curve data points to construct a 3D "curve over time" graph.

Init ()

# Curve tickers
if (!is.null (FetchTimeSeries (dataField = "CLOSE", identifier = "OG_SYNTHETIC_TICKER~USDCASHP1D", maxPoints = 1))) {
  tickers <- c ("USDCASHP1D", "USDCASHP1M", "USDCASHP2M", "USDCASHP3M", "USDCASHP4M", "USDCASHP5M", "USDCASHP6M", "USDCASHP7M", "USDCASHP8M", "USDCASHP9M", "USDCASHP10M", "USDCASHP11M", "USDCASHP12M", "USDSWAPP2Y", "USDSWAPP3Y", "USDSWAPP4Y", "USDSWAPP5Y", "USDSWAPP6Y", "USDSWAPP7Y", "USDSWAPP8Y", "USDSWAPP9Y", "USDSWAPP10Y", "USDSWAPP12Y", "USDSWAPP15Y", "USDSWAPP20Y", "USDSWAPP25Y", "USDSWAPP30Y", "USDSWAPP40Y")
  tickers.field <- "CLOSE"
  tickers.scheme <- "OG_SYNTHETIC_TICKER"
} else {
  if (!is.null (FetchTimeSeries (dataField = "PX_LAST", identifier = "BLOOMBERG_TICKER~US0001W Index", maxPoints = 1))) {
    tickers <- c ("US0001W Index", "US0001M Index", "US0002M Index", "USSW2 Curncy", "USSW3 Curncy", "USSW4 Curncy", "USSW5 Curncy", "USSW6 Curncy", "USSW7 Curncy", "USSW8 Curncy", "USSW9 Curncy", "USSW10 Curncy", "USSW15 Curncy", "USSW20 Curncy", "USSW25 Curncy", "USSW30 Curncy")
    tickers.field <- "PX_LAST"
    tickers.scheme <- "BLOOMBERG_TICKER"
  } else {
    stop ("Can't find time series tickers")
  }
}

# TODO: should query the curve definitions in the system to get these tickers

# Fetch timeseries
timeseries <- lapply (tickers, function (x) {
  x.ts <- FetchTimeSeries (dataField = tickers.field, identifier = paste (tickers.scheme, x, sep = "~"))
  if (is.null (x.ts)) {
    stop ("Time series '", x, "' not found")
  }
  x.ts
})
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
if (require ("zoo")) {
  timeseries.dates <- as.Date (sapply (index (timeseries), function (x) { x + timeseries.start }), origin = "1970-01-01")
  if (require ("xts")) {
    timeseries <- xts (timeseries, order.by = timeseries.dates)
    colnames (timeseries) <- tickers
  }
}
