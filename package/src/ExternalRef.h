/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_externalref_h
#define __inc_og_rstats_package_externalref_h

class RExternalRef {
private:
	RExternalRef () { }
	~RExternalRef () { }
public:
	static SEXP Create (SEXP value, SEXP destructor);
	static SEXP Fetch (SEXP externalref);
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC ExternalRef_create2 (SEXP value, SEXP destructor) {
		return RExternalRef::Create (value, destructor);
	}

	SEXP RPROC ExternalRef_fetch1 (SEXP externalref) {
		return RExternalRef::Fetch (externalref);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_externalref_h */
