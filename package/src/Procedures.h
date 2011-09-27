/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_procedures_h
#define __inc_og_rstats_package_procedures_h

class RProcedures {
private:
	RProcedures () { }
	~RProcedures () { }
public:
	static SEXP Count ();
	static SEXP GetName (SEXP index);
	static SEXP Invoke (SEXP index, SEXP args, SEXP envir);
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC Procedures_count0 () {
		return RProcedures::Count ();
	}

	SEXP RPROC Procedures_getName1 (SEXP index) {
		return RProcedures::GetName (index);
	}

	SEXP RPROC Procedures_invoke3 (SEXP index, SEXP args, SEXP envir) {
		return RProcedures::Invoke (index, args, envir);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_procedures_h */
