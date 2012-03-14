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
  name = "Long put USD 1000000.0, call EUR 75000.0 on 2012-07-01",
  callAmount = 75000,
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
 # ValueRequirementNames.Forward.Delta.LV,
#  ValueRequirementNames.Forward.Gamma.LV,
#  ValueRequirementNames.Forward.Vanna.LV,
 # ValueRequirementNames.Forward.Vega.LV,
#  ValueRequirementNames.Forward.Vomma.LV,
#  ValueRequirementNames.Full.PDE.Grid.LV,
#  ValueRequirementNames.PDE.Bucketed.Vega.LV,
 # ValueRequirementNames.PDE.Greeks.LV,
  new.ValueRequirement (ValueRequirementNames.Implied.Vol.LV.Black.Equivalent, "CalculationMethod=LocalVolatilityPDEMethod"))
view <- ViewDefinition ("FX Option Example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
calc.config <- "Default"
OpenGamma:::LOGINFO ("Created view", view.id)

# Create a snapshot containing the market data
OpenGamma:::LOGDEBUG ("Creating snapshot")
market.data <- Snapshot ()
market.data <- SetSnapshotName (market.data, "FX Option Example")
market.data <- SetSnapshotGlobalValue (snapshot = market.data, valueName = MarketDataRequirementNames.Market.Value, identifier = "BLOOMBERG_TICKER~EURUSD Curncy", marketValue = 1.3503, type = "PRIMITIVE")
surface.points <- list (
  list ("P14D", "0, ATM", 0.1113),
  list ("P21D", "15, BUTTERFLY", 0.0059),
  list ("P1Y", "25, BUTTERFLY", 0.0056),
  list ("P21D", "25, RISK_REVERSAL", -0.01565),
  list ("P1Y", "15, RISK_REVERSAL", -0.041525),
  list ("P6M", "15, BUTTERFLY", 0.01135),
  list ("P6M", "25, RISK_REVERSAL", -0.025325),
  list ("P5Y", "0, ATM", 0.123425),
  list ("P6M", "15, RISK_REVERSAL", -0.03845),
  list ("P6M", "25, BUTTERFLY", 0.00475),
  list ("P21D", "15, RISK_REVERSAL", -0.02265),
  list ("P1Y", "25, RISK_REVERSAL", -0.02735),
  list ("P21D", "25, BUTTERFLY", 0.002625),
  list ("P1Y", "15, BUTTERFLY", 0.012975),
  list ("P21D", "0, ATM", 0.1091),
  list ("P6M", "0, ATM", 0.117325),
  list ("P14D", "15, BUTTERFLY", 0.004875),
  list ("P14D", "25, RISK_REVERSAL", -0.010375),
  list ("P5Y", "25, BUTTERFLY", 0.004025),
  list ("P5Y", "15, RISK_REVERSAL", -0.032225),
  list ("P1Y", "0, ATM", 0.1234),
  list ("P5Y", "25, RISK_REVERSAL", -0.02105),
  list ("P5Y", "15, BUTTERFLY", 0.008975),
  list ("P14D", "15, RISK_REVERSAL", -0.0156),
  list ("P14D", "25, BUTTERFLY", 0.002125),
  list ("P1M", "25, BUTTERFLY", 0.002525),
  list ("P1M", "15, RISK_REVERSAL", -0.023675),
  list ("P7D", "15, BUTTERFLY", 0.005075),
  list ("P7D", "25, RISK_REVERSAL", -0.0063),
  list ("P3M", "25, BUTTERFLY", 0.003925),
  list ("P10Y", "15, BUTTERFLY", 0.008275),
  list ("P3M", "15, RISK_REVERSAL", -0.03415),
  list ("P10Y", "25, RISK_REVERSAL", -0.018725),
  list ("P1M", "25, RISK_REVERSAL", -0.016075),
  list ("P1M", "15, BUTTERFLY", 0.005475),
  list ("P3M", "25, RISK_REVERSAL", -0.0226),
  list ("P10Y", "15, RISK_REVERSAL", -0.030975),
  list ("P3M", "15, BUTTERFLY", 0.00875),
  list ("P10Y", "25, BUTTERFLY", 0.002775),
  list ("P7D", "15, RISK_REVERSAL", -0.009225),
  list ("P9M", "0, ATM", 0.12125),
  list ("P7D", "25, BUTTERFLY", 0.0022),
  list ("P7D", "0, ATM", 0.1146),
  list ("P9M", "15, RISK_REVERSAL", -0.04175),
  list ("P9M", "25, BUTTERFLY", 0.005225),
  list ("P10Y", "0, ATM", 0.124325),
  list ("P3M", "0, ATM", 0.113),
  list ("P9M", "15, BUTTERFLY", 0.01255),
  list ("P9M", "25, RISK_REVERSAL", -0.02645),
  list ("P1M", "0, ATM", 0.11015))
surface.data <- SnapshotVolatilitySurface ()
for (surface.point in surface.points) {
  surface.data <- SetVolatilitySurfacePoint (snapshot = surface.data, x = surface.point[[1]], y = surface.point[[2]], marketValue = surface.point[[3]], xc = "TENOR", yc = "INTEGER_FXVOLQUOTETYPE_PAIR")
}
surface.name <- "UnorderedCurrencyPair~EURUSD_DEFAULT_MarketStrangleRiskReversal_FX_VANILLA_OPTION"
market.data <- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)
market.data.id <- StoreSnapshot (market.data)
OpenGamma:::LOGINFO ("Created snapshot", market.data.id)

