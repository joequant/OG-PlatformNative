##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Iterates a view over historical market data to produce timeseries for the portfolio. This
# demonstrates the functions used to perform the historical iteration; the choice of view
# and handling the data is arbitrary.

Init ()

# Find a view identifier (omit the view name to grab the first view)
viewName <- "Equity Portfolio View"
matchedViews <- Views (viewName)
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
# to further perturb the historical data)
TriggerViewCycle (viewClient)

# Build the result into these vectors
value.JensensAlpha <- c ()
value.HistoricalVaR <- c ()
value.FairValue <- c ()
value.PnL <- c()

# Lookup the identifier of the root node in the view's portfolio. The data frame containing the cycle results
# will use the node (and position) identifiers as row names.
rootNode <- GetPortfolioNodeUniqueId (GetPortfolioRootNode (FetchPortfolio (GetViewPortfolio (viewIdentifier))))

# Iterate through the results, blocking on the first but waiting no longer than 30s for each subsequent one.
# The timeout is arbitrary to demonstrate how the functions can be used to fail rather than block
# indefinately which may (or may not) be desirable in some systems.
timeout <- 30000
result <- GetViewResult (viewClient, -1)
while (!is.null (result)) {
  # Trigger the next iteration while we work with the previous
  TriggerViewCycle (viewClient)
  print (paste ("Got cycle", viewCycleId.ViewComputationResultModel (result)))
  # Get the data from the "Default" configuration in the view
  data <- results.ViewComputationResultModel (result)$Default
  print (paste(nrow (data), "row(s) of data"))
  # Identify the row with the portfolio values
  portfolio <- data[rootNode,]
  # Get the values for the portfolio
  jensensAlpha <- firstValue.ViewComputationResultModel (portfolio, columns.ViewComputationResultModel (data, ValueRequirementNames.Jensen.s.Alpha))
  historicalVaR <- firstValue.ViewComputationResultModel (portfolio, columns.ViewComputationResultModel (data, ValueRequirementNames.HistoricalVaR))
  fairValue <- firstValue.ViewComputationResultModel (portfolio, columns.ViewComputationResultModel (data, ValueRequirementNames.FairValue))
  pnl <- firstValue.ViewComputationResultModel (portfolio, columns.ViewComputationResultModel (data, ValueRequirementNames.PnL))
  print (paste ("Value for", valuationTime.ViewComputationResultModel (result), "=", jensensAlpha, historicalVaR, fairValue, pnl))
  value.JensensAlpha <- append (value.JensensAlpha, if (is.numeric (jensensAlpha)) as.double (jensensAlpha) else NA)
  value.HistoricalVaR <- append (value.HistoricalVaR, if (is.numeric (historicalVaR)) as.double (historicalVaR) else NA)
  value.FairValue <- append (value.FairValue, if (is.numeric (fairValue)) as.double (fairValue) else NA)
  value.PnL <- append (value.PnL, if (is.numeric (pnl)) as.double (pnl) else NA)
  # Next iteration
  print (paste ("Waiting for next cycle"))
  result <- GetViewResult (viewClient, timeout, viewCycleId.ViewComputationResultModel (result))
}

value.JensensAlpha.ts <- ts (data = value.JensensAlpha, start = as.Date (startTime))
value.HistoricalVaR.ts <- ts (data = value.HistoricalVaR, start = as.Date (startTime))
value.FairValue.ts <- ts (data = value.FairValue, start = as.Date (startTime))
value.PnL.ts <- ts (data = value.PnL, start = as.Date (startTime))

# Store this time series into the database
# StoreTimeSeries (value.FairValue.ts, name = "FairValue on Example Portfolio", dataField = "FairValue", dataSource = "OPENGAMMA", dataProvider = "OPENGAMMA", observationTime = "LONDON_CLOSE", master = "GLOBAL")
