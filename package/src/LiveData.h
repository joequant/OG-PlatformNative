/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_livedata_h
#define __inc_og_rstats_package_livedata_h

#include "Entities.h"
#include Client(LiveData.h)
#include "globals.h"

// R method helper for live data.
class RLiveData : public REntities {
protected:
	const CEntityEntry *GetEntryImpl (int index) const;
public:
	RLiveData () : REntities (g_poLiveData) { }
	SEXP Invoke (SEXP index, SEXP args, SEXP envir) const;
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC LiveData_count0 () {
		RLiveData oL;
		return oL.Count ();
	}

	SEXP RPROC LiveData_getName1 (SEXP index) {
		RLiveData oL;
		return oL.GetName (index);
	}

	SEXP RPROC LiveData_getParameterFlags1 (SEXP index) {
		RLiveData oL;
		return oL.GetParameterFlags (index);
	}

	SEXP RPROC LiveData_getParameterNames1 (SEXP index) {
		RLiveData oL;
		return oL.GetParameterNames (index);
	}

	SEXP RPROC LiveData_invoke3 (SEXP index, SEXP args, SEXP envir) {
		RLiveData oL;
		return oL.Invoke (index, args, envir);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_livedata_h */
