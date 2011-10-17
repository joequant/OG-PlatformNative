\name{mapToDataFrame.FudgeMsg}
\alias{mapToDataFrame.FudgeMsg}
\title{Convert a Fudge encoded map to a data frame}
\description{
	Returns a data frame containing the content of a "map" encoded with keys as field ordinal 1
	and values as field ordinal 2. The data frame contains two columns; Key and Value containing
	the key/value pairs. Construction of the data frame will fail if either a key or value cannot
	be put into a suitable vector. If keys or values contain Fudge sub-messages or primitive
	arrays that cannot be converted automatically, conversion functions can be applied (e.g. toString).
}
\arguments{
	\item{x}{A Fudge message object}
	\item{keyFun}{Function to apply to keys, NULL to use each as-is}
	\item{valueFun}{Function to apply to values, NULL to use each as-is}
}
