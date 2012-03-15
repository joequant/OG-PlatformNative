##
 # Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates constructing an FX Option Security, specifying all of the market data required and pricing it using the engine.

# Note: this is not currently part of the main demo set as it includes Bloomberg tickers so does not work with the Open Source example server

Init ()

# Create the security as an R object
OpenGamma:::LOGDEBUG ("Creating security")
security <- FXOptionSecurity (
  name = "Long put USD 1000000.0, call EUR 80000.0 on 2012-07-01",
  callAmount = 80000,
  callCurrency = "EUR",
  putCurrency = "USD",
  putAmount = 100000,
  expiry = "2012-07-01",
  exerciseType = EuropeanExerciseType (),
  long = TRUE,
  settlementDate = "2012-07-03")

# Store the security in the session database
OpenGamma:::LOGDEBUG ("Storing security")
security.id <- StoreSecurity (security)
OpenGamma:::LOGINFO ("Created security", security.id)

# Create a portfolio containing a position in this security so that we can price it
OpenGamma:::LOGDEBUG ("Creating portfolio")
position <- PortfolioPosition (security = security.id, quantity = 1)
node <- PortfolioNode (name = "FX Option", positions = position)
portfolio <- Portfolio (name = "Single FX Option", rootNode = node)
portfolio.id <- StorePortfolio (portfolio)
OpenGamma:::LOGINFO ("Created portfolio", portfolio.id)

# Create a view on this portfolio
OpenGamma:::LOGDEBUG ("Creating view")
requirements <- c (
  ValueRequirementNames.Forward.Delta.LV,
  ValueRequirementNames.Forward.Gamma.LV,
  ValueRequirementNames.Forward.Vanna.LV,
  ValueRequirementNames.Forward.Vega.LV,
  ValueRequirementNames.Forward.Vomma.LV,
  ValueRequirementNames.PDE.Bucketed.Vega.LV,
  ValueRequirementNames.PDE.Greeks.LV,
  new.ValueRequirement (ValueRequirementNames.Present.Value, "CalculationMethod=LocalVolatilityPDEMethod"))
view <- ViewDefinition ("FX Option Example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
calc.config <- "Default"
valuation.time <- as.POSIXct ("2012-01-30 12:00:00 GMT")
OpenGamma:::LOGINFO ("Created view", view.id)

# Create a snapshot containing the market data
OpenGamma:::LOGDEBUG ("Creating snapshot")
market.data <- Snapshot ()
market.data <- SetSnapshotName (market.data, "FX Option Example")
market.data <- SetSnapshotGlobalValue (snapshot = market.data, valueName = MarketDataRequirementNames.Market.Value, identifier = "BLOOMBERG_TICKER~EURUSD Curncy", marketValue = 1.32905, type = "PRIMITIVE")
surface.points.x <- c ("P7D", "P14D", "P21D", "P1M", "P3M", "P6M", "P9M", "P1Y", "P5Y", "P10Y")
surface.points.y <- c ("0, ATM", "15, BUTTERFLY", "15, RISK_REVERSAL", "25, BUTTERFLY", "25, RISK_REVERSAL")
surface.points.data <- c (0.1146, 0.005075, -0.009225, 0.0022, -0.0063,
                          0.1113, 0.004875, -0.0156, 0.002125, -0.010375,
                          0.1091, 0.0059, -0.02265, 0.002625, -0.01565,
                          0.11015, 0.005475, -0.023675, 0.002525, -0.016075,
                          0.113, 0.00875, -0.03415, 0.003925, -0.0226,
                          0.117325, 0.01135, -0.03845, 0.00475, -0.025325,
                          0.12125, 0.01255, -0.04175, 0.005225, -0.02645,
                          0.1234, 0.012975, -0.041525, 0.0056, -0.02735,
                          0.123425, 0.008975, -0.032225, 0.004025, -0.02105,
                          0.124325, 0.008275, -0.030975, 0.002775, -0.018725)
surface.data <- fromVectors.VolatilitySurfaceSnapshot (xc = "TENOR", x = surface.points.x, yc = "INTEGER_FXVOLQUOTETYPE_PAIR", y = surface.points.y, marketValue = surface.points.data)
surface.name <- "UnorderedCurrencyPair~EURUSD_DEFAULT_MarketStrangleRiskReversal_VolatilityQuote_FX_VANILLA_OPTION"
market.data <- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)
curve.points <- list (
  "BLOOMBERG_TICKER~USDR1T Curncy" = 0.002,
  "BLOOMBERG_TICKER~USDR2T Curncy" = 0.002,
  "BLOOMBERG_TICKER~USSOA Curncy" = 0.001112,
  "BLOOMBERG_TICKER~USSOB Curncy" = 0.00113,
  "BLOOMBERG_TICKER~USSOC Curncy" = 0.00119,
  "BLOOMBERG_TICKER~USSOD Curncy" = 0.00119,
  "BLOOMBERG_TICKER~USSOE Curncy" = 0.001162,
  "BLOOMBERG_TICKER~USSOF Curncy" = 0.00125,
  "BLOOMBERG_TICKER~USSOI Curncy" = 0.00132,
  "BLOOMBERG_TICKER~USSO1 Curncy" = 0.00137,
  "BLOOMBERG_TICKER~USSO2 Curncy" = 0.00195,
  "BLOOMBERG_TICKER~USSO3 Curncy" = 0.00315,
  "BLOOMBERG_TICKER~USSO4 Curncy" = 0.005185,
  "BLOOMBERG_TICKER~USSO5 Curncy" = 0.007505,
  "BLOOMBERG_TICKER~USSO10 Curncy" = 0.01696)
