##
 # Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Converts a value specification into a simple string
.convert.ValueSpecification.label <- function (valueSpecification) {
  valueName <- NULL
  identifier <- NULL
  for (field in fields.FudgeMsg (valueSpecification)) {
    fieldName <- field$Name
    if (fieldName == "valueName") {
      valueName <- field$Value
    } else {
      if (fieldName == "computationTargetIdentifier") {
        identifier <- field$Value
      }
    }
  }
  # TODO: Handle the properties too
  paste (identifier, valueName, sep = "/")
}

# Converts a label into a simple string
.convert.labels <- function (labelType, label) {
  for (i in seq (1, length (label))) {
    if (labelType[[i]] == "com.opengamma.engine.value.ValueSpecification") {
      label[[i]] <- .convert.ValueSpecification.label (label[[i]])
    } else {
      label[[i]] <- toString (label[[i]])
    }
  }
  label
}

# Converts a Fudge representation of a double labelled matrix 2D into a data frame
fromFudgeMsg.DoubleLabelledMatrix2D <- function (msg) {
  m <- msg$`matrix`
  xLabelType <- field.FudgeMsg (m, 0) # X_LABEL_TYPE_ORDINAL (0)
  # xKey <- field.FudgeMsg (m, 1) # X_KEY_ORDINAL (1)
  xLabel <- field.FudgeMsg (m, 2) # X_LABEL_ORDINAL (2)
  yLabelType <- field.FudgeMsg (m, 3) # Y_LABEL_TYPE_ORDINAL (3)
  # yKey <- field.FudgeMsg (m, 4) # Y_KEY_ORDINAL (4)
  yLabel <- field.FudgeMsg (m, 5) # Y_LABEL_ORDINAL (5)
  value <- field.FudgeMsg (m, 6) # VALUE_ORDINAL (6)
  xLabel <- .convert.labels (xLabelType, xLabel)
  yLabel <- .convert.labels (yLabelType, yLabel)
  value <- matrix (value, length (yLabel), length (xLabel), TRUE)
  value <- data.frame (value)
  row.names (value) <- yLabel
  colnames (value) <- xLabel
  value
}

# Brings 2D matrix definitions into scope
Install.DoubleLabelledMatrix2D <- function (stub) {
  stub.DoubleLabelledMatrix2D <- stub$begin ("DoubleLabelledMatrix2D", Category.MISC)
  stub.DoubleLabelledMatrix2D$fromFudgeMsg ("OpenGamma:::fromFudgeMsg.DoubleLabelledMatrix2D (msg)", "DoubleLabelledMatrix2D")
  stub.DoubleLabelledMatrix2D$end ()
}
