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
requirements <- c (
  ValueRequirementNames.Present.Value,
  new.ValueRequirement (ValueRequirementNames.PV01, "Curve=Discounting"),
  new.ValueRequirement (ValueRequirementNames.PV01, "Curve=Forward3M"))
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

# Test whether running against OG-Examples or OG-BloombergExamples
if (!is.null (FetchTimeSeries (dataField = "CLOSE", identifier = "OG_SYNTHETIC_TICKER~USDCASHP2D", maxPoints = 1))) {
  discounting.tickers <- c ("USDCASHP2D" = 0.0025, "USDOIS_SWAPP1M" = 0.0027, "USDOIS_SWAPP2M" = 0.0027, "USDOIS_SWAPP3M" = 0.0026, "USDOIS_SWAPP4M" = 0.0027, "USDOIS_SWAPP5M" = 0.0030, "USDOIS_SWAPP6M" = 0.0030, "USDOIS_SWAPP9M" = 0.0031, "USDOIS_SWAPP1Y" = 0.0032, "USDOIS_SWAPP2Y" = 0.0035, "USDOIS_SWAPP3Y" = 0.0045, "USDOIS_SWAPP4Y" = 0.0075, "USDOIS_SWAPP5Y" = 0.0010, "USDOIS_SWAPP10Y" = 0.0015)
  forward3m.tickers <- c ("USDLIBORP7D" = 0.0029, "USDLIBORP1M" = 0.0034, "USDLIBORP2M" = 0.0044, "USDSWAPP2Y" = 0.025, "USDSWAPP3Y" = 0.031, "USDSWAPP4Y" = 0.035, "USDSWAPP5Y" = 0.037, "USDSWAPP6Y" = 0.039, "USDSWAPP7Y" = 0.040, "USDSWAPP8Y" = 0.041, "USDSWAPP9Y" = 0.041, "USDSWAPP10Y" = 0.040, "USDSWAPP15Y" = 0.039, "USDSWAPP20Y" = 0.039, "USDSWAPP25Y" = 0.039, "USDSWAPP30Y" = 0.039)
  tickers.scheme <- "OG_SYNTHETIC_TICKER"
} else {
  if (!is.null (FetchTimeSeries (dataField = "PX_LAST", identifier = "BLOOMBERG_TICKER~USDR2T Curncy", maxPoints = 1))) {
    discounting.tickers <- c ("USDR2T Curncy" = 0.0025, "USSOA Curncy" = 0.0027, "USSOB Curncy" = 0.0027, "USSOC Curncy" = 0.0026, "USSOD Curncy" = 0.0027, "USSOE Curncy" = 0.0030, "USSOF Curncy" = 0.0030, "USSOI Curncy" = 0.0031, "USSO1 Curncy" = 0.0032, "USSO2 Curncy" = 0.0035, "USSO3 Curncy" = 0.0045, "USSO4 Curncy" = 0.0075, "USSO5 Curncy" = 0.0010, "USSO10 Curncy" = 0.0015)
    forward3m.tickers <- c ("US0001W Index" = 0.0029, "US0002W Index" = 0.0032, "US0001M Index" = 0.0034, "US0002M Index" = 0.0044, "US0003M Index" = 0.0080, "USSW1 Curncy" = 0.014, "USSW2 Curncy" = 0.025, "USSW3 Curncy" = 0.031, "USSW4 Curncy" = 0.035, "USSW5 Curncy" = 0.037, "USSW6 Curncy" = 0.039, "USSW7 Curncy" = 0.040, "USSW8 Curncy" = 0.041, "USSW9 Curncy" = 0.041, "USSW10 Curncy" = 0.040, "USSW15 Curncy" = 0.039, "USSW20 Curncy" = 0.039, "USSW25 Curncy" = 0.039, "USSW30 Curncy" = 0.039)
    tickers.scheme <- "BLOOMBERG_TICKER"
  } else {
    stop ("Can't find time series tickers")
  }
}

# Set the snapshot with necessary market data
OpenGamma:::LOGDEBUG ("Updating snapshot")
discounting.curve <- SnapshotYieldCurve ()
for (ticker in names (discounting.tickers)) {
  discounting.curve <- SetYieldCurvePoint (
    snapshot = discounting.curve,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = paste (tickers.scheme, ticker, sep = "~"),
    marketValue = discounting.tickers[[ticker]])
}
forward3m.curve <- SnapshotYieldCurve ()
for (ticker in names (forward3m.tickers)) {
  forward3m.curve <- SetYieldCurvePoint (
    snapshot = forward3m.curve,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = paste (tickers.scheme, ticker, sep = "~"),
    marketValue = forward3m.tickers[[ticker]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_Discounting", yieldCurve = discounting.curve)
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_Forward3M", yieldCurve = forward3m.curve)
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
