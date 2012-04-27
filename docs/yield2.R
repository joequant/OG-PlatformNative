library ("OpenGamma")
library(xts)
demo(YieldCurve2, package = "OpenGamma")

source("chartSeries3d.alpha.R")

pdf("dexy--yield-chart-2-series-3d-example.pdf")
chartSeries3d0(curves[[1]]$xts)
dev.off()
