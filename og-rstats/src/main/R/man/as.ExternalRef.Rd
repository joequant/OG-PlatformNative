\name{as.ExternalRef}%Misc
\alias{as.ExternalRef}
\title{Get a tracked reference to a Java stack entity}
\description{
	Returns an \code{externalptr} that will call a procedure in the Java stack when it falls out of
	scope. The original value can be retrieved from the reference using the \code{from.ExternalRef}
	function.
}
\arguments{
	\item{value}{Value to form the reference from}
	\item{destructorName}{Name of the Java destructor procedure to call when finalized}
}
