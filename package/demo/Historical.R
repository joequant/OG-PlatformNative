##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Iterates a view over historical market data to produce timeseries for the portfolio. This
# demonstrates the functions used to perform the historical iteration; the choice of view
# and handling the data is arbitrary.

Init ()

# Find a view identifier (omit the view name to graph the first view)
viewName <- "Swap Portfolio View"
matchedViews <- Views (viewName)
if (length (matchedViews) == 0) {
  matchedViews <- Views (paste ("Example", viewName))
}
if (length (matchedViews) == 0) {
  stop ("No view called '", viewName, "' defined")
} else {
  viewIdentifier <- matchedViews[1, 1]
}

# Create a client (private process) for sampling a historic period (past 365 days in this example)
endTime <- Sys.time () - (14 * 86400)
startTime <- endTime - (365 * 86400)
viewClientDescriptor <- HistoricalMarketDataViewClient (viewIdentifier, startTime, endTime)
viewClient <- ViewClient (viewClientDescriptor, FALSE)
# Start the first cycle (historical clients start "halted" so that additional parameters can be applied
# to further peturb the historical data)
TriggerViewCycle (viewClient)

# Build the result into these vectors
presentValue <- c ()

# Iterate through the results, blocking on the first but waiting no longer than 10s for each subsequent one.
# The timeout is arbitrary to demonstrate how the functions can be used to fail rather than block
# indefinately which may (or may not) be desirable in some systems.
timeout <- 10000
result <- GetViewResult (viewClient, -1)
while (!is.null (result)) {
  # Trigger the next iteration while we work with the previous
  TriggerViewCycle (viewClient)
  print (paste ("Got cycle", viewCycleId.ViewComputationResultModel (result)))
  # Get the data from the "Default" configuration in the view
  data <- results.ViewComputationResultModel (result)$Default
  print (paste(nrow (data), "row(s) of data"))
  # Identify the row with the portfolio values (it's the only portfolio node row in our example view)
  portfolio <- data[which (data$type == "PORTFOLIO_NODE"),]
  # Identify the PV column(s)
  columns.pv <- columns.ViewComputationResultModel (data, ValueRequirementNames.Present.Value)
  # Get the PV for the portfolio
  value.pv <- firstValue.ViewComputationResultModel (portfolio, columns.pv)
  print (paste ("PV for", valuationTime.ViewComputationResultModel (result), "=", value.pv))
  presentValue <- append (presentValue, value.pv)
  # Next iteration
  print (paste ("Waiting for next cycle"))
  result <- GetViewResult (viewClient, timeout, viewCycleId.ViewComputationResultModel (result))
}

presentValue.ts <- ts (data = presentValue, start = as.Date (startTime))
print (presentValue.ts)

# Store this time series into the database (the name of the portfolio and portfolio identifier should be resolved programatically)
# StoreTimeSeries (presentValue.ts, name = "Present Value on Swap Portfolio", identifier = "DbPrt~1303", dataField = ValueRequirementNames.Present.Value, dataSource = "OPENGAMMA", dataProvider = "OPENGAMMA", observationTime = "LONDON_CLOSE", master = "GLOBAL")
