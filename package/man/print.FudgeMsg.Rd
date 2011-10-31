\name{print.FudgeMsg}
\alias{print.FudgeMsg}
\title{Pretty prints a Fudge messsage structure}
\description{
	Prints out a Fudge message, using the \code{toString} of all member fields values except
	for embedded Fudge messages which are recursively printed with an indent.
}
\arguments{
	\item{x}{A Fudge message object}
	\item{...}{Ignored}
}
\usage{
	\method{print}{FudgeMsg}(x, ...)
}
