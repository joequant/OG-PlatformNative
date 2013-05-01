##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Produce a data frame from a Fudge encoded map
fudgeMsgToDataFrame.Map <- function (x, keyFun = NULL, valueFun = NULL) {
  fn <- keyFun
  if (is.null (fn)) {
    fn <- function (x) { x }
  }
  keys <- sapply (field.FudgeMsg (x, 1), fn)
  if (is.list (keys)) {
    x
  } else {
    fn <- valueFun
    if (is.null (fn)) {
      fn <- function (x) { x }
    }
    values <- sapply (field.FudgeMsg (x, 2), fn)
    if (is.list (values)) {
      x
    } else {
      data.frame (Key = keys, Value = values, row.names = "Key")
    }
  }
}
