\name{firstValue.ViewComputationResultModel}
\alias{firstValue.ViewComputationResultModel}
\title{Get the first non-NA value from a data frame row}
\description{
	Returns the first non-NA value from the row. Typically the columns requested are a subset
	that can satisfy a given value requirement. This will then return the first usable value
	found. Values appear in multiple columns because a column is created in the data frame for
	each value name/properties pair. Differences in, for example, the function identifier may
	mean that there is not a single column containing all of the desired values requested in a
	view definition.
}
\arguments{
	\item{row}{Data frame row}
	\item{columns}{Vector of column names to look in}
}
