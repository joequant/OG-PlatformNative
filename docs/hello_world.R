### @export "init"
library(OpenGamma)
Init()

### @export "fetch-security"
security <- FetchSecurity("CUSIP~023135106")
security

### @export "print-security"
print(security)

### @export "expand-security"
ExpandSecurity(security)

### @export "expand-equity-security"
ExpandEquitySecurity(security)

### @export "get-security-attributes"
GetSecurityName(security)
GetSecurityType(security)

### @export "isin"
security <- FetchSecurity("ISIN~US0231351067")
GetSecurityName(security)

### @export "synthetic"
security <- FetchSecurity("OG_SYNTHETIC_TICKER~AMZN")
GetSecurityName(security)

### @export "unique-id"
security <- FetchSecurity(uniqueId="DbSec~1176")
GetSecurityName(security)

### @export "portfolios"
Portfolios()

### @export "cash-portfolio"
cash.portfolio <- FetchPortfolio("DbPrt~1012")
GetPortfolioName(cash.portfolio)

### @export "node"
node <- GetPortfolioRootNode(cash.portfolio)
GetPortfolioNodeName(node)
GetPortfolioNodeUniqueId(node)

### @export "positions"
positions <- GetPortfolioNodePositions(node)
p <- positions[[1]]
print(p)
ExpandSecurity(GetPositionSecurity(p))
