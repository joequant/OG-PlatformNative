##
 # Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates constructing a portfolio containing securities with different properties
# (in this case FX Options with a range of strikes) to show how a single OpenGamma
# engine cycle can then be used to calculate everything in parallel.

Init ()

expiry <- Sys.time () + as.difftime (26, format = "%X", units = "weeks") # About six-months to expiry
option.strikes <- seq (0.8, 2.0, length.out = 50) # This can take a while - if running on a workstation, try a smaller number
notional <- 1000000

# Create a portfolio containing all of the theortical securities
positions <- c ()
settlementDate <- expiry + as.difftime (2, format = "%X", units = "days") #2 days after expiry
for(option.strike in option.strikes) {
  security <- FXOptionSecurity (
    name = paste ("Long put USD,", notional, ", call EUR", notional / option.strike, "on", expiry),
    callAmount = notional / option.strike,
    callCurrency = "EUR",
    putCurrency = "USD",
    putAmount = notional,
    expiry,
    exerciseType = EuropeanExerciseType (),
    long = TRUE,
    settlementDate)
  security.id <- StoreSecurity (security)
  positions <- c (positions, PortfolioPosition (security = security.id, quantity = 1))
}
node <- PortfolioNode (name = "FX Options", positions = positions)
portfolio <- Portfolio (name = "FX Options Temporary Portfolio", rootNode = node)
portfolio.id <- StorePortfolio (portfolio)
# Fetch the position IDs as stored in the database - results will be keyed by these
positions <- sapply (fields.FudgeMsg (FetchPortfolio (portfolio.id)$root$positions), function (x) { x$Value$uniqueId })

# Create a view on this portfolio. Note the constraints used to configure how
# calculations are performed; refer to the online documentation for the full range
# of parameters that can be used to control each underlying analytic model.
requirements <- list (
  impliedVol = new.ValueRequirement (ValueRequirementNames.Implied.Volatility, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  price = ValueRequirementNames.Present.Value,
  forwardDelta = new.ValueRequirement (ValueRequirementNames.Forward.Delta, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  forwardGamma = new.ValueRequirement (ValueRequirementNames.Forward.Gamma, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  dualDelta = new.ValueRequirement (ValueRequirementNames.Dual.Delta, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  dualGamma = new.ValueRequirement (ValueRequirementNames.Dual.Gamma, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  forwardVega = new.ValueRequirement (ValueRequirementNames.Forward.Vega, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  forwardVanna = new.ValueRequirement (ValueRequirementNames.Forward.Vanna, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")),
  forwardVomma = new.ValueRequirement (ValueRequirementNames.Forward.Vomma, paste (
    "CalculationMethod=LocalVolatilityPDE",
    "SmileInterpolator=Spline",
    "PDEDirection=Forward",
    sep = ", ")))
view <- ViewDefinition ("FX Option Example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
calc.config <- "Default"

# Create a view client attached to the snapshot and execute a cycle against current market data.
# This could equally use a snapshot as shown in other examples.
view.client <- ViewClient (StaticMarketDataViewClient (view.id), useSharedProcess = FALSE)
TriggerViewCycle (view.client)
view.result <- GetViewResult (viewClient = view.client, waitForResult = -1)

# For each value requirement, extract the result column values and produce that in the portfolio
# ordering (i.e. to correspond to option.strikes)
view.result.data <- results.ViewComputationResultModel (view.result)[[calc.config]]
results <- data.frame (
  strikes = option.strikes,
  lapply (requirements, function (requirement) {
    columns <- columns.ViewComputationResultModel (view.result.data, requirement)
    sapply (positions, function (position) {
      firstValue.ViewComputationResultModel (view.result.data[position,], columns)
    }, USE.NAMES = FALSE)}))

# Print the results data frame
print (results)

# Plot the column pairs
plot (results$strikes, results$impliedVol)
plot (results$strikes, results$price)
plot (results$strikes, results$forwardDelta)
plot (results$strikes, results$forwardGamma)
plot (results$strikes, results$dualDelta)
plot (results$strikes, results$dualGamma)
plot (results$strikes, results$forwardVega)
plot (results$strikes, results$forwardVanna)
plot (results$strikes, results$forwardVomma)
