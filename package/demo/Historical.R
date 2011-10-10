##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Iterates a view over historical market data to produce timeseries for the portfolio. This
# demonstrates the functions used to perform the historical iteration; the choice of view
# and handling the data is arbitrary.

# Find a view identifier
viewName <- "Simple Swap Test View"
viewIdentifier <- Views (viewName)[[1]][[1]]

# Create a client (private process) for sampling a historic period (past 365 days in this example)
endTime <- Sys.time ()
startTime <- endTime - (365 * 86400)
viewClientDescriptor <- HistoricalMarketDataViewClient (viewIdentifier, startTime, endTime)
viewClient <- ViewClient (viewClientDescriptor, FALSE)

# Build the result into these vectors
presentValue <- c ()

# Iterate through the results, blocking on the first but waiting no longer than 10s for each subsequent one
timeout <- 10000
result <- GetViewResult (viewClient, -1)
while (!is.null (result)) {
  # Trigger the next iteration while we work with the previous
  TriggerViewCycle (viewClient)
  print (paste ("Got cycle", viewCycleId.ViewComputationResultModel (result)))
  # Get the data from the "Default" configuration in the view
  data <- results.ViewComputationResultModel (result)$Default
  print (paste(length (data), "row(s) of data"))
  # Identify the PV column(s)
  columns <- colnames (data)
  columns.presentValue <- columns[which (substr (columns, 1, 14) == "Present.Value.")]
  # Identify the row with the portfolio values (its the only portfolio node row in our example view)
  portfolio <- data[which (data$type == "PORTFOLIO_NODE"),]
  # Get the PV for this iteration
  if (length (columns.presentValue) > 0) {
    columns.presentValue <- head (columns.presentValue[sapply (columns.presentValue, function (x) { !is.na (portfolio[[x]]) })], 1)
    if (length (columns.presentValue) > 0) {
      value.presentValue <- portfolio[[columns.presentValue]]
    } else {
      value.presentValue <- NA
    }
  } else {
    value.presentValue <- NA
  }
  print (paste ("PV for", valuationTime.ViewComputationResultModel (result), "=", value.presentValue))
  presentValue <- append (presentValue, value.presentValue)
  # Next iteration
  print (paste ("Waiting for next cycle"))
  result <- GetViewResult (viewClient, timeout, viewCycleId.ViewComputationResultModel (result))
}

presentValue.ts <- ts (data = presentValue)
print (presentValue.ts)
