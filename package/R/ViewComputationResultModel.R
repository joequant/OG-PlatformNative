##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts the results from a configuration into a data.frame object
.configurationResults.ViewComputationResultModel <- function (msg) {
  computationTargetType <- c ()
  computationTargetIdentifier <- c ()
  values <- list ()
  for (field in fields.FudgeMsg (msg)) {
    specification <- field$Value$specification
    v <- specification$computationTargetIdentifier
    i <- which (computationTargetIdentifier == v)
    if (length (i) == 0) {
      computationTargetIdentifier <- append (computationTargetIdentifier, v)
      computationTargetType <- append (computationTargetType, specification$computationTargetType)
      i <- length (computationTargetIdentifier)
    }
    valueName <- paste (specification$valueName, "{", toString (fromFudgeMsg.ValueProperties (specification$properties)), "}", sep = "")
    column <- values[[valueName]]
    if (is.null (column)) {
      column <- c ()
    }
    while (length (column) < i) {
      column <- append (column, NA)
    }
    v <- field$Value$value
    if (is.FudgeMsg (v)) {
      v <- "FudgeMsg"
    }
    column[[i]] <- v
    values[[valueName]] <- column
  }
  values <- lapply (values, function (x) {
    while (length (x) < length (computationTargetIdentifier)) {
      x <- append (x, NA)
    }
    x
  })
  cmd <- c ("data.frame (identifier = computationTargetIdentifier, type = computationTargetType")
  cmd <- append (cmd, sapply (names (values), function (valueName) {
    paste ("`", valueName, "` = values[[\"", gsub ("(\"|\\\\)", "\\\\\\1", valueName), "\"]]", sep = "")
  }))
  cmd <- append (cmd, "row.names = \"identifier\")")
  cmd <- paste (cmd, collapse = ", ")
  eval (parse (text = cmd))
}

# Converts the results Fudge message payload to a list of data.frame objects
.results.ViewComputationResultModel <- function (msg) {
  configurations <- msg[1]
  results <- msg[2]
  result <- list ()
  if (is.list (configurations)) {
    if (length (configurations) > 0) {
      for (index in seq (from = 1, to = length (configurations))) {
        result[[configurations[[index]]]] <- .configurationResults.ViewComputationResultModel (results[[index]])
      }
    }
  } else {
    result[[configurations]] <- .configurationResults.ViewComputationResultModel (results)
  }
  result
}

# Brings declarations for ViewComputationResultModel into scope
Install.ViewComputationResultModel <- function () {
  object.FudgeMsg ("ViewComputationResultModel")
  .field.ViewComputationResultModel ("viewProcessId")
  .field.ViewComputationResultModel ("viewCycleId")
  .field.ViewComputationResultModel ("valuationTime")
  .field.ViewComputationResultModel ("calculationTime")
  .field.ViewComputationResultModel ("calculationDuration")
  .field.ViewComputationResultModel ("versionCorrection")
  .field.ViewComputationResultModel ("results", ".results.ViewComputationResultModel")
  .field.ViewComputationResultModel ("liveData")
}
