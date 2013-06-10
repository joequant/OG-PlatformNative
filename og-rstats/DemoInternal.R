# Temporary demonstration file

debug.counter <- 1
DebugUid <- function (what) {
  counter <- debug.counter
  debug.counter <<- counter + 1
  UniqueIdentifier ("Demo", paste (what, "-", counter, sep = ""))
}

demo.bond.portfolio.id <- "Demo::Bond-Portfolio"
demo.bond.view.id <- "Demo::Bond-View"
demo.swap.portfolio.id <- "Demo::Swap-Portfolio"
demo.swap.view.id <- "Demo::Swap-View"

Positions <- function (portfolio) {
  if (portfolio == demo.bond.portfolio.id) {
    security <- c ("T 7 1/8 02/15/23", "T 8 11/15/21", "T 4 7/8 07/31/11")
    quantity <- c (2080, 1020, 4040)
    data.frame (security, quantity)
  } else if (portfolio == demo.swap.portfolio.id) {
    security <- c ("IR Swap 100MM USD 2030-07-15 - 1% / LIBOR 6m", "IR Swap 50MM USD 2040-08-07 - 1% / LIBOR 12m", "IR Swap 20MM USD 2035-11-04 - LIBOR 3m / 1%")
    quantity <- c (1, 1, 1)
    data.frame (security, quantity)
  } else {
    NULL
  }
}

# General housekeeping

UniqueIdentifier <- function (scheme, value) {
  paste (scheme, "::", value, sep = "")
}

ValidateUniqueIdentifier <- function (uid) {
  if (is.null (uid)) {
    stop ("Not a valid UniqueIdentifier")
  }
  uid
}

ValidatePosition <- function (uid) {
  ValidateUniqueIdentifier (uid)
}

ValidateSecurity <- function (uid) {
  ValidateUniqueIdentifier (uid)
}

ValidatePortfolio <- function (uid) {
  ValidateUniqueIdentifier (uid)
}

# Conversion functions

YieldCurveToFudgeMsg <- function (yield.curve) {
  # TODO: This is an OG supplied function that will convert from the R representation of the analytic object to the Fudge representation
  yield.curve
}

FudgeMsgToYieldCurve <- function (fudge.msg) {
  # TODO: This is an OG supplied function that will convert from the Fudge representation of the analytic object to a more useful R representation
  fudge.msg
}

VolatilitySurfaceToFudgeMsg <- function (volatility.surface) {
  # TODO: This is an OG supplied function that will convert from the R representation of the analytic object to the Fudge representation
  volatility.surface
}

FudgeMsgToVolatilitySurface <- function (fudge.msg) {
  # TODO: This is an OG supplied function that will convert from the Fudge representation of the analytic object to a more useful R representation
  fudge.msg
}

# Computation target

ComputationTargetSpecification <- function (uid = NULL, position = NULL, security = NULL, portfolio = NULL) {
  if (!is.null (uid)) {
    if (!is.null (position) || !is.null (security) || !is.null (portfolio)) {
      stop ("Can only specify one of unique identifier, position, security or portfolio")
    }
    c (uid = ValidateUniqueIdentifier (uid))
  } else if (!is.null (position)) {
    if (!is.null (security) || !is.null (portfolio)) {
      stop ("Can only specify one of unique identifier, position, security or portfolio")
    }
    c (position = ValidatePosition (position))
  } else if (!is.null (security)) {
    if (!is.null (portfolio)) {
      stop ("Can only specify one of unique identifier, position, security or portfolio")
    }
    c (security = ValidateSecurity (security))
  } else if (!is.null (portfolio)) {
    c (portfolio = ValidatePortfolio (portfolio))
  } else {
    stop ("Must specify one of unique identifier, position, security or portfolio")
  }
}

# Value requirements

DirtyPrice.name <- "DirtyPrice"
DirtyPrice.ctspec <- function (ctspec) c (name = DirtyPrice.name, target = ctspec)
DirtyPrice.position <- function (position) DirtyPrice.ctspec (ComputationTargetSpecification (position = position))
DirtyPrice <- function () DirtyPrice.ctspec (NULL)

CleanPrice.name <- "CleanPrice"
CleanPrice.ctspec <- function (ctspec) c (name = CleanPrice.name, target = ctspec)
CleanPrice.position <- function (position) CleanPrice.ctspec (ComputationTargetSpecification (position = position))
CleanPrice <- function () CleanPrice.ctspec (NULL)

FairValue.name <- "FairValue"
FairValue.ctspec <- function (ctspec) c (name = FairValue.name, target = ctspec)
FairValue.uid <- function (uid) FairValue.ctspec (ComputationTargetSpecification (uid = uid))
FairValue.position <- function (position) FairValue.ctspec (ComputationTargetSpecification (position = position))
FairValue.security <- function (security) FairValue.ctspec (ComputationTargetSpecification (security = security))
FairValue.portfolio <- function (portfolio) FairValue.ctspec (ComputationTargetSpecification (portfolio = portfolio))
FairValue <- function () FairValue.ctspec (NULL)

