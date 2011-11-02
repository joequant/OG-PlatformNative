##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Wraps a ManageableYieldCurveSnapshot instance to a YieldCurveSnapshot
fromFudgeMsg.ManageableYieldCurveSnapshot <- function (x) {
  fromFudgeMsg.YieldCurveSnapshot (x)
}

# Unpack the curve values to a data frame
.values.YieldCurveSnapshot <- function (x) {
  fromFudgeMsg.UnstructuredMarketDataSnapshot (x)
}

# Brings declarations for YieldCurveSnapshot into scope
Install.YieldCurveSnapshot <- function () {
  object.FudgeMsg ("YieldCurveSnapshot")
  .field.YieldCurveSnapshot ("valuationTime")
  .field.YieldCurveSnapshot ("values", ".values.YieldCurveSnapshot")
}

# Default conversion to data frame
as.data.frame.YieldCurveSnapshot <- function (x, row.names, optional, ...) {
  values.YieldCurveSnapshot (x)
}
