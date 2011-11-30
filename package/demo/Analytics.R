##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Demonstrates the full use of the system for performing calculations by creating a security, a suitable view,
# a market data snapshot from the view, and then executing the view with peturbations on the market data snapshot.

# Create the security
swap <- EquityVarianceSwapSecurity (
  name = "USD 1MM, strike=0.5, maturing 2012-11-01",
  currency = "USD",
  strike = 0.5,
  notional = 1000000,
  parameterizedAsVariance = TRUE,
  annualizationFactor = 250,
  firstObservationDate = "2010-11-01",
  lastObservationDate = "2012-11-01",
  settlementDate = "2012-11-01",
  observationFrequency = "Daily",
  regionId = "ISO_COUNTRY_ALPHA2~US",
  spotUnderlyingId = "BLOOMBERG_TICKER~DJX Index")
swap.id <- StoreSecurity (swap)

# Create a portfolio containing a position in this security
position <- PortfolioPosition (swap.id, 1)
node <- PortfolioNode (name = "Swap", positions = position)
portfolio <- Portfolio ("Example Portfolio", node)
portfolio.id <- StorePortfolio (portfolio)

# Create a view on this portfolio
requirements <- c ("Present Value", "ValueDelta", "ValueGamma", "ValueVega", "ValueTheta", "ValueRho", "ValueZeta", "ValuePhi", "ValueZomma", "ValueSpeed", "ValueVanna", "ValueVomma")
view <- ViewDefinition (name = "Example View", portfolio = portfolio.id, portfolioRequirements = requirements)
view.id <- StoreViewDefinition (view)

# TODO
