##
# Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
#
# Please see distribution for license.
##

# Note: this is not currently part of the main demo set as it includes Bloomberg tickers so does not work with the Open Source example server
Init ()

#get an expiry date when tenor is a string in the formatt PxE, with E = D, M or Y and x an integer 
getExpiry <- function(tenor, today){
  num <- as.integer(substring (tenor, 2, nchar(tenor) - 1))
  dmy <-  substring (tenor, nchar(tenor))
  expiry <- today
  if(dmy == "d" || dmy == "D"){
    expiry <- expiry + as.difftime(num, format = "%X", units = "days")
  }else if(dmy == "m" || dmy == "M"){
    expiry$mon <- expiry$mon + num
  }else if(dmy == "y" || dmy == "Y"){
    expiry$year <- expiry$year + num
  }else{
    stop(paste(dmy," not a valid tenor"))
  }
  expiry <- as.POSIXct(expiry)
}
#########################
# end of utility functions 
#########################


today <- as.POSIXlt("2012-04-24", "GMT") #price option from 24/04/2012. Change to use some other date or use Sys.Date()
option.expiries  <- c("P6M") #The expiries of the options to price
spotFX <- 1.34
option.strikes <-seq(0.8 ,2.0,length.out=50)
notional <- 1000000

# Create a snapshot containing the base market data
OpenGamma:::LOGDEBUG ("Creating snapshot")
market.data <- Snapshot ()
market.data <- SetSnapshotName (market.data, "FX Option Example")
market.data <- SetSnapshotGlobalValue (snapshot = market.data, valueName = MarketDataRequirementNames.Market.Value, identifier = "BLOOMBERG_TICKER~EURUSD Curncy", marketValue = spotFX, type = "PRIMITIVE")

######################################################################################
# Specify the market surface data
######################################################################################
tenors <- c ("P7D", "P14D", "P21D", "P1M", "P3M", "P6M", "P9M", "P1Y", "P5Y", "P10Y")
FX.quotes <- c ("0, ATM", "15, BUTTERFLY", "15, RISK_REVERSAL", "25, BUTTERFLY", "25, RISK_REVERSAL")
#NOTE only use one of these
#Flat surface (for debugging)
#atmVol <- 0.11
#surface.points.data <- rep (c(atmVol, 0, 0, 0, 0), length (tenors))
#Real market data
# surface.points.data <- c (0.1146, 0.005075, -0.009225, 0.0022, -0.0063,
#                           0.1113, 0.004875, -0.0156, 0.002125, -0.010375,
#                           0.1091, 0.0059, -0.02265, 0.002625, -0.01565,
#                           0.11015, 0.005475, -0.023675, 0.002525, -0.016075,
#                           0.113, 0.00875, -0.03415, 0.003925, -0.0226,
#                           0.117325, 0.01135, -0.03845, 0.00475, -0.025325,
#                           0.12125, 0.01255, -0.04175, 0.005225, -0.02645,
#                           0.1234, 0.012975, -0.041525, 0.0056, -0.02735,
#                           0.123425, 0.008975, -0.032225, 0.004025, -0.02105,
#                           0.124325, 0.008275, -0.030975, 0.002775, -0.018725)


surface.points.data <- c (0.17045 ,0.00665 ,-0.0168 ,0.002725 ,-0.012025,
                          0.1688 ,0.00725 ,-0.02935 ,0.00335 ,-0.02015,
                          0.167425 ,0.00835 ,-0.039125 ,0.0038 ,-0.026,
                          0.1697 ,0.009075 ,-0.047325 ,0.004 ,-0.0314,
                          0.1641 ,0.013175 ,-0.058325 ,0.0056 ,-0.0377,
                          0.1642 ,0.01505 ,-0.06055 ,0.0061 ,-0.03905,
                          0.1641 ,0.01565 ,-0.0621 ,0.00615 ,-0.0396,
                          0.1642 ,0.0163 ,-0.063 ,0.00635 ,-0.0402,
                          0.138 ,0.009275 ,-0.032775 ,0.00385 ,-0.02085,
                          0.12515 ,0.007075 ,-0.023925 ,0.002575 ,-0.015175)

surface.data <- fromVectors.VolatilitySurfaceSnapshot (xc = "TENOR", x = tenors, yc = "INTEGER_FXVOLQUOTETYPE_PAIR", y = FX.quotes, marketValue = surface.points.data)
surface.name <- "UnorderedCurrencyPair~EURUSD_DEFAULT_MarketStrangleRiskReversal_VolatilityQuote_FX_VANILLA_OPTION"
market.data <- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)

