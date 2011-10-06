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

# Create a client (private process) for sampling a historic period (past 30 days in this example)
endTime <- Sys.time ()
startTime <- endTime - (30 * 86400)
viewClientDescriptor <- SampleMarketDataViewClient (viewIdentifier, startTime, endTime)
viewClient <- ViewClient (viewClientDescriptor, FALSE)

# Build the result into these vectors
pv01 <- c ()

# Iterate through the results, waiting no longer than 10s for each one
timeout <- 10000
result <- GetViewResult (viewClient, timeout)
while (!is.null (result)) {
  # Trigger the next iteration while we work with the previous
  TriggerViewCycle (viewClient)
  print (paste ("Got cycle", viewCycleId.ViewComputationResultModel (result)))
  # Get the data from the "Default" configuration in the view
  data <- results.ViewComputationResultModel (result)$Default
  print (paste(length (data), "row(s) of data"))
  # Identify the PV01 column(s)
  columns <- colnames (data)
  columns.pv01 <- columns[which (substr (columns, 1, 5) == "PV01.")]
  # Identify the row with the portfolio values (its the only portfolio node row in our example view)
  portfolio <- data[which (data$type == "PORTFOLIO_NODE"),]
  # Get the PV01 for this iteration
  if (length (columns.pv01) > 0) {
    columns.pv01 <- head (columns.pv01[sapply (columns.pv01, function (x) { !is.na (portfolio[[x]]) })], 1)
    if (length (columns.pv01) > 0) {
      value.pv01 <- portfolio[[columns.pv01]]
    } else {
      value.pv01 <- NA
    }
  } else {
    value.pv01 <- NA
  }
  print (paste ("PV01 for", valuationTime.ViewComputationResultModel (result), "=", value.pv01))
  pv01 <- append (pv01, value.pv01)
  # Next iteration
  print (paste ("Waiting for next cycle"))
  result <- GetViewResult (viewClient, timeout, viewCycleId.ViewComputationResultModel (result))
}

pv01 <- ts (data = pv01, start = startTime)
print (pv01)
