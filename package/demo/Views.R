##
 # Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 #
 # Please see distribution for license.
 ##

# Iterates through the views available, runs a cycle and displays the output
views <- Views ()
discard <- lapply (views, function (view) {
  viewIdentifier <- view[[1]]
  viewName <- view[[2]]
  viewClient <- ViewClient (viewIdentifier)
  print (paste ("Waiting for result from", viewName))
  viewResultModel <- GetViewResult (viewClient, 5000)
  if (is.ViewComputationResultModel (viewResultModel)) {
    viewResult <- results.ViewComputationResultModel (viewResultModel)
    lapply (names (viewResult), function (calcConfig) {
      result <- viewResult[[calcConfig]]
      print (paste ("Result from", viewName, "calculation config", calcConfig));
      print (result)
      print ("")
    })
  } else {
    warning (paste ("No results from view", viewName, "produced in 5s"))
  }
})

