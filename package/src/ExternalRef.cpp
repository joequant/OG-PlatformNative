/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "ExternalRef.h"
#define FUDGE_NO_NAMESPACE
#include "DataValue.h"
#undef FUDGE_NO_NAMESPACE
#include "globals.h"
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.ExternalRef);

static void RPROC ExternalRef_finalizer (SEXP externalref) {
	char *pszDestructor = (char*)R_ExternalPtrAddr (externalref);
	if (pszDestructor) {
		SEXP value = R_ExternalPtrProtected (externalref);
		PROTECT (value);
		Data data;
#ifdef _WIN32
		ZeroMemory (&data, sizeof (data));
#else /* ifdef _WIN32 */
		memset (&data, 0, sizeof (data));
#endif /* ifdef _WIN32 */
		data._single = CValue::FromSEXP (value);
		UNPROTECT (1);
		R_ClearExternalPtr (externalref);
		if (data._single) {
			LOGDEBUG ("Calling destructor '" << pszDestructor << "'");
			if (g_poProcedures) {
				const CProcedureEntry *poEntry = g_poProcedures->Get (pszDestructor);
				if (poEntry) {
					const Data *apArgs[1] = { &data };
					LOGINFO ("Invoke " << poEntry->GetName ());
					g_poProcedures->Invoke (poEntry, apArgs, NULL, NULL);
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
				}
			} else {
				LOGERROR (ERR_INITIALISATION);
			}
			CValue::Release (data._single);
		} else {
			LOGERROR (ERR_PARAMETER_VALUE);
		}
		free (pszDestructor);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
}

SEXP RExternalRef::Create (SEXP value, SEXP destructor) {
	if (TYPEOF (destructor) != STRSXP) {
		LOGERROR (ERR_PARAMETER_TYPE);
		return R_NilValue;
	}
	char *pszDestructor = strdup (CHAR (STRING_ELT (destructor, 0)));
	if (!pszDestructor) {
		LOGFATAL (ERR_MEMORY);
		return R_NilValue;
	}
	SEXP externalref = R_MakeExternalPtr (pszDestructor, R_NilValue, value);
	PROTECT (externalref);
	R_RegisterCFinalizerEx (externalref, ExternalRef_finalizer, FALSE);
	UNPROTECT (1);
	return externalref;
}

SEXP RExternalRef::Fetch (SEXP externalref) {
	return R_ExternalPtrProtected (externalref);
}
