/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_functions_h
#define __inc_og_rstats_package_functions_h

class RFunctions {
private:
	RFunctions () { }
	~RFunctions () { }
public:
	static SEXP Count ();
	static SEXP GetName (SEXP index);
	static SEXP GetParameterFlags (SEXP index);
	static SEXP GetParameterNames (SEXP index);
	static SEXP Invoke (SEXP index, SEXP args, SEXP envir);
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC Functions_count0 () {
		return RFunctions::Count ();
	}

	SEXP RPROC Functions_getName1 (SEXP index) {
		return RFunctions::GetName (index);
	}

	SEXP RPROC Functions_getParameterFlags1 (SEXP index) {
		return RFunctions::GetParameterFlags (index);
	}

	SEXP RPROC Functions_getParameterNames1 (SEXP index) {
		return RFunctions::GetParameterNames (index);
	}

	SEXP RPROC Functions_invoke3 (SEXP index, SEXP args, SEXP envir) {
		return RFunctions::Invoke (index, args, envir);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_functions_h */
