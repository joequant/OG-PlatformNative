/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Procedures.h"
#include "RCallback.h"
#include "globals.h"
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.Procedures);

SEXP RProcedures::Count () {
	SEXP count = R_NilValue;
	if (g_poProcedures) {
		count = allocVector (INTSXP, 1);
		*INTEGER (count) = g_poProcedures->Size ();
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return count;
}

SEXP RProcedures::GetName (SEXP index) {
	SEXP name = R_NilValue;
	if (g_poProcedures) {
		if (isInteger (index)) {
			const CProcedureEntry *poEntry = g_poProcedures->Get (*INTEGER (index));
			if (poEntry) {
				name = mkString (poEntry->GetName ());
			} else {
				LOGERROR (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return name;
}

SEXP RProcedures::Invoke (SEXP index, SEXP args, SEXP envir) {
	CRCallback oR (envir);
	if (g_poProcedures) {
		LOGWARN (TEXT ("Invoke procedure"));
		LOGFATAL (ERR_INTERNAL);
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return R_NilValue;
}
