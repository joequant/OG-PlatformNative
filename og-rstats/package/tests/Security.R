##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Tests the encoding and retrieval of securities

source ("TestUtil.R")

# Create a security
LOGDEBUG ("Creating security")
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
    floatingReferenceRateId = "Reference Rate Simple Name~USD LIBOR 3m",
    floatingRateType = "IBOR"),
  receiveLeg = FixedInterestRateLeg (
    dayCount = "30U/360",
    frequency = "Semi-annual",
    regionId = "FINANCIAL_REGION~US+GB",
    businessDayConvention = "Modified Following",
    notional = InterestRateNotional ("USD", 40326000),
    eom = FALSE,
    rate = 0.027))

# Store into the session database
LOGDEBUG ("Storing security")
security.id <- StoreSecurity (security)
LOGDEBUG (security.id)

# Fetch the security by unique identifier
LOGDEBUG ("Fetch security by UniqueId")
security <- FetchSecurity (security.id)
LOGDEBUG (security)
ASSERT_EQUAL (GetSecurityType (security), "SWAP")
