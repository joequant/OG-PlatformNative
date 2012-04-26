### @export "libraries"
library(OpenGamma)
OpenGamma::Init()
options(useFancyQuotes=FALSE)

library(xts)

### @export "fetch-ts"
ticker <- "DbHts~1002"
ticker.ts <- FetchTimeSeries(ticker)
ticker.ts[1:30] # just show a few points

### @export "plot-ts"
pdf("dexy--dbhts.pdf")
plot(ticker.ts)
dev.off()

### @export "convert-xts"
ticker.xts <- as.xts.TimeSeries(ticker.ts)
ticker.xts[1:10,] # just show a few points

### @export "plot-xts"
pdf("dexy--dbhts-xts.pdf")
plot(ticker.xts)
dev.off()

### @export "security-amazon-tickers"
tickers <- c(
             "CUSIP~023135106",
             "ISIN~US0231351067",
             "OG_SYNTHETIC_TICKER~AMZN"
             )

for (t in tickers) {
    print("==================================================")
    print(t)
    print("--------------------")
    security <- FetchSecurity(t)
    print(security)
}

### @export "security-amazon-uniqueid"
security <- FetchSecurity(uniqueId="DbSec~1176")
print(security)

### @export "security-properties"
security
ExpandSecurity(security)
ExpandEquitySecurity(security)
GetSecurityName(security)
GetSecurityType(security)

### @export "save-vars"
library(rjson)
var_file <- file("dexy--r-vars.json", "w")
vars = list(ticker=ticker)
writeLines(toJSON(vars), var_file)
close(var_file)