MarketValue.name <- "Market_Value"
MarketValue.ctspec <- function (ctspec) c (name = MarketValue.name, target = ctspec)
MarketValue <- function (uid) MarketValue.ctspec (ComputationTargetSpecification (uid = uid))

ParRate.name <- "ParRate"
ParRate.ctspec <- function (ctspec) c (name = ParRate.name, target = ctspec)
ParRate <- function () ParRate.ctspec (NULL)

PresentValue.name <- "PresentValue"
PresentValue.ctspec <- function (ctspec) c (name = PresentValue.name, target = ctspec)
PresentValue <- function () PresentValue.ctspec (NULL)

PV01.name <- "PV01"
PV01.ctspec <- function (ctspec) c (name = PV01.name, target = ctspec)
PV01 <- function () PV01.ctspec (NULL)

VolatilitySurface.name <- "VolatilitySurface"
VolatilitySurface.ctspec <- function (ctspec) c (name = VolatilitySurface.name, target = ctspec, input.conversion.function = FudgeMsgToVolatilitySurface, output.conversion.function = VolatilitySurfaceToFudgeMsg)
VolatilitySurface <- function () VolatilitySurface.ctspec (NULL)

YieldCurve.name <- "YieldCurve"
YieldCurve.ctspec <- function (ctspec) c (name = YieldCurve.name, target = ctspec, input.conversion.function = FudgeMsgToYieldCurve, output.conversion.function = YieldCurveToFudgeMsg)
YieldCurve.uid <- function (uid) YieldCurve.ctspec (ComputationTargetSpecification (uid = uid))
YieldCurve <- function (currency.iso = NULL) {
  target <- if (is.null (currency.iso)) NULL else ComputationTargetSpecification (uid = UniqueIdentifier ("CurrencyISO", currency.iso))
  YieldCurve.ctspec (target)
}

YieldCurveDataBundle.name <- "YieldCurveDataBundle"
YieldCurveDataBundle.ctspec <- function (ctspec) c (name = YieldCurveDataBundle.name, target = ctspec)
YieldCurveDataBundle <- function (currency.iso = NULL) {
  target <- if (is.null (currency.iso)) NULL else ComputationTargetSpecification (uid = UniqueIdentifier ("CurrencyISO", currency.iso))
  YieldCurveDataBundle.ctspec (target)
}

ZSpread.name <- "ZSpread"
ZSpread.ctspec <- function (ctspec) c (name = ZSpread.name, target = ctspec)
ZSpread <- function () ZSpread.ctspec (NULL)

# View definition

View <- function (name, portfolio, value.requirements) {
  debug.uid ("View")
}

ViewInfo <- function (view) {
  if (view == demo.bond.view.id) {
    c (name = "Bond Test View", portfolio = demo.bond.portfolio.id, value.requirements = c (CleanPrice (), DirtyPrice (), ZSpread ()))
  } else if (view == demo.swap.view.id) {
    c (name = "Swap Test View", portfolio = demo.swap.portfolio.id, value.requirements = c (ParRate (), PresentValue (), PV01 ()))
  } else {
    c ()
  }
}

# Function registration

RegisterFunction <- function (func, input.value.spec = NULL, output.value.spec = NULL) {
  DebugUid ("Function")
}

# View execution

Random <- function (count) {
  sapply (seq (from = 1, to = count), function (index) runif (1))
}

Jitter <- function (range, values) {
  sapply (values, function (value) { value + runif (1) / range })
}

Execute <- function (view, custom.functions = c (), timestamp = Sys.time (), data.overrides = c ()) {
  if (view == demo.bond.view.id) {
    position <- c ("T 7 1/8 02/15/23 (2080)", "T 8 11/15/21 (1020)", "T 4 7/8 07/31/11 (4040)")
    DirtyPrice <- Jitter (100, c (133.768, 142.921, 102.390))
    CleanPrice <- Jitter (100, c (132.922, 139.938, 101.609))
    ZSpread <- Jitter (1000, c (7.729, 3.043, -23.225))
    data.frame (position, CleanPrice, DirtyPrice, ZSpread)
  } else if (view == demo.swap.view.id) {
    position <- c ("IR Swap 100MM USD 2030-07-15 - 1% / LIBOR 6m (1)", "IR Swap 50MM USD 2040-08-07 - 1% / LIBOR 12m (1)", "IR Swap 20MM USD 2035-11-04 - LIBOR 3m / 1% (1)")
    ParRate <- Jitter (10000, c (0.041085, 0.041510, 0.042185))
    PresentValue <- Jitter (1000, c (43.401, 27.881, -10.312))
    PV01 <- Jitter (10000, c (0.093871, 0.048756, -0.020179))
    data.frame (position, ParRate, PresentValue, PV01)
  } else {
    c ()
  }
}
