\name{Init}
\alias{Init}
\alias{Init}
\title{Initialise the OpenGamma library}
\description{
	Connects to the OpenGamma installation and imports the OG namespace containing
	the wrapper functions. The OG package is constructed dynamically using the
	definitions published by the Java components.

	Note that the OG package containing the Java based definitions is installed as
	a source package. A minimal R installation may not be able to install this and
	you may see errors from the \code{install.packages} function. If there is not
	a writable directory to install the package into you may see errors and the
	namespace will not be loaded (or be prompted to create a directory if you are
	running R interactively).
}
\arguments{
	\item{cached.stub}{If set to TRUE will use an existing OG namespace. If set to FALSE will always rebuild it. It is necessary to rebuild it if the Java definitions have changed, but carries a performance penalty when running many short scripts. If omitted, the opengamma.cache.stub option is used - if this is not set the default behavior is to use automatic cache management.}
}