tickers <- list (
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

curve.funding <- SnapshotYieldCurve ()
for (ticker in names (tickers)) {
  curve.funding <- SetYieldCurvePoint (
    snapshot = curve.funding,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = ticker,
    marketValue = tickers[[ticker]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "USD_FUNDING", yieldCurve = curve.funding)
market.data.id <- StoreSnapshot (snapshot = market.data, identifier = market.data.id)


tickers <- list (
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

curve.funding <- SnapshotYieldCurve ()
for (ticker in names (tickers)) {
  curve.funding <- SetYieldCurvePoint (
    snapshot = curve.funding,
    valueName = MarketDataRequirementNames.Market.Value,
    identifier = ticker,
    marketValue = tickers[[ticker]])
}
market.data <- SetSnapshotYieldCurve (snapshot = market.data, name = "EUR_FUNDING", yieldCurve = curve.funding)
market.data.id <- StoreSnapshot (snapshot = market.data, identifier = market.data.id)


# Create a view client attached to the snapshot
OpenGamma:::LOGDEBUG ("Creating view client")
view.client <- ViewClient (viewDescriptor = StaticSnapshotViewClient (view.id, unversioned.Identifier (market.data.id)), useSharedProcess = FALSE)
view.result <- NULL


RiskReversal <- function(callVol, putVol) callVol - putVol
Butterfly <- function(callVol, putVol, atm) 0.5*(callVol+putVol)-atm
CallVol <- function(butterfly, riskReversal,atmVol)butterfly+0.5*riskReversal+atmVol
PutVol <- function(butterfly, riskReversal,atmVol)butterfly-0.5*riskReversal+atmVol


#Convert FX quote points (Risk reversals etc) to flat delta points
toDeltaPoints <- function(fxQuotePoints){
  dimensions <- dim(fxQuotePoints)
  deltas = dimensions[1]
  expiries = dimensions[2]
  if((deltas-1)%%2!=0) OpenGamma:::LOGERROR ("must be an odd number of deltas")
  deltaPairs = (deltas-1)%/%2;
  res <- matrix(NA,deltas,expiries)
  for(j in 1:expiries){
    atmVol = fxQuotePoints[1,j]
    for(i in 1:deltaPairs){
      rr =  fxQuotePoints[2*i,j]
      butt =  fxQuotePoints[2*i+1,j]
      res[i,j]= PutVol(butt,rr,atmVol)
      res[deltas+1-i,j] = CallVol(butt,rr,atmVol)
    }
    res[deltaPairs+1,j] =atmVol
  }
  res
}

#Convert flat FX delta quotes to risk reversals etc 
toFXQuotePoints <- function(deltaPoints){
  dimensions <- dim(deltaPoints)
  deltas = dimensions[1]
  expiries = dimensions[2]
  if((deltas-1)%%2!=0) OpenGamma:::LOGERROR ("must be an odd number of deltas")
  deltaPairs = (deltas-1)%/%2;
  res <- matrix(NA,deltas,expiries)
  for(j in 1:expiries){
    atmVol = deltaPoints[deltaPairs+1,j]
    for(i in 1:deltaPairs){
      put = deltaPoints[i,j]
      call = deltaPoints[deltas+1-i,j]
      res [2*i,j] = RiskReversal(call,put)
      res [2*i+1,j] = Butterfly(call,put,atmVol)
    }
    res[1,j] =atmVol
  }
  res
}

getPresentValue<- function(deltaPoints){
  fxQuotePoints <-toFXQuotePoints(deltaPoints)
  surface.data <<- SetVolatilitySurfaceTensor (snapshot = surface.data, overrideValue = fxQuotePoints)
  market.data <<- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)
  market.data.id <<- StoreSnapshot (snapshot = market.data, identifier = market.data.id)
  # Get the results from the view
  OpenGamma:::LOGDEBUG ("Fetching results")
  TriggerViewCycle (view.client)
  view.result <<- GetViewResult (
    viewClient = view.client,
    waitForResult = -1,
    lastViewCycleId = if (is.null (view.result)) { NULL } else { viewCycleId.ViewComputationResultModel (view.result) })
  # Pull out the results from the data.frame to a simple list
  view.result.data <- results.ViewComputationResultModel (view.result)[[calc.config]]
  results <- lapply (requirements, function (requirement) {
    columns <- columns.ViewComputationResultModel (view.result.data, requirement)
    values <- column.ViewComputationResultModel (view.result, calc.config, columns)
    if (length (values) > 0) {
      values[[1]]
    } else {
      NA
    }
  })
  names (results) <- requirements
  results[[1]]
}


tensor <- GetVolatilitySurfaceTensor (snapshot = surface.data, marketValue = TRUE)
OpenGamma:::LOGINFO ("about to run toDeltaPoints")
deltaPoints <- toDeltaPoints(tensor)

#PV cal
pv =  getPresentValue(deltaPoints)

#Bump each flat delta input in turn and compute the change in value of the option
eps <- 1e-4
dimensions <- dim(deltaPoints)
deltas <- dimensions[1]
expiries <- dimensions[2]
res <- matrix(NA,deltas,expiries)
for(i in 1:deltas){
  for(j in 1:expiries){
      temp <- deltaPoints
      temp[i,j] <- temp[i,j]+eps;
      bumped <- getPresentValue(temp)
      res[i,j] <- (bumped-pv)/eps
    }
}


print(pv)
print(res)

  
