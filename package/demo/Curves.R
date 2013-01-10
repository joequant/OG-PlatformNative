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
  name = "Swap: receive 3.60% fixed ACT/360 vs 3m Bank Bill",
  tradeDate = "2013-09-05",
  effectiveDate = "2013-09-05",
  maturityDate = "2015-09-05",
  counterparty = "CParty",
  payLeg = FixedInterestRateLeg (
    dayCount = "Actual/360",
    frequency = "Quarterly",
    regionId = "FINANCIAL_REGION~AU",
    businessDayConvention = "Following",
    notional = InterestRateNotional ("AUD", 15000000),
    eom = TRUE,
    rate = 0.036),
  receiveLeg = FloatingInterestRateLeg (
    dayCount = "Actual/365",
    frequency = "Quarterly",
    regionId = "FINANCIAL_REGION~AU",
    businessDayConvention = "Following",
    notional = InterestRateNotional ("AUD", 15000000),
    eom = TRUE,
    floatingReferenceRateId = "Reference Rate Simple Name~AUD LIBOR 3m",
    floatingRateType = "IBOR"))
security.id <- StoreSecurity (security)

# Create a portfolio containing positions in this security using the available curve configurations.
print ("Creating portfolio")
position.1 <- SetPositionAttribute(PortfolioPosition (security.id, 1), "Present Value.DEFAULT_CurveCalculationConfig", "DefaultThreeCurveAUDConfig")
position.2 <- SetPositionAttribute(PortfolioPosition (security.id, 1), "Present Value.DEFAULT_CurveCalculationConfig", "DiscountingAUDConfig")
position.3 <- SetPositionAttribute(PortfolioPosition (security.id, 1), "Present Value.DEFAULT_CurveCalculationConfig", "ForwardFromDiscountingAUDConfig")
position.4 <- SetPositionAttribute(PortfolioPosition (security.id, 1), "Present Value.DEFAULT_CurveCalculationConfig", "SingleAUDConfig")
node <- PortfolioNode (name = "Example", positions = list (position.1, position.2, position.3, position.4))
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
