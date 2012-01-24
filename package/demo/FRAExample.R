##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates constructing a FRA security, specifying all of the market data required and pricing it using the engine.

Init ()

# Create the security as an R object
OpenGamma:::LOGDEBUG ("Creating security")
fra.security <- FRASecurity (
  name = "FRA USD 4000MM @ 4.5%, from 2020-04-06 to 2021-04-06",
  currency = "USD",
  regionId = "ISO COUNTRY ALPHA2~US",
  startDate = "2020-04-06",
  endDate = "2021-04-06",
  rate = "0.045",
  amount = "4000",
  underlyingId = "BLOOMBERG_TICKER~US0003M Index",
  fixingDate = "2020-04-04")

# Store the security in the session database
OpenGamma:::LOGDEBUG ("Storing security")
fra.security.id <- StoreSecurity (fra.security)
OpenGamma:::LOGINFO ("Created security", fra.security.id)

# Create a portfolio containing a position in this security so that we can price it
OpenGamma:::LOGDEBUG ("Creating portfolio")
position <- PortfolioPosition (security = fra.security.id, quantity = 1)
node <- PortfolioNode (name = "FRA", positions = position)
portfolio <- Portfolio (name = "FRA", rootNode = node)
portfolio.id <- StorePortfolio (portfolio)
OpenGamma:::LOGINFO ("Created portfolio", portfolio.id)

# Create a view on this portfolio
OpenGamma:::LOGDEBUG ("Creating view")
requirements <- c (ValueRequirementNames.Present.Value, ValueRequirementNames.PV01)
view <- ViewDefinition ("FRA example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
OpenGamma:::LOGINFO ("Created view", view.id)

# Create a snapshot containing the market data
OpenGamma:::LOGDEBUG ("Creating (empty) snapshot")
market.data <- Snapshot ()
market.data <- SetSnapshotName (market.data, "FRA example")
market.data.id <- StoreSnapshot (market.data)
OpenGamma:::LOGINFO ("Created snapshot", market.data.id)

# Create a view client attached to the snapshot
OpenGamma:::LOGDEBUG ("Creating view client")
view.client <- ViewClient (viewDescriptor = StaticSnapshotViewClient (view.id, unversioned.Identifier (market.data.id)), useSharedProcess = FALSE)
view.result <- NULL

# Set the snapshot snapshot with necessary market data
OpenGamma:::LOGDEBUG ("Updating snapshot")
tickers <- list (
  "BLOOMBERG_TICKER~US0001M Index" = 0.0029185,
  "BLOOMBERG_TICKER~US0001W Index" = 0.002025,
  "BLOOMBERG_TICKER~US0002M Index" = 0.004195,
  "BLOOMBERG_TICKER~US0002W Index" = 0.00246,  
  "BLOOMBERG_TICKER~US0003M Index" = 0.0057125,
  "BLOOMBERG_TICKER~USFR0CF Curncy" = 0.00665,
  "BLOOMBERG_TICKER~USFR0FI Curncy" = 0.00731,
  "BLOOMBERG_TICKER~USSW1 Curncy" = 0.00697,
  "BLOOMBERG_TICKER~USSW10 Curncy" = 0.02113,
  "BLOOMBERG_TICKER~USSW15 Curncy" = 0.02473,
  "BLOOMBERG_TICKER~USSW2 Curncy" = 0.007505,
  "BLOOMBERG_TICKER~USSW20 Curncy" = 0.025935,
  "BLOOMBERG_TICKER~USSW25 Curncy" = 0.026515,
  "BLOOMBERG_TICKER~USSW3 Curncy" = 0.008585,
  "BLOOMBERG_TICKER~USSW30 Curncy" = 0.02689,
  "BLOOMBERG_TICKER~USSW4 Curncy" = 0.010655,
  "BLOOMBERG_TICKER~USSW5 Curncy" = 0.013005,
  "BLOOMBERG_TICKER~USSW6 Curncy" = 0.01525,
  "BLOOMBERG_TICKER~USSW7 Curncy" = 0.017185,
  "BLOOMBERG_TICKER~USSW8 Curncy" = 0.01875,
  "BLOOMBERG_TICKER~USSW9 Curncy" = 0.020015)
curve.forward.3m <- SnapshotYieldCurve ()
for (ticker in tickers) {
  curve.forward.3m <- SetYieldCurvePoint (
    snapshot = curve.forward.3m,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = ticker,
    marketValue = tickers[[ticker]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_FORWARD_3M", yieldCurve = curve.forward.3m)
tickers <- list (
  "BLOOMBERG_TICKER~USDR2T Curncy" = 0.0027,
  "BLOOMBERG_TICKER~USSO1 Curncy" = 0.00125,
  "BLOOMBERG_TICKER~USSO10 Curncy" = 0.0173,
  "BLOOMBERG_TICKER~USSO2 Curncy" = 0.001705,
  "BLOOMBERG_TICKER~USSO3 Curncy" = 0.00289,
  "BLOOMBERG_TICKER~USSO4 Curncy" = 0.005185,
  "BLOOMBERG_TICKER~USSO5 Curncy" = 0.007765,
  "BLOOMBERG_TICKER~USSOA Curncy" = 0.000749,
  "BLOOMBERG_TICKER~USSOB Curncy" = 0.00083,
  "BLOOMBERG_TICKER~USSOC Curncy" = 0.00091,
  "BLOOMBERG_TICKER~USSOD Curncy" = 0.00094,
  "BLOOMBERG_TICKER~USSOE Curncy" = 0.000987,
  "BLOOMBERG_TICKER~USSOF Curncy" = 0.001022,
  "BLOOMBERG_TICKER~USSOI Curncy" = 0.00112)
curve.funding <- SnapshotYieldCurve ()
for (ticker in tickers) {
  curve.funding <- SetYieldCurvePoint (
    snapshot = curve.funding,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = ticker,
    marketValue = tickers[[ticker]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_FUNDING", yieldCurve = curve.funding)
market.data.id <- StoreSnapshot (snapshot = market.data, identifier = market.data.id)
OpenGamma:::LOGINFO ("Snapshot updated to", market.data.id)

# Get the results from the view
OpenGamma:::LOGDEBUG ("Fetching next results")
TriggerViewCycle (view.client)
view.result <- GetViewResult (
  viewClient = view.client,
  waitForResult = -1,
  lastViewCycleId = if (is.null (view.result)) NULL else viewCycleId.ViewComputationResultModel (view.result))
OpenGamma:::LOGINFO ("Got result", viewCycleId.ViewComputationResultModel (view.result))

# Pull out the results from the data.frame to a simple list
view.result.data <- results.ViewComputationResultModel (view.result)[["Default"]]
root.node <- view.result.data[which (view.result.data$type == "PORTFOLIO_NODE"),]
results <- sapply (requirements, function (requirement) {
  firstValue.ViewComputationResultModel (root.node, columns.ViewComputationResultModel (view.result.data, requirement))
})
print (results)
