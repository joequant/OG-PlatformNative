\name{as.character.ViewClient}
\alias{as.character,ViewClient-method}
\title{Get the unique identifier of a view client}
\description{
	Returns the unique identifier as a string describing the view client instance. Note that the
	unique identifier is not portable and only valid for the lifetime of the instance. For a
	portable string representation, the view descriptor used to create the view client instance
	should be used instead.
}
\arguments{
	\item{x}{A view client instance}
	\item{...}{Ignored}
}
\usage{
	\S4method{as.character}{ViewClient}(x, ...)
}
