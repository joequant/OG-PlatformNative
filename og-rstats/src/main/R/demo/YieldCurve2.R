##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Iterates a view over historical data, extracting the fitted curve from each cycle to produce a
# dataset for a 3D "fitted curve over time" chart. This uses the code within the main OpenGamma
# engine and analytics library compared to YieldCurve.R which simply fetches the raw time-series
# used to construct the curve.

Init ()

# Find a view identifier (the view must contain at least one yield curve primitive output)
view.name <- "Swap Portfolio View"
view.matches <- Views (view.name)
if (length (view.matches) == 0) {
  stop ("No view called '", view.name, "' defined")
} else {
  view.identifier <- view.matches[1, 1]
}

# Create a client to iterate over a year of data (see Historical.R for explanation of how)
end.time <- Sys.time () - (14 * 86400)
start.time <- end.time - (365 * 86400)
view.client.descriptor <- HistoricalMarketDataViewClient (view.identifier, start.time, end.time)
view.client <- ViewClient (view.client.descriptor, FALSE)
TriggerViewCycle (view.client)

# Build the result into this - a list of lists, each sub-list contains Curve/Date values for a curve
curves <- list ()
result <- GetViewResult (view.client, -1)
while (!is.null (result)) {
  TriggerViewCycle (view.client)
  print (paste ("Got cycle", viewCycleId.ViewComputationResultModel (result)))
  # Look up the column(s) containing curves
  curve.specs <- columns.ViewComputationResultModel (results.ViewComputationResultModel (result)$Default, ValueRequirementNames.YieldCurve)
  if (length (curve.specs) > 0) {
    print (paste ("Got", length (curve.specs), "curve column(s)"))
    # The data frame representation can't contain the actual FudgeMsg, so we have to request each columns as a list
    for (curve.spec in curve.specs) {
      # Extract the curve name from the column label properties
      curve.name <- properties.ValueRequirement (curve.spec)$Curve
      # Process the curves with this name
      curve.values <- column.ViewComputationResultModel (result, "Default", curve.spec)
      if (length (curve.values) > 0) {
        curve.identifiers <- labels (curve.values)
        for (j in seq (from = 1, to = length (curve.values))) {
          curve.value <- curve.values[[j]]
          if (is.FudgeMsg (curve.value)) {
            # The label is the identifier; e.g. CurrencyISO~GBP so lose the scheme
            curve.currency <- substring (curve.identifiers[j], 13)
            # The curve data is a FudgeMsg (com.opengamma.financial.model.interestrate.curve.YieldCurve)
            curve.object <- curve.value$curve
            if (length (curve.object) > 0) {
              # Append this iteration's curve data into the list
              curve.label <- paste (curve.name, curve.currency, sep = "_")
              curve.data <- curves[[curve.label]]
              if (is.null (curve.data)) {
                curve.data <- list (Date = c(), Curve = list ())
              }
              curve.data$Date <- append (curve.data$Date, valuationTime.ViewComputationResultModel (result))
              curve.data$Curve[[length (curve.data$Curve) + 1]] <- curve.object
              curves[[curve.label]] <- curve.data
              print (paste ("Got curve", curve.name, "on", curve.currency))
            }
          }
        }
      } else {
        print ("No curve values")
      }
    }
  } else {
    print ("No curves in result")
  }
  # Next iteration
  result <- GetViewResult (view.client, -1, viewCycleId.ViewComputationResultModel (result))
}

# For each curve, prepare it for plotting by running the interpolator to get consistently positioned values
curves <- lapply (curves, function (a) {
  # Get the earliest and latest point & generate a spread of 30 points between them
  x.min <- min (sapply (a$Curve, function (b) { min (b$`x data`) }))
  x.max <- max (sapply (a$Curve, function (b) { max (b$`x data`) }))
  x.points <- seq (from = x.min, to = x.max, length.out = 30)
  # Apply the curve's interpolator function to adjust the raw nodal points to get values suitable for plotting
  # by creating a matrix where each row is a yield curve, and each column corresponds to the interpolated points
  y.points <- matrix (sapply (a$Curve, function (b) { GetCurveYValues (b, x.points) }), nc = length (x.points), byrow = TRUE)
  if (require ("xts")) {
    # Create an XTS time series that can be plotted (note the multiply to give us bigger values to plot)
    curve.xts <- xts (y.points * 100, order.by = as.Date (a$Date))
    colnames (curve.xts) <- x.points
    a$xts <- curve.xts
  } else {
    colnames (y.points) <- x.points
    a$ts <- ts (y.points)
  }
  a
})
