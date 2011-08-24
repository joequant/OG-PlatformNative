##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts the transport form to a R time-series object
interop.TimeSeries <- function (data) {
  startDate <- as.Date (data[[1]])
  dataValues <- sapply (data[2:length (data)], function (x) { if (is.null (x)) NA else x })
  ts (data = dataValues, start = startDate)
}
