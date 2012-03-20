\name{unversioned.Identifier}%Identifier
\alias{unversioned.Identifier}
\title{Removes the version suffix from a unique identifier}
\description{
	By removing the version suffix, a unique identifier becomes an object identifier
	referencing the latest version of the object. For example a view using a versioned
	identifier for a snapshot will reference that exact version whereas one using
	the unversioned object identifier can be set to "tick" whenever that snapshot
	changes as it always references the latest version of the snapshot.
}
\arguments{
	\item{identifier}{Unique identifier}
}