######################################################################################
# Specify the instruments that build the USD funding curve 
######################################################################################
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

######################################################################################
# Specify the instruments that build the EUR funding curve 
######################################################################################
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

######################################################################################
# Build a portfolio containing all of the theoretical securities; each cycle of the 
# engine can then crunch the numbers for all of them in parallel.
######################################################################################
positions <- c ()
security.labels <- list ()
for (option.expiry in option.expiries) {
  expiry <- getExpiry(option.expiry,today)
  settlementDate <- expiry + as.difftime(2, format = "%X", units = "days") #2 days after expiry
  timeToExpiry <- as.integer(expiry - today)/365.25
  for(option.strike in option.strikes){
#   atm <- spotFX*exp(timeToExpiry*atmVol^2/2) #this is not quite DNS since we are using the spotFX forward 
#   strike <- atm
  # Create and store the security
  OpenGamma:::LOGDEBUG ("Creating security", option.expiry)
  security <- FXOptionSecurity (
    name = paste("Long put USD,",notional,", call EUR", notional/option.strike,"on", expiry),
    callAmount = notional/option.strike,
    callCurrency = "EUR",
    putCurrency = "USD",
    putAmount = notional,
    expiry,
    exerciseType = EuropeanExerciseType (),
    long = TRUE,
    settlementDate)
  security.id <- StoreSecurity (security)
  OpenGamma:::LOGINFO ("Created security", security.id)
  security.labels[[security.id]] <- paste ("Option expiry:", option.expiry, "(", expiry, "tau =", timeToExpiry, "years). Strike:", option.strike)
  # NOTE: is the security "name" used at all; the string above could be stored there and an additional "labels" won't be necessary
  # Create the position
  position <- PortfolioPosition (security = security.id, quantity = 1)
  positions <- c (positions, position)
}}
OpenGamma:::LOGDEBUG ("Creating portfolio")
node <- PortfolioNode (name = "FX Options", positions = positions)
portfolio <- Portfolio (name = "BucketedVega temporary portfolio", rootNode = node)
portfolio.id <- StorePortfolio (portfolio)
OpenGamma:::LOGINFO ("Created portfolio", portfolio.id)

# Create a view on this portfolio
OpenGamma:::LOGDEBUG ("Creating view")
impliedVol.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Implied.Volatility, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=Spline, PDEDirection=Forward")
price.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Present.Value, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
forwardDelta.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Forward.Delta, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator= SABR, PDEDirection=Forward")
 forwardGamma.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Forward.Gamma, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
