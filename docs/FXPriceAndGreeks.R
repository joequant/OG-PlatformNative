### @export "setup"
library(OpenGamma)
Init ()

### @export "constants"
today <- as.POSIXct ("2012-04-24", "GMT") # price option from 24/04/2012
expiry <- as.POSIXct ("2012-10-24", "GMT") # expiry of the option
settlementDate <- expiry + as.difftime (2, format = "%X", units = "days")
notional <- 1000000
spotFX <- 1.34
option.strikes <- seq (0.8, 2.0, length.out = 50)
option.strikes

### @export "create-positions"
positions <- c ()
for(option.strike in option.strikes) {
  callAmount <- notional / option.strike

  security <- FXOptionSecurity (
    name = paste("Long put USD,", notional, ", call EUR",
                        callAmount, "on", expiry),
    callAmount = callAmount,
    callCurrency = "EUR",
    putCurrency = "USD",
    putAmount = notional,
    expiry,
    exerciseType = EuropeanExerciseType (),
    long = TRUE,
    settlementDate)
  security.id <- StoreSecurity (security)
  positions <- c (positions,
                  PortfolioPosition(security = security.id, quantity = 1))
}

### @export "create-portfolio"
node <- PortfolioNode (name = "FX Options", positions = positions)
portfolio <- Portfolio (name = "FX Options Temporary Portfolio",
                            rootNode = node)
portfolio.id <- StorePortfolio (portfolio)
portfolio.id

### @export "position-ids"
portfolio <- FetchPortfolio(portfolio.id)
positions <- sapply(
                 fields.FudgeMsg(portfolio$root$positions),
                 function (x) { x$Value$uniqueId }
             )

### @export "requirements"
requirements <- list (
  impliedVol = new.ValueRequirement(
                    ValueRequirementNames.Implied.Volatility,
                    paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=Spline",
                         "PDEDirection=Forward",
                    sep = ", ")),
  price = new.ValueRequirement(
                    ValueRequirementNames.Present.Value,
                    paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=SABR",
                         "PDEDirection=Forward",
                    sep = ", ")),
  forwardDelta = new.ValueRequirement(
                    ValueRequirementNames.Forward.Delta, paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=SABR",
                         "PDEDirection=Forward",
                     sep = ", ")),
  forwardGamma = new.ValueRequirement(
                     ValueRequirementNames.Forward.Gamma, paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=SABR",
                         "PDEDirection=Forward",
                      sep = ", ")),
  dualDelta = new.ValueRequirement(
                     ValueRequirementNames.Dual.Delta, paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=SABR",
                         "PDEDirection=Forward",
                     sep = ", ")),
  dualGamma = new.ValueRequirement(
                     ValueRequirementNames.Dual.Gamma, paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=SABR",
                         "PDEDirection=Forward",
                     sep = ", ")),
  forwardVega = new.ValueRequirement(
                      ValueRequirementNames.Forward.Vega, paste (
                         "CalculationMethod=LocalVolatilityPDE",
                         "SmileInterpolator=SABR",
                         "PDEDirection=Forward",
                     sep = ", ")),
  forwardVanna = new.ValueRequirement(
                       ValueRequirementNames.Forward.Vanna, paste (
                          "CalculationMethod=LocalVolatilityPDE",
                          "SmileInterpolator=SABR",
                          "PDEDirection=Forward",
                      sep = ", ")),
  forwardVomma = new.ValueRequirement(
                       ValueRequirementNames.Forward.Vomma, paste (
                          "CalculationMethod=LocalVolatilityPDE",
                          "SmileInterpolator=SABR",
                          "PDEDirection=Forward",
                      sep = ", "))
)

### @export "define-view"
view <- ViewDefinition ("FX Option Example", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
view.id

### @export "view-client"
view.client <- ViewClient (view.id, useSharedProcess = FALSE)
view.result <- GetViewResult (viewClient = view.client, waitForResult = -1)

### @export "extract-results"
calc.config <- "Default"
view.result.model <- results.ViewComputationResultModel(view.result)
view.result.data <- view.result.model[[calc.config]]

get.results.for.position <- function(position, columns) {
    result <- view.result.data[position,]
    return(firstValue.ViewComputationResultModel(result, columns))
}

get.results.for.requirement <- function(requirement) {
    columns <- columns.ViewComputationResultModel(
                view.result.data, requirement)

    return(sapply(positions, get.results.for.position,
                    columns, USE.NAMES = FALSE))
}

results <- data.frame (
    strikes = option.strikes,
    lapply(requirements, get.results.for.requirement)
)

### @export "results"
print(results[0:10,])

pdf("dexy--price.pdf", width=4, height=4)
plot(results$strikes, results$price, pch=19, col="gray", main="Price", ylab="Price", xlab="Strike Price")
dev.off()

### @export "plots"
do.plot <- function(name, y) {
    pdf(name, width=3.5, height=3.5)
    par(mar=c(2.5,2.5,3,1))
    plot(results$strikes, results[[y]], pch=19, col="gray", main=y, ylab="", xlab="Strike Price")
    dev.off()
}

do.plot("dexy--implied-vol.pdf", "impliedVol")
do.plot("dexy--forward-delta.pdf", "forwardDelta")
do.plot("dexy--forward-gamma.pdf", "forwardGamma")
do.plot("dexy--dual-delta.pdf", "dualDelta")
do.plot("dexy--dual-gamma.pdf", "dualGamma")
do.plot("dexy--forward-vega.pdf", "forwardVega")
do.plot("dexy--forward-vanna.pdf", "forwardVanna")
do.plot("dexy--forward-vomma.pdf", "forwardVomma")

