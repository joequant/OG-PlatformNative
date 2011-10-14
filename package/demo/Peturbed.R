##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Iterates a view over some historical market data that is peturbed slightly. This demonstrates
# injecting values into the engine.

# Find a view identifier
viewName <- "Simple Swap Test View"
viewIdentifier <- Views (viewName)[[1]][[1]]

# Set up the view client descriptor to sample a historic period (a month ending last week). This
# will be used over each of our "shift" samples.
endTime <- Sys.time () - (7 * 86400)
startTime <- endTime - (30 * 86400)
viewClientDescriptor <- HistoricalMarketDataViewClient (viewIdentifier, startTime, endTime)

# Our shifts (-25%, -15%, 0, +15%, +25%) applied to some tickers
shiftAmounts <- c (0.75, 0.85, 1, 1.15, 1.25)
shiftTickers <- c ("BLOOMBERG_TICKER~US0002M Index", "BLOOMBERG_TICKER~US0003M Index", "BLOOMBERG_TICKER~USSw5 Curncy", "BLOOMBERG_TICKER~USSW10 Curncy")

# Iterate over the shifts
results <- lapply (shiftAmounts, function (shift) {
  
  # Create the view client and configure. Note that we need to give the client's a unique name
  # or we will get the same one back each time as the descriptor matches.
  viewClient <- ViewClient (viewClientDescriptor, useSharedProcess = FALSE, clientName = toString (shift))
  ConfigureViewClient (viewClient, lapply (shiftTickers, function (ticker) { MarketDataOverride (value = shift, valueName = "Market_Value", identifier = ticker, operation = "MULTIPLY") }))

  # Build the PV into this vector
  presentValue <- c ()

  # Run the view cycles
  TriggerViewCycle (viewClient)
  result <- GetViewResult (viewClient, -1)
  while (!is.null (result)) {

    # Trigger the next iteration while we work with the previous
    TriggerViewCycle (viewClient)

    # Extract the PV from our results
    print (paste ("Got cycle", viewCycleId.ViewComputationResultModel (result)))
    data <- results.ViewComputationResultModel (result)$Default
    print (paste (length (data), "row(s) of data"))
    columns <- colnames (data)
    columns.presentValue <- columns[which (substr (columns, 1, 14) == "Present.Value.")]
    portfolio <- data[which (data$type == "PORTFOLIO_NODE"),]
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
    print (paste ("PV for", valuationTime.ViewComputationResultModel (result), "with", shift, "=", value.presentValue))
    presentValue <- append (presentValue, value.presentValue)

    # Get the next iteration
    print (paste ("Waiting for next cycle"))
    result <- GetViewResult (viewClient, -1, viewCycleId.ViewComputationResultModel (result))

  }

  # Release the view client and return the PV vector as the result of this iteration
  presentValue

})

# Build a data frame from the results
columns <- c ()
rows <- max (sapply (results, length))
for (i in seq (from = 1, to = length (shiftAmounts))) {
  columns <- append (paste ("\"x", shiftAmounts[i], "\" = results[[", i, "]]", sep = ""), columns)
  while (length (results[[i]]) < rows) {
    results[[i]] <- append (results[[i]], NA)
  }
}
columns <- paste (columns, collapse = ", ")
cmd <- paste ("data.frame (", columns, ")", sep = "")
result <- eval (parse (text = cmd))
