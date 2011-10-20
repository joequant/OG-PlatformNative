\name{toObject.FudgeMsg}
\alias{toObject.FudgeMsg}
\title{Convert a Fudge message to an R object}
\description{
	Produces an R object based on the Fudge message. If the message contains a class name in field
	with ordinal 0, a method called fromFudgeMsg.class is used, where class is the value returned
	by displayName.FudgeMsg. If no such method exists, toString is used to produce a textual
	description of the Fudge message.
}
\arguments{
	\item{x}{A Fudge message object}
}
