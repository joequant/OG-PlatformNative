\name{as.xts.TimeSeries}%TimeSeries
\alias{as.xts.TimeSeries}
\title{Get a XTS form of a time-series returned from Java}
\description{
	Converts a core R time-series object to a XTS time-series. Time-series returned from the Java
	stack use a start date which is the day offset from 1970-01-01. Using this conversion will
	remove any NA values and set the start date of the XTS time-series using a correct R date object.

	Calling this function will import the XTS package if it is not already loaded. This function
	will fail and return NA if the XTS package is not available.
}
\arguments{
	\item{data}{Time-series object to convert}
}
