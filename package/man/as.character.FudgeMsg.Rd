\name{as.character.FudgeMsg}
\alias{as.character,FudgeMsg-method}
\title{Get a textual representation of a Fudge messsage}
\description{
	Returns a textual description of a Fudge message. All field values are in turn converted to text
	values using their \code{toString} methods.
}
\arguments{
	\item{x}{A Fudge message object}
	\item{...}{Ignored}
}
\usage{
	\S4method{as.character}{FudgeMsg}(x, ...)
}
