##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates specifying curve names at the position level.

Init ()

# Create the security
print ("Creating security")
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
    floatingReferenceRateId = "BLOOMBERG_TICKER~US0003M Index",
    floatingRateType = "IBOR"),
  receiveLeg = FixedInterestRateLeg (
    dayCount = "30U/360",
    frequency = "Semi-annual",
    regionId = "FINANCIAL_REGION~US+GB",
    businessDayConvention = "Modified Following",
    notional = InterestRateNotional ("USD", 40326000),
    eom = FALSE,
    rate = 0.027))
security.id <- StoreSecurity (security)

# Create a portfolio containing two positions in this security
print ("Creating portfolio")
position.forward <- SetPositionAttribute (PortfolioPosition (security.id, 1), "*.DEFAULT_ForwardCurve", "FORWARD_3M")
position.funding <- SetPositionAttribute (PortfolioPosition (security.id, 1), "*.DEFAULT_ForwardCurve", "FUNDING")
node <- PortfolioNode (name = "Example", positions = list (position.forward, position.funding))
portfolio <- Portfolio ("Example Portfolio", node)
portfolio.id <- StorePortfolio (portfolio)

# Create a view on this portfolio
print ("Creating view")
requirements <- c (ValueRequirementNames.Present.Value)
view <- ViewDefinition ("Example View", portfolio.id, requirements)
view.id <- StoreViewDefinition (view)

# Create a view client to get an initial calculation from current market data
print ("Creating market data view client")
view.client.descriptor <- StaticMarketDataViewClient (view.id)
view.client <- ViewClient (view.client.descriptor, FALSE)
TriggerViewCycle (view.client)
view.result <- GetViewResult (view.client, -1)

# Format a data frame with the results
view.result.frame <- results.ViewComputationResultModel (view.result)$Default
