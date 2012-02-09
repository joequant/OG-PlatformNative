##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates constructing a swap security, specifying all of the market data required and pricing it using the engine.

Init ()

# Create the security as an R object
OpenGamma:::LOGDEBUG ("Creating security")
security <- SwapSecurity (
  name = "IR Swap USD 40,326,000 2021-08-08 - USD LIBOR 3m / 2.709%",
  tradeDate = "2011-08-08",
  effectiveDate = "2011-08-08",
  maturityDate = "2021-08-08",
  counterparty = "CParty",
  payLeg = FloatingInterestRateLeg (
    dayCount = "Actual/360",
    frequency = "Quarterly",
    regionId = "FINANCIAL_REGION~US+GB",
    businessDayConvention = "Modified Following",
    notional = InterestRateNotional ("USD", 40326000),
    eom = FALSE,
    floatingReferenceRateId = "Reference Rate Simple Name~USD LIBOR 3m",
    floatingRateType = "IBOR"),
  receiveLeg = FixedInterestRateLeg (
    dayCount = "30U/360",
    frequency = "Semi-annual",
    regionId = "FINANCIAL_REGION~US+GB",
    businessDayConvention = "Modified Following",
    notional = InterestRateNotional ("USD", 40326000),
    eom = FALSE,
    rate = 0.027))

# Store the security in the session database
OpenGamma:::LOGDEBUG ("Storing security")
security.id <- StoreSecurity (security)
OpenGamma:::LOGINFO ("Created security", security.id)

# Create a portfolio containing a position in this security so that we can price it
OpenGamma:::LOGDEBUG ("Creating portfolio")
position <- PortfolioPosition (security = security.id, quantity = 1)
node <- PortfolioNode (name = "SWAP", positions = position)
portfolio <- Portfolio (name = "SWAP", rootNode = node)
portfolio.id <- StorePortfolio (portfolio)
OpenGamma:::LOGINFO ("Created portfolio", portfolio.id)

# Create a view on this portfolio
OpenGamma:::LOGDEBUG ("Creating view")
requirements <- c (ValueRequirementNames.Present.Value, ValueRequirementNames.PV01)
view <- ViewDefinition ("Swap example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
OpenGamma:::LOGINFO ("Created view", view.id)

# Create a snapshot containing the market data
OpenGamma:::LOGDEBUG ("Creating (empty) snapshot")
market.data <- Snapshot ()
market.data <- SetSnapshotName (market.data, "Swap example")
market.data.id <- StoreSnapshot (market.data)
OpenGamma:::LOGINFO ("Created snapshot", market.data.id)

# Create a view client attached to the snapshot
OpenGamma:::LOGDEBUG ("Creating view client")
view.client <- ViewClient (viewDescriptor = StaticSnapshotViewClient (view.id, unversioned.Identifier (market.data.id)), useSharedProcess = FALSE)
view.result <- NULL

# Set the snapshot with necessary market data
OpenGamma:::LOGDEBUG ("Updating snapshot")
tickers <- list (
  "OG_SYNTHETIC_TICKER~USDSWAPP40Y" = 0.032172118,
  "OG_SYNTHETIC_TICKER~USDCASHP2M" = 0.004125419,
  "OG_SYNTHETIC_TICKER~USDSWAPP3Y" = 0.028081827,
  "OG_SYNTHETIC_TICKER~USDCASHP12M" = 0.015572704,
  "OG_SYNTHETIC_TICKER~USDCASHP6M" = 0.008696717,
  "OG_SYNTHETIC_TICKER~USDCASHP5M" = 0.006364289,
  "OG_SYNTHETIC_TICKER~USDSWAPP9Y" = 0.039016326,
  "OG_SYNTHETIC_TICKER~USDSWAPP20Y" = 0.037136499,
  "OG_SYNTHETIC_TICKER~USDCASHP3M" = 0.004699534,
  "OG_SYNTHETIC_TICKER~USDSWAPP15Y" = 0.040911091,
  "OG_SYNTHETIC_TICKER~USDCASHP11M" = 0.015744525,
  "OG_SYNTHETIC_TICKER~USDSWAPP10Y" = 0.046078292,
  "OG_SYNTHETIC_TICKER~USDCASHP10M" = 0.014378368,
  "OG_SYNTHETIC_TICKER~USDCASHP4M" = 0.006755011,
  "OG_SYNTHETIC_TICKER~USDCASHP1M" = 0.002773332,
  "OG_SYNTHETIC_TICKER~USDCASHP7M" = 0.009537775,
  "OG_SYNTHETIC_TICKER~USDCASHP1D" = 0.002178863,
  "OG_SYNTHETIC_TICKER~USDSWAPP25Y" = 0.039112661,
  "OG_SYNTHETIC_TICKER~USDSWAPP2Y" = 0.027865134,
  "OG_SYNTHETIC_TICKER~USDCASHP9M" = 0.013241200,
  "OG_SYNTHETIC_TICKER~USDSWAPP6Y" = 0.039671293,
  "OG_SYNTHETIC_TICKER~USDSWAPP8Y" = 0.040113628,
  "OG_SYNTHETIC_TICKER~USDSWAPP12Y" = 0.037815631,
  "OG_SYNTHETIC_TICKER~USDSWAPP30Y" = 0.041970889,
  "OG_SYNTHETIC_TICKER~USDSWAPP7Y" = 0.034719706,
  "OG_SYNTHETIC_TICKER~USDSWAPP4Y" = 0.034639707,
  "OG_SYNTHETIC_TICKER~USDSWAPP5Y" = 0.032331473,
  "OG_SYNTHETIC_TICKER~USDCASHP8M" = 0.011902323)
curve <- SnapshotYieldCurve ()
for (ticker in names (tickers)) {
  curve <- SetYieldCurvePoint (
    snapshot = curve,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = ticker,
    marketValue = tickers[[ticker]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_SECONDARY", yieldCurve = curve)
market.data.id <- StoreSnapshot (snapshot = market.data, identifier = market.data.id)
OpenGamma:::LOGINFO ("Snapshot updated to", market.data.id)

# Get the results from the view
OpenGamma:::LOGDEBUG ("Fetching results")
TriggerViewCycle (view.client)
view.result <- GetViewResult (
  viewClient = view.client,
  waitForResult = -1,
  NULL)
OpenGamma:::LOGINFO ("Got result", viewCycleId.ViewComputationResultModel (view.result))

# Pull out the results from the data.frame to a simple list
view.result.data <- results.ViewComputationResultModel (view.result)[["Default"]]
root.node <- view.result.data[which (view.result.data$type == "PORTFOLIO_NODE"),]
results <- sapply (requirements, function (requirement) {
  firstValue.ViewComputationResultModel (root.node, columns.ViewComputationResultModel (view.result.data, requirement))
})
print (results)
