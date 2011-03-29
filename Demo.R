# Demonstration scripts
source ("DemoInternal.R")


# 1. Shift the points on the USD yield curve used by 0.05 points

YieldCurveShiftBy5 <- function (yield.curve) {
  sapply (yield.curve, function (val) val + 0.05)
}

yield.curve.shift.id <- RegisterFunction (YieldCurveShiftBy5, input.value.spec = c (YieldCurve ("USD")), output.value.spec = YieldCurve ("USD"))

Example1 <- function () {
  Execute (view = demo.swap.view.id, custom.functions = c (yield.curve.shift.id))
}


# 2. Run a view at the same data snapshot date with different shifts

shift.amount.uid <- UniqueIdentifier ("Demo", "Shift Amount")

YieldCurveShiftByX <- function (yield.curve, shift.amount) {
  sapply (yield.curve, function (val) val + shift.amount)
}

yield.curve.shift.id.2 <- RegisterFunction (YieldCurveShiftByX, input.value.spec = c (YieldCurve (), MarketValue (shift.amount.uid)), output.value.spec = c (YieldCurve ()))

example.2.shifts <- list (0.01, 0.02, 0.03, 0.04, 0.05)

Example2 <- function () {
  snapshot.time <- Sys.time ()
  lapply (example.2.shifts,
          function (shift.amount) {
            data = list ()
            data[[shift.amount.uid]] = shift.amount
            Execute (view = demo.swap.view.id,
                     custom.functions = c (yield.curve.shift.id.2),
                     timestamp = snapshot.time,
                     data.overrides = data)
          })
  # Each iteration of lapply passes in the shift amount as a data override. When the engine executes our function, the data we injected
  # is then available. An alternative would be to register muliple versions of the function with specific shift amounts but that would
  # require potentially costly dependancy graph rebuilds on each iteration.
}


# 3. Run a view with adjustments to the data points making up the curve

YieldCurveAdjustDataBundle <- function (curve.data.bundle) {
  curve.data.bundle["US00O/N Index"] <- curve.data.bundle["US00O/N Index"] + 0.001
  curve.data.bundle["USDR5 Curncy"] <- curve.data.bundle["USDR5 Curncy"] - 0.001
  curve.data.bundle
}

yield.curve.data.adjuster.id <- RegisterFunction (YieldCurveAdjustDataBundle, input.value.spec = c (YieldCurveDataBundle ("USD")), output.value.spec = YieldCurveDataBundle ("USD"))

Example3 <- function () {
  Execute (view = demo.swap.view.id, custom.functions = c (yield.curve.data.adjuster.id))
}


# 4. Could register a whole load of custom functions once

VolatilitySurfaceAdjuster <- function (volatility.surface) {
  volatility.surface
}

yield.curve.adjuster.id <- RegisterFunction (YieldCurveShiftBy5, input.value.spec = c (YieldCurve ()), output.value.spec = YieldCurve ())

yield.curve.data.adjuster.id <- RegisterFunction (YieldCurveAdjustDataBundle, input.value.spec = c (YieldCurveDataBundle ("USD")), output.value.spec = YieldCurveDataBundle ())

volatility.surface.adjuster.id <- RegisterFunction (VolatilitySurfaceAdjuster, input.value.spec = c (VolatilitySurface ()), output.value.spec = VolatilitySurface ())

my.custom.functions <- c (yield.curve.adjuster.id, yield.curve.data.adjuster.id, volatility.surface.adjuster.id)

Example4 <- function () {
  Execute (view = demo.swap.view.id, custom.functions = my.custom.functions)
  # The custom function bundle can be referred to without explicitly listing each
}


# 5. Iterate over some historical data with the custom function bundle

sample.dates <- c ("Mon Mar 21 09:30:00 2011", "Tue Mar 22 09:30:00 2011", "Wed Mar 23 09:30:00 2011", "Thu Mar 24 09:30:00 2011", "Fri Mar 25 09:30:00 2011")
sample.data <- data.frame ("Date" = sample.dates, "US00O/N Index" = Random (5), "US00S/N Index" = Random (5), "US0001 Index" = Random (5), "US0002W Index" = Random (5), check.names = FALSE)
# In reality this would be loaded from a file or database

Example5 <- function () {
  result <- list ()
  overrides <- colnames (sample.data)[-1]
  for (i in 1:nrow (sample.data)) {
    row <- sample.data[i,]
    timestamp <- as.vector (row$Date)
    data <- lapply (overrides, function (j) as.vector (row[j]))
    cycle <- Execute (view = demo.swap.view.id, custom.functions = my.custom.functions, timestamp = timestamp, data.overrides = data)
    result[[timestamp]] <- cycle
  }
  result
}
