##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts the duration sub-message to a real (number of seconds)
calculationDuration.ViewComputationResultModel <- function (msg) {
  msg$seconds + msg$nanos / 1000000000
}

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
    valueName <- new.ValueRequirement (specificationName, specificationProperties)
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
    escaped <- gsub ("(\"|\\\\)", "\\\\\\1", valueName)
    paste ("`", escaped, "` = values[[\"", escaped, "\"]]", sep = "")
  }))
  cmd <- append (cmd, "row.names = \"identifier\", check.names=FALSE)")
  cmd <- paste (cmd, collapse = ", ")
  eval (parse (text = cmd))
}

# Converts the results Fudge message payload to a list of data.frame objects
results.ViewComputationResultModel <- function (msg) {
  configurations <- msg[1]
  results <- msg[2]
  result <- list ()
  if (length (configurations) > 0) {
    if (length (configurations) > 1) {
      for (index in seq (from = 1, to = length (configurations))) {
        result[[configurations[index]]] <- .configurationResults.ViewComputationResultModel (results[[index]])
      }
    } else {
      result[[configurations]] <- .configurationResults.ViewComputationResultModel (results)
    }
  }
  result
}

# Find the column names that satisfy a given value requirement name (and properties)
columns.ViewComputationResultModel <- function (data, valueRequirement) {
  name <- name.ValueRequirement (valueRequirement)
  properties <- properties.ValueRequirement (valueRequirement)
  columns <- colnames (data)
  columns[sapply (columns, function (x) {
    x.name <- name.ValueRequirement (x)
    if (name == x.name) {
      x.properties <- properties.ValueRequirement (x)
      if (satisfiedBy.ValueProperties (properties, x.properties)) {
        TRUE
      } else {
        FALSE
      }
    } else {
      FALSE
    }
  })]
}

# Find the first non-NA value from a data frame row
firstValue.ViewComputationResultModel <- function (row, columns) {
  if (length (row.names (row)) == 1) {
    if (length (columns) > 0) {
      a.columns <-  columns[sapply (columns, function (x) { !is.na (row[[x]]) })]
      if (length (a.columns) > 0) {
        row[[a.columns[[1]]]]
      } else {
        NA
      }
    } else {
      NA
    }
  } else {
    NA
  }
}

# Extract a direct column list from a configuration result
.column.ViewComputationResultModel <- function (data, col) {
  values <- list ()
  for (field in fields.FudgeMsg (data)) {
    computationTargetIdentifier <- NULL
    specificationProperties <- NULL
    specificationName <- NULL
    value <- NULL
    for (x in fields.FudgeMsg (field$Value)) {
      name <- x$Name
      if (name == "specification") {
        for (y in fields.FudgeMsg (x$Value)) {
          name <- y$Name
          if (name == "properties") {
            specificationProperties <- .toString.ValueProperties (y$Value)
          } else {
            if (name == "valueName") {
              specificationName <- y$Value
            } else {
              if (name == "computationTargetIdentifier") {
                computationTargetIdentifier <- y$Value
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
    valueReq <- new.ValueRequirement (specificationName, specificationProperties)
    if (valueReq %in% col) {
      values[[computationTargetIdentifier]] <- value
    }
  }
  values
}

# Extract a direct column list from a result
column.ViewComputationResultModel <- function (data, config, col) {
  results <- data@msg$results
  configs <- results[1]
  results <- results[2]
  if (length (configs) > 0) {
    if (length (configs) > 1) {
      result <- NA
      for (index in seq (from = 1, to = length (configs))) {
        if (configs[index] == config) {
          result <- .column.ViewComputationResultModel (results[[index]], col)
        }
      }
      result
    } else {
      if (configs == config) {
        .column.ViewComputationResultModel (results, col)
      } else {
        NA
      }
    }
  } else {
    NA
  }
}

# Converts the live data Fudge message payload to a data.frame object
liveData.ViewComputationResultModel <- function (msg) {
  liveData <- msg[1]
  if (!is.list (liveData)) {
    liveData <- list (liveData)
  }
  specification <- sapply (liveData, function (x) { x$specification })
  value <- sapply (liveData, function (x) { x$value})
  valueName <- sapply (specification, function (x) { x$valueName })
  identifier <- sapply (specification, function (x) { x$computationTargetIdentifier })
  data.frame (ValueName = valueName, Identifier = identifier, Value = value)
}

# Brings declarations for ViewComputationResultModel into scope
Install.ViewComputationResultModel <- function (stub) {
  stub.ViewComputationResultModel <- stub$begin ("ViewComputationResultModel", Category.VIEW)
  .object.FudgeMsg (stub.ViewComputationResultModel)
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "viewProcessId")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "viewCycleId")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "valuationTime")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "calculationTime")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "calculationDuration", "calculationDuration.ViewComputationResultModel")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "versionCorrection")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "results", "results.ViewComputationResultModel")
  .field.object.FudgeMsg (stub.ViewComputationResultModel, "liveData", "liveData.ViewComputationResultModel")
  stub.ViewComputationResultModel$func (
    "column",
    "Find column from a data result",
    "Finds a column with a given value specification from a named configuration within a result object. A list is returned with the column values and labels corresponding to the computation target identifiers from each value",
    list (data = "The result object to process",
          config = "The name of the configuration to process",
          col = "The value specification to extract - e.g. as returned from columns.ViewComputationResultModel"),
    "OpenGamma:::column.ViewComputationResultModel (data, config, col)")
  stub.ViewComputationResultModel$func (
    "columns",
    "Find a set of satisfying columns from a data frame",
    "Finds the column names from a data frame that match the named value and can satisfy any constraints on the requirement.",
    list (data = "The data frame to search the columns of",
          valueRequirement = "The value requirement string to match"),
    "OpenGamma:::columns.ViewComputationResultModel (data, valueRequirement)")
  stub.ViewComputationResultModel$func (
    "firstValue",
    "Get the first non-NA value from a data frame row",
    "Returns the first non-NA value from the row. Typically the columns requested are a subset that can satisfy a given value requirement. This will then return the first usable value found. Values appear in multiple columns because a column is created in the data frame for each value name/properties pair. Differences in, for example, the function identifier may mean that there is not a single column containing all of the desired values requested in a view definition.",
    list (row = "The data frame row", columns = "Vector of column names to look in"),
    "OpenGamma:::firstValue.ViewComputationResultModel (row, columns)")
  stub.ViewComputationResultModel$end ()
}
