\name{field.FudgeMsg}
\alias{[,FudgeMsg-method}
\alias{$,FudgeMsg-method}
\alias{field.FudgeMsg}
\title{Get a field from a Fudge message}
\description{
	Gets the field (or fields) from a Fudge message with a given name or ordinal.
}
\arguments{
	\item{x}{A Fudge message object}
	\item{field}{A string naming the field(s) to extract or an integer giving the ordinal to extract}
	\item{i}{Ordinal of the field(s) to extract}
	\item{name}{Name of the field(s) to extract}
}
\usage{
	field.FudgeMsg(x, field)
	\S4method{[}{FudgeMsg}(x, i)
	\S4method{$}{FudgeMsg}(x, name)
}
