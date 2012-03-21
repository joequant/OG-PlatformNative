\name{displayName.FudgeMsg}%Misc
\alias{displayName.FudgeMsg}
\title{Get a displayable class name of an encoded object}
\description{
	Returns a string that can be displayed to describe the type of the encoded object. The name is
	constructed from the first class name found. If the class name is delimited by '.' characters, the
	last fragment is used. If there are no class names, the string "FudgeMsg" is returned
}
\arguments{
	\item{msg}{A Fudge message object}
}
