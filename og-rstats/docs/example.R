library(rjson)

library(OpenGamma)
Init()


# Helper function to get a value from a view result (assumes the only node is the root node)
get.result <- function (view.result, value.requirement.name, calc.config = "Default") {
    data <- results.ViewComputationResultModel (view.result)[[calc.config]]
    root.node <- data[which (data$type == "PORTFOLIO_NODE"),]
    columns <- columns.ViewComputationResultModel (data, value.requirement.name)
    firstValue.ViewComputationResultModel (root.node, columns)
}

### @export "create-fx-option-security"
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

ExpandFXOptionSecurity(security)

security.id <- StoreSecurity (security)
security.id

### @export "create-portfolio"
position <- PortfolioPosition (security.id, 1)
node <- PortfolioNode (name = "Example", positions = position)
portfolio <- Portfolio ("Example Portfolio", node)
portfolio.id <- StorePortfolio (portfolio)

ExpandPortfolio(portfolio)

portfolio.id

### @export "create-portfolio-view"
requirements <- c (ValueRequirementNames.Present.Value)
view <- ViewDefinition ("Example View", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)
view.id

### @export "create-view-client"
StaticMarketDataViewClient
view.client.descriptor <- StaticMarketDataViewClient (view.id)
view.client <- ViewClient (view.client.descriptor, FALSE)
ConfigureViewClient (view.client, list (EnableCycleAccess ()))
TriggerViewCycle (view.client)
view.result.raw <- GetViewResult (view.client, -1)
view.result.raw

results.ViewComputationResultModel
results.ViewComputationResultModel(view.result.raw)

snapshot <- SnapshotViewResult (view.client)
snapshot <- SetSnapshotName (snapshot, "Example")
snapshot
snapshot.id <- StoreSnapshot (snapshot)

view.client.descriptor <- TickingSnapshotViewClient (view.id, unversioned.Identifier (snapshot.id))
view.client <- ViewClient (view.client.descriptor, FALSE)
view.result.snapshot <- GetViewResult (view.client, -1)

get.result (view.result.snapshot, ValueRequirementNames.Present.Value)
