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
    specificationName <- NULL
    specificationIdentifier <- NULL
    specificationType <- NULL
    specificationProperties <- NULL
    value <- NULL
    for (x in fields.FudgeMsg (field$Value)) {
      name <- x$Name
      if (name == "specification") {
        for (y in fields.FudgeMsg (x$Value)) {
          name <- y$Name
          if (name == "computationTargetIdentifier") {
            specificationIdentifier <- y$Value
          } else {
            if (name == "computationTargetType") {
              specificationType <- y$Value
            } else {
              if (name == "properties") {
                specificationProperties <- .toString.ValueProperties (y$Value)
              } else {
                if (name == "valueName") {
                  specificationName <- y$Value
                }
              }
            }
          }
        }
      } else {
        if (name == "value") {
          value <- x$Value
        }
      }
    }
    i <- which (computationTargetIdentifier == specificationIdentifier)
    if (length (i) == 0) {
      computationTargetIdentifier <- append (computationTargetIdentifier, specificationIdentifier)
      computationTargetType <- append (computationTargetType, specificationType)
      i <- length (computationTargetIdentifier)
    }
    valueName <- paste (specificationName, "{", specificationProperties, "}", sep = "")
    column <- values[[valueName]]
    if (is.null (column)) {
      column <- c ()
    }
    while (length (column) < i) {
      column <- append (column, NA)
    }
    if (is.FudgeMsg (value)) {
      value <- "FudgeMsg"
    }
    column[[i]] <- value
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

# Converts the live data Fudge message payload to a data.frame object
.liveData.ViewComputationResultModel <- function (msg) {
  liveData <- msg[1]
  specification <- sapply (liveData, function (x) { x$specification })
  value <- sapply (liveData, function (x) { x$value})
  valueName <- sapply (specification, function (x) { x$valueName })
  identifier <- sapply (specification, function (x) { x$computationTargetIdentifier })
  data.frame (ValueName = valueName, Identifier = identifier, Value = value)
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
  .field.ViewComputationResultModel ("liveData", ".liveData.ViewComputationResultModel")
}
