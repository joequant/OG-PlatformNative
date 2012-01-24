\name{Init}
\alias{Init}
\alias{Init}
\title{Initialise the OpenGamma library}
\description{
	Connects to the OpenGamma installation and imports the OG namespace containing
	the wrapper functions. The OG package is constructed dynamically using the
	definitions published by the Java components.
}
\arguments{
	\item{cached.stub}{If set to TRUE will use an existing OG namespace. If set to FALSE will always rebuild it. It is necessary to rebuild it if the Java definitions have changed, but carries a performance penalty when running many short scripts. If omitted, the opengamma.cache.stub option is used - if this is not set the default behavior is to rebuild the namespace.}
}
