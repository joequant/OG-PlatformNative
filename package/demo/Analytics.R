##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates the full use of the system for performing calculations by creating a security, a suitable view,
# a market data snapshot from the view, and then executing the view with peturbations on the market data snapshot.

Init ()

# Helper function to get a value from a view result (assumes the only node is the root node)
get.result <- function (view.result, value.requirement.name, calc.config = "Default") {
  data <- results.ViewComputationResultModel (view.result)[[calc.config]]
  root.node <- data[which (data$type == "PORTFOLIO_NODE"),]
  columns <- columns.ViewComputationResultModel (data, value.requirement.name)
  firstValue.ViewComputationResultModel (root.node, columns)
}

# Helper function to alter the values for a yield curve
modify.curve <- function (curve, shift) {
  data <- values.YieldCurveSnapshot (curve)
  apply (data, 1, function (x) {
    v <- x["MarketValue"]
    if (!is.na (v)) {
      curve <<- SetYieldCurvePoint (curve, x["ValueName"], x["Identifier"], as.real (v) + shift)
    }
    invisible (0)
  })
  curve
}

# Create the security
print ("Creating security")
security <- FXOptionSecurity (
  name = "FX vanilla option, put EUR 1.5M, receive USD 1M, maturity 1/6/2014",
  putCurrency = "EUR",
  putAmount = 1500000,
  callCurrency = "USD",
  callAmount = 1000000,
  expiry = "2014-01-06",
  settlementDate = "2014-01-06",
  long = TRUE,
  exerciseType = AmericanExerciseType ())
security.id <- StoreSecurity (security)

# Create a portfolio containing a position in this security
print ("Creating portfolio")
position <- PortfolioPosition (security.id, 1)
node <- PortfolioNode (name = "Example", positions = position)
portfolio <- Portfolio ("Example Portfolio", node)
portfolio.id <- StorePortfolio (portfolio)

# Create a view on this portfolio
print ("Creating view")
requirements <- c (ValueRequirementNames.Present.Value)
view <- ViewDefinition ("Example View", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)

# Create a view client to get an initial calculation from current market data
print ("Creating market data view client")
view.client.descriptor <- StaticMarketDataViewClient (view.id)
view.client <- ViewClient (view.client.descriptor, FALSE)
ConfigureViewClient (view.client, list (EnableCycleAccess ()))
TriggerViewCycle (view.client)
view.result.raw <- GetViewResult (view.client, -1)

# Print out the values calculated on the view
print (paste ("PV from current market data", get.result (view.result.raw, ValueRequirementNames.Present.Value)))

# Take a snapshot
print ("Taking snapshot")
snapshot <- SnapshotViewResult (view.client)
snapshot <- SetSnapshotName (snapshot, "Example")
snapshot.id <- StoreSnapshot (snapshot)

# Create a view client referencing the snapshot that will recalculate when the snapshot changes
print ("Creating snapshot view client")
view.client.descriptor <- TickingSnapshotViewClient (view.id, unversioned.Identifier (snapshot.id))
view.client <- ViewClient (view.client.descriptor, FALSE)
view.result.snapshot <- GetViewResult (view.client, -1)

# Sanity check; the value from the snapshot MUST match the live data version
print (paste ("PV from base snapshot", get.result (view.result.snapshot, ValueRequirementNames.Present.Value)))

# Generate some peturbation amounts (THIS IS FOR DEMONSTRATION ONLY; IT IS NOT A GOOD WAY TO PRODUCE RANDOM NUMBERS)
shifts <- sapply (head (randu$x, 10), function (x) { x / 100 })

# Iterate through them
for (shift in shifts) {

  # Modify the snapshot by tweaking the USD forward curve
  print (paste ("Modifying snapshot", snapshot.id, "by", shift))
  snapshot <- SetSnapshotYieldCurve (snapshot, "USD_FUNDING", modify.curve (GetSnapshotYieldCurve (snapshot, "USD_FUNDING"), shift))

  # Write the snapshot back
  print (paste ("Updating snapshot"))
  snapshot.id <- StoreSnapshot (snapshot, snapshot.id)

  # Wait for the new result
  print (paste ("Waiting for next result on snapshot", snapshot.id))
  view.result.snapshot <- GetViewResult (view.client, -1, viewCycleId.ViewComputationResultModel (view.result.snapshot))

  # Print out the values calculated on the view
  print (paste ("PV from snapshot", get.result (view.result.snapshot, ValueRequirementNames.Present.Value)))

}
