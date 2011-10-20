##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Select one or more views to run; this might be a wild-card search string like "*" to
# run all views (not currently supported in OG-Language)
viewSearchPattern <- "Equity Option Test View 1"

# Iterates through the views available, runs a cycle and displays the output
views <- Views (viewSearchPattern)
for (i in seq (from = 1, to = length (views[,1]))) {
  viewIdentifier <- views[i, 1]
  viewName <- views[i, 2]
  viewClient <- ViewClient (viewIdentifier)
  print (paste ("Waiting for result from", viewName))
  viewResultModel <- GetViewResult (viewClient, 30000)
  if (is.ViewComputationResultModel (viewResultModel)) {
    viewResult <- results.ViewComputationResultModel (viewResultModel)
    lapply (names (viewResult), function (calcConfig) {
      result <- viewResult[[calcConfig]]
      print (paste ("Result from", viewName, "calculation config", calcConfig));
      print (result)
      print ("")
    })
    liveData <- liveData.ViewComputationResultModel (viewResultModel)
    print (paste ("Live data from", viewName))
    print (liveData)
    print ("")
  } else {
    warning (paste ("No results from view", viewName, "produced in 30s"))
  }
}
