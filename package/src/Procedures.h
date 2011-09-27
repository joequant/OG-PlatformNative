/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_procedures_h
#define __inc_og_rstats_package_procedures_h

#include "Entities.h"
#include Client(Procedures.h)
#include "globals.h"

/// R method helper for procedures
class RProcedures : public REntities {
protected:
	const CEntityEntry *GetEntryImpl (int index) const;
public:
	RProcedures () : REntities (g_poProcedures) { }
	SEXP Invoke (SEXP index, SEXP args, SEXP envir) const;
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC Procedures_count0 () {
		RProcedures oP;
		return oP.Count ();
	}

	SEXP RPROC Procedures_getName1 (SEXP index) {
		RProcedures oP;
		return oP.GetName (index);
	}

	SEXP RPROC Procedures_getParameterFlags1 (SEXP index) {
		RProcedures oP;
		return oP.GetParameterFlags (index);
	}

	SEXP RPROC Procedures_getParameterNames1 (SEXP index) {
		RProcedures oP;
		return oP.GetParameterNames (index);
	}

	SEXP RPROC Procedures_invoke3 (SEXP index, SEXP args, SEXP envir) {
		RProcedures oP;
		return oP.Invoke (index, args, envir);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_procedures_h */
