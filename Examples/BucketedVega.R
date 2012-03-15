##
# Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
#
# Please see distribution for license.
##

# Demonstrates constructing an FX Option Security, specifying all of the market data required and pricing it using the engine.

# Note: this is not currently part of the main demo set as it includes Bloomberg tickers so does not work with the Open Source example server
today <- as.POSIXlt(Sys.Date(), "GMT")


tenors <- list("7D", "14D", "21D", "1M", "3m", "6M", "9M", "1Y", "5Y", "10Y")
nTenors <- length(tenors)
expiries <- c()
# for (index in c(1:nTenors))
# {
#   offsets <- regexpr("\\d+", tenors[i], perl=TRUE)
#   num <- as.numeric(substring(tenors[i], offsets[1], offsets[1]+attr(offsets, "match.length")-1))
#   if(l)
#   expiries 
# }

#tenor <-  as.difftime(1, format = "%X", units = "weeks")
expiry <- today# + tenor
expiry$year <- expiry$year + 10
settlementDate <- expiry + as.difftime(2, format = "%X", units = "days")
t <- 10#as.numeric(tenor)*7/365

forward <- 1.3
atmVol <- 0.10
atm <- forward*exp(t*atmVol^2/2)
strike <- atm

Init ()
# Create the security as an R object
OpenGamma:::LOGDEBUG ("Creating security")
security <- FXOptionSecurity (
  name = "Long put USD 1000000.0, call EUR 75000.0 on 2012-07-01",
  callAmount = 1000000/strike,
  callCurrency = "EUR",
  putCurrency = "USD",
  putAmount = 1000000,
  expiry = as.POSIXct(expiry),
  #expiry = "2012-07-01",
  exerciseType = EuropeanExerciseType (),
long = TRUE,
  settlementDate = as.POSIXct(settlementDate))
  # settlementDate = "2012-07-03"

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
market.data <- SetSnapshotGlobalValue (snapshot = market.data, valueName = MarketDataRequirementNames.Market.Value, identifier = "BLOOMBERG_TICKER~EURUSD Curncy", marketValue = 1.3, type = "PRIMITIVE")
surface.points.x <- c ("P7D", "P14D", "P21D", "P1M", "P3M", "P6M", "P9M", "P1Y", "P5Y", "P10Y")
surface.points.y <- c ("0, ATM", "15, BUTTERFLY", "15, RISK_REVERSAL", "25, BUTTERFLY", "25, RISK_REVERSAL")
surface.points.data <- rep (c(atmVol, 0, 0, 0, 0), length (surface.points.x))
surface.data <- fromVectors.VolatilitySurfaceSnapshot (xc = "TENOR", x = surface.points.x, yc = "INTEGER_FXVOLQUOTETYPE_PAIR", y = surface.points.y, marketValue = surface.points.data)
surface.name <- "UnorderedCurrencyPair~EURUSD_DEFAULT_MarketStrangleRiskReversal_VolatilityQuote_FX_VANILLA_OPTION"
market.data <- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)
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
market.data.id <- StoreSnapshot (snapshot = market.data)
OpenGamma:::LOGINFO ("Created market data snapshot", market.data.id)

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