curve.data <- SnapshotYieldCurve ()
for (curve.point in names (curve.points)) {
  curve.data <- SetYieldCurvePoint (
    snapshot = curve.data,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = curve.point,
    marketValue = curve.points[[curve.point]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_FUNDING", yieldCurve = curve.data)
curve.points <- list (
  "BLOOMBERG_TICKER~EUDR1Z Curncy" = 0.004,
  "BLOOMBERG_TICKER~EUDR2Z Curncy" = 0.00325,
  "BLOOMBERG_TICKER~EUDR3Z Curncy" = 0.0035,
  "BLOOMBERG_TICKER~EUSWEA Curncy" = 0.003455,
  "BLOOMBERG_TICKER~EUSWEB Curncy" = 0.003505,
  "BLOOMBERG_TICKER~EUSWEC Curncy" = 0.003485,
  "BLOOMBERG_TICKER~EUSWED Curncy" = 0.00345,
  "BLOOMBERG_TICKER~EUSWEE Curncy" = 0.00343,
  "BLOOMBERG_TICKER~EUSWEF Curncy" = 0.003415,
  "BLOOMBERG_TICKER~EUSWEG Curncy" = 0.0034175,
  "BLOOMBERG_TICKER~EUSWEH Curncy" = 0.00343,
  "BLOOMBERG_TICKER~EUSWEI Curncy" = 0.003415,
  "BLOOMBERG_TICKER~EUSWEJ Curncy" = 0.00338,
  "BLOOMBERG_TICKER~EUSWEK Curncy" = 0.0034625,
  "BLOOMBERG_TICKER~EUSWE1 Curncy" = 0.00346,
  "BLOOMBERG_TICKER~EUSWE2 Curncy" = 0.00401,
  "BLOOMBERG_TICKER~EUSWE3 Curncy" = 0.0053075,
  "BLOOMBERG_TICKER~EUSWE4 Curncy" = 0.007225,
  "BLOOMBERG_TICKER~EUSWE5 Curncy" = 0.00939,
  "BLOOMBERG_TICKER~EUSWE6 Curncy" = 0.01156,
  "BLOOMBERG_TICKER~EUSWE7 Curncy" = 0.01349,
  "BLOOMBERG_TICKER~EUSWE8 Curncy" = 0.015175,
  "BLOOMBERG_TICKER~EUSWE9 Curncy" = 0.016626,
  "BLOOMBERG_TICKER~EUSWE10 Curncy" = 0.017882,
  "BLOOMBERG_TICKER~EUSWE15 Curncy" = 0.021725,
  "BLOOMBERG_TICKER~EUSWE20 Curncy" = 0.022755,
  "BLOOMBERG_TICKER~EUSWE25 Curncy" = 0.022565,
  "BLOOMBERG_TICKER~EUSWE30 Curncy" = 0.02205)
curve.data <- SnapshotYieldCurve ()
for (curve.point in names (curve.points)) {
  curve.data <- SetYieldCurvePoint (
    snapshot = curve.data,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = curve.point,
    marketValue = curve.points[[curve.point]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "EUR_FUNDING", yieldCurve = curve.data)
market.data.id <- StoreSnapshot (market.data)
OpenGamma:::LOGINFO ("Created snapshot", market.data.id)

# Create a view client attached to the snapshot
OpenGamma:::LOGDEBUG ("Creating view client")
view.client <- ViewClient (viewDescriptor = StaticSnapshotViewClient (view.id, unversioned.Identifier (market.data.id), valuation.time), useSharedProcess = FALSE)
view.result <- NULL

# Run the view with the surface adjusted each time
for (shift in c (0.85, 0.90, 0.95, 1.00, 1.05, 1.10, 1.15)) {

  # Shift the surface
  tensor <- GetVolatilitySurfaceTensor (snapshot = surface.data, marketValue = TRUE)
  tensor <- tensor * shift
  surface.data <- SetVolatilitySurfaceTensor (snapshot = surface.data, overrideValue = tensor)
  market.data <- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)
  market.data.id <- StoreSnapshot (snapshot = market.data, identifier = market.data.id)
  OpenGamma:::LOGINFO ("Updated snapshot for", shift, "shift")

  # Get the results from the view
  OpenGamma:::LOGDEBUG ("Fetching results")
  TriggerViewCycle (view.client)
  view.result <- GetViewResult (
    viewClient = view.client,
    waitForResult = -1,
    lastViewCycleId = if (is.null (view.result)) { NULL } else { viewCycleId.ViewComputationResultModel (view.result) })
  OpenGamma:::LOGINFO ("Got result", viewCycleId.ViewComputationResultModel (view.result))
  
  # Pull out the results from the data.frame to a simple list, converting any Fudge message instances
  view.result.data <- results.ViewComputationResultModel (view.result)[[calc.config]]
  results <- lapply (requirements, function (requirement) {
    columns <- columns.ViewComputationResultModel (view.result.data, requirement)
    values <- column.ViewComputationResultModel (view.result, calc.config, columns)
    if (length (values) > 0) {
      value <- values[[1]]
      if (is.FudgeMsg (value)) {
        toObject.FudgeMsg (value, identity)
      } else {
        value
      }
    } else {
      NA
    }
  })
  names (results) <- requirements
  
  # Just print the results out; further analysis/summary or collation into an outer list is possible here 
  print (results)

}
