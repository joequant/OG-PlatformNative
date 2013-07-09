##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Unpack the curve values to a data frame
values.YieldCurveSnapshot <- function (x) {
  fromFudgeMsg.UnstructuredMarketDataSnapshot (x)
}

# Brings declarations for YieldCurveSnapshot into scope
Install.YieldCurveSnapshot <- function (stub) {
  stub.YieldCurveSnapshot <- stub$begin ("YieldCurveSnapshot", Category.MARKET_DATA)
  .object.FudgeMsg (stub.YieldCurveSnapshot)
  .field.object.FudgeMsg (stub.YieldCurveSnapshot, "valuationTime")
  .field.object.FudgeMsg (stub.YieldCurveSnapshot, "values", "values.YieldCurveSnapshot")
  stub.YieldCurveSnapshot$asDataFrame ("values.YieldCurveSnapshot (x)")
  stub.YieldCurveSnapshot$fromFudgeMsg ("fromFudgeMsg.YieldCurveSnapshot (msg)", "ManageableYieldCurveSnapshot")
  stub.YieldCurveSnapshot$end ()
}
