##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates specifying curve names at the position level.

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

# Create a portfolio containing two positions in this security
print ("Creating portfolio")
position.default <- PortfolioPosition (security.id, 1)
position.override <- SetPositionAttribute (PortfolioPosition (security.id, 1), "*.DEFAULT_ForwardCurve", "FUNDING")
node <- PortfolioNode (name = "Example", positions = list (position.default, position.override))
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
TriggerViewCycle (view.client)
view.result <- GetViewResult (view.client, -1)

# Format a data frame with the results
view.result.frame <- results.ViewComputationResultModel (view.result.raw)$Default
