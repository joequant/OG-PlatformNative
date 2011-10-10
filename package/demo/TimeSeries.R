##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Load a time-series from the database using a Bloomberg ticker
ts <- FetchTimeSeries (dataField = "PX_LAST", identifier = "BLOOMBERG_TICKER~US0001M Index")
