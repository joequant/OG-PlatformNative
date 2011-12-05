##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates the full use of the system for performing calculations by creating a security, a suitable view,
# a market data snapshot from the view, and then executing the view with peturbations on the security.

# Helper function to get a value from a view result (assumes the only node is the root node)
get.result <- function (view.result, value.requirement.name, calc.config = "Default") {
  data <- results.ViewComputationResultModel (view.result)[[calc.config]]
  root.node <- data[which (data$type == "PORTFOLIO_NODE"),]
  columns <- columns.ViewComputationResultModel (data, value.requirement.name)
  firstValue.ViewComputationResultModel (root.node, columns)
}

# Creates a portfolio containing a single position in a security and a view on the portfolio
create.view <- function (security) {
  security.id <- StoreSecurity (security)
  position <- PortfolioPosition (security.id, 1)
  node <- PortfolioNode (name = "Example", positions = position)
  portfolio <- Portfolio ("Example Portfolio", node)
  portfolio.id <- StorePortfolio (portfolio)
  requirements <- c (ValueRequirementNames.Present.Value)
  ViewDefinition ("Example View", portfolio.id, requirements)
}

# Create the security
print ("Creating base security")
security <- FXOptionSecurity (
  name = "Example security",
  putCurrency = "EUR",
  putAmount = 1500000,
  callCurrency = "USD",
  callAmount = 1000000,
  expiry = "2014-01-06",
  settlementDate = "2014-01-06",
  long = TRUE,
  exerciseType = AmericanExerciseType ())

# Create the view
print ("Creating base view")
view.id <- StoreViewDefinition (create.view (security))

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

# Create a view client referencing the snapshot
print ("Creating snapshot view client")
view.client.descriptor <- StaticSnapshotViewClient (unversioned.Identifier (view.id), snapshot.id)
view.client <- ViewClient (view.client.descriptor, FALSE)
TriggerViewCycle (view.client)
view.result.snapshot <- GetViewResult (view.client, -1)

# Sanity check; the value from the snapshot MUST match the live data version
print (paste ("PV from base snapshot", get.result (view.result.snapshot, ValueRequirementNames.Present.Value)))

# Generate some peturbation amounts (THIS IS FOR DEMONSTRATION ONLY; IT IS NOT A GOOD WAY TO PRODUCE RANDOM NUMBERS)
shifts <- sapply (head (randu$x, 10), function (x) { x + 0.5 })

# Iterate through them
for (shift in shifts) {

  # Modify the security and update the view
  print (paste ("Modifying security by", shift))
  security.modified <- SetFXOptionSecurityCallAmount (security, GetFXOptionSecurityCallAmount (security) * shift)
  view.modified <- create.view (security.modified)
  view.id <- StoreViewDefinition (view.modified, view.id)

  # Trigger a cycle and wait for the new result
  TriggerViewCycle (view.client)
  view.result.cycleId <- viewCycleId.ViewComputationResultModel (view.result.snapshot)
  print (paste ("Waiting for next result after cycle", view.result.cycleId))
  view.result.snapshot <- GetViewResult (view.client, -1, view.result.cycleId)

  # Print out the values calculated on the view
  print (paste ("PV ", get.result (view.result.snapshot, ValueRequirementNames.Present.Value)))

}
