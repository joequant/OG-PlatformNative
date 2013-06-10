/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_functions_h
#define __inc_og_rstats_package_functions_h

#include "Entities.h"
#include Client(Functions.h)
#include "globals.h"

/// R method helper for functions.
class RFunctions : public REntities {
protected:
	const CEntityEntry *GetEntryImpl (int index) const;
public:
	RFunctions () : REntities (g_poFunctions) { }
	SEXP Invoke (SEXP index, SEXP args, SEXP envir) const;
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC Functions_count0 () {
		RFunctions oF;
		return oF.Count ();
	}

	SEXP RPROC Functions_getCategory1 (SEXP index) {
		RFunctions oF;
		return oF.GetCategory (index);
	}

	SEXP RPROC Functions_getDescription1 (SEXP index) {
		RFunctions oF;
		return oF.GetDescription (index);
	}

	SEXP RPROC Functions_getName1 (SEXP index) {
		RFunctions oF;
		return oF.GetName (index);
	}

	SEXP RPROC Functions_getParameterFlags1 (SEXP index) {
		RFunctions oF;
		return oF.GetParameterFlags (index);
	}

	SEXP RPROC Functions_getParameterNames1 (SEXP index) {
		RFunctions oF;
		return oF.GetParameterNames (index);
	}

	SEXP RPROC Functions_getParameterDescriptions1 (SEXP index) {
		RFunctions oF;
		return oF.GetParameterDescriptions (index);
	}

	SEXP RPROC Functions_invoke3 (SEXP index, SEXP args, SEXP envir) {
		RFunctions oF;
		return oF.Invoke (index, args, envir);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_functions_h */