dualDelta.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Dual.Delta, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
dualGamma.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Dual.Gamma, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
forwardVega.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Forward.Vega, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
forwardVanna.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Forward.Vanna, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
forwardVomma.valueRequirement <- new.ValueRequirement (ValueRequirementNames.Forward.Vomma, "CalculationMethod=LocalVolatilityPDE, SmileInterpolator=SABR, PDEDirection=Forward")
requirements <- c (impliedVol.valueRequirement, price.valueRequirement, forwardDelta.valueRequirement, forwardGamma.valueRequirement, dualDelta.valueRequirement, dualGamma.valueRequirement, forwardVega.valueRequirement, forwardVanna.valueRequirement, forwardVomma.valueRequirement)
#requirements <- c ( forwardVomma.valueRequirement)
view <- ViewDefinition ("FX Option Example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
calc.config <- "Default"
OpenGamma:::LOGINFO ("Created view", view.id)

# Create a view client attached to the snapshot
OpenGamma:::LOGDEBUG ("Creating view client")
view.client <- ViewClient (viewDescriptor = StaticSnapshotViewClient (view.id, unversioned.Identifier (market.data.id)), useSharedProcess = FALSE)
view.result <- NULL
  
# Update the volatility surface, trigger a cycle, and return the list of results 
runOneCycle <- function(fxQuotePoints) {
#   fxQuotePoints <-toFXQuotePoints (deltaPoints)
   surface.data <<- SetVolatilitySurfaceTensor (snapshot = surface.data, overrideValue = fxQuotePoints)
  market.data <<- SetSnapshotVolatilitySurface (market.data, surface.name, surface.data)
  market.data.id <<- StoreSnapshot (snapshot = market.data, identifier = market.data.id)
  # Get the results from the view
  OpenGamma:::LOGDEBUG ("Triggering cycle")
  TriggerViewCycle (view.client)
  view.result <<- GetViewResult (
    viewClient = view.client,
    waitForResult = -1,
    lastViewCycleId = if (is.null (view.result)) { NULL } else { viewCycleId.ViewComputationResultModel (view.result) })
  OpenGamma:::LOGINFO ("Got result", viewCycleId.ViewComputationResultModel (view.result))
  # Pull out the results from the data.frame to a labeled list
  view.result.data <- results.ViewComputationResultModel (view.result)[[calc.config]]
  results <- lapply (requirements, function (requirement) {
    columns <- columns.ViewComputationResultModel (view.result.data, requirement)
    values <- column.ViewComputationResultModel (view.result, calc.config, columns)
    if (length (values) > 0) {
      values
    } else {
      NA
    }
  })
  names (results) <- requirements
  results
}

# Initial calculation using base
tensor <- GetVolatilitySurfaceTensor (snapshot = surface.data, marketValue = TRUE)
base.values <- runOneCycle(tensor)
base.impliedVols <- base.values[[impliedVol.valueRequirement]]
base.pipPrices <- base.values[[price.valueRequirement]]
base.forwardDelta <- base.values[[forwardDelta.valueRequirement]]
base.forwardGamma <- base.values[[forwardGamma.valueRequirement]]
base.dualDelta <- base.values[[dualDelta.valueRequirement]]
base.dualGamma <- base.values[[dualGamma.valueRequirement]]
base.forwardVega <- base.values[[forwardVega.valueRequirement]]
base.forwardVanna <- base.values[[forwardVanna.valueRequirement]]
 base.forwardVomma <- base.values[[forwardVomma.valueRequirement]]
positions <- sapply (fields.FudgeMsg (FetchPortfolio (portfolio.id)$root$positions), function (x) { x$Value$uniqueId })

# Print out the results (note these are keyed by the position identifiers, so we need to query the
# database to resolve these back to security identifiers)
ordered.impliedVols <- c ()
ordered.pipPrices <- c ()
ordered.forwardDelta <- c ()
ordered.forwardGamma <- c ()
ordered.dualDelta <- c ()
ordered.dualGamma <- c ()
ordered.forwardVega <- c ()
ordered.forwardVanna <-c ()
ordered.forwardVomma <- c ()
for (position.id in positions) {
  position <- NA
  try({position <- FetchPosition (position.id)}, silent = TRUE)
  if (!is.na (position)) {
    security.id <- position$securityKey$ID
    security.id <- paste (security.id$Scheme, security.id$Value, sep = "~")
    ordered.impliedVols <- c (ordered.impliedVols , base.impliedVols[[position.id]])
   ordered.pipPrices <- c (ordered.pipPrices, base.pipPrices[[position.id]])
    ordered.forwardDelta <- c (ordered.forwardDelta, base.forwardDelta[[position.id]])
    ordered.forwardGamma <- c (ordered.forwardGamma, base.forwardGamma[[position.id]])
    ordered.dualDelta <- c (ordered.dualDelta, base.dualDelta[[position.id]])
    ordered.dualGamma <- c (ordered.dualGamma, base.dualGamma[[position.id]])
    ordered.forwardVega <- c (ordered.forwardVega, base.forwardVega[[position.id]])
    ordered.forwardVanna <- c (ordered.forwardVanna, base.forwardVanna[[position.id]])
    ordered.forwardVomma <- c (ordered.forwardVomma, base.forwardVomma[[position.id]])
#     print (paste (security.labels[[security.id]]))#, "Implied vol:", base.impVols[[position.id]]))
#     print ("Put price")
#     print (base.pipPrices[[position.id]])
#     print ("Forward delta")
#     print (base.forwardDelta[[position.id]])
#     ordered.forwardDelta <- c (ordered.forwardDelta, base.forwardDelta[[position.id]])
#     print ("Forward gamma")
#     print (base.forwardGamma[[position.id]])
#     print ("Dual delta")
#     print (base.dualDelta[[position.id]])
#     print ("Dual gamma")
#     print (base.dualGamma[[position.id]])
#     print ("Forward vega")
#     print (base.forwardVega[[position.id]])
#     print ("Forward vanna")
#     print (base.forwardVanna[[position.id]])
#     print ("Forward vomma")
#     print (base.forwardVomma[[position.id]])
  }
}
  
plot(option.strikes,ordered.impliedVols)
 plot(option.strikes,ordered.pipPrices)
  plot(option.strikes,ordered.forwardDelta)
  plot(option.strikes,ordered.forwardGamma)
  plot(option.strikes,ordered.dualDelta)
  plot(option.strikes,ordered.dualGamma)
  plot(option.strikes,ordered.forwardVega)
  plot(option.strikes,ordered.forwardVanna)
 plot(option.strikes,ordered.forwardVomma)

