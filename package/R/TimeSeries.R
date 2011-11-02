##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts the transport form to a R time-series object
Interop.TimeSeries <- function (data) {
  startDate <- as.Date (data[[1]])
  dataValues <- sapply (data[2:length (data)], function (x) { if (is.null (x)) NA else x })
  ts (data = dataValues, start = startDate)
}

# Converts the start date from an R time-series to the transport form
Interop.TimeSeriesStart <- function (data) {
  toString (as.Date (start (data)[1], origin = "1970-01-01"))
}
