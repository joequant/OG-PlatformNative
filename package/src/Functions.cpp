/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Functions.h"
#include "globals.h"
#include "Errors.h"
#include "Parameters.h"
#include "DataInfo.h"

LOGGING (com.opengamma.rstats.package.Functions);

SEXP RFunctions::Count () {
	SEXP count = R_NilValue;
	if (g_poFunctions) {
		count = allocVector (INTSXP, 1);
		*INTEGER (count) = g_poFunctions->Size ();
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return count;
}

SEXP RFunctions::GetName (SEXP index) {
	SEXP name = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			const CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
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

SEXP RFunctions::GetParameterFlags (SEXP index) {
	SEXP flags = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			const CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
			if (poEntry) {
				flags = allocVector (INTSXP, poEntry->GetParameterCount ());
				int n;
				for (n = 0; n < poEntry->GetParameterCount (); n++) {
					INTEGER (flags)[n] = poEntry->GetParameter (n)->GetFlags ();
				}
			} else {
				LOGERROR (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return flags;
}

SEXP RFunctions::GetParameterNames (SEXP index) {
	SEXP names = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			const CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
			if (poEntry) {
				PROTECT (names = allocVector (STRSXP, poEntry->GetParameterCount ()));
				int n;
				for (n = 0; n < poEntry->GetParameterCount (); n++) {
					SEXP name = mkChar (poEntry->GetParameter (n)->GetName ());
					PROTECT (name);
					SET_STRING_ELT (names, n, name);
				}
				UNPROTECT (1 + poEntry->GetParameterCount ());
			} else {
				LOGERROR (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return names;
}

SEXP RFunctions::Invoke (SEXP index, SEXP args, SEXP envir) {
	CRCallback oR (envir);
	SEXP result = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			const CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
			if (poEntry) {
				CParameters *poParameters = CParameters::Decode (&oR, args);
				if (poParameters) {
					if (poParameters->Count () == poEntry->GetParameterCount ()) {
						LOGINFO ("Invoke " << poEntry->GetName ());
						com_opengamma_rstats_msg_DataInfo *pInfo = NULL;
						com_opengamma_language_Data *pResult = g_poFunctions->Invoke (poEntry, poParameters->GetData (), &pInfo);
						if (pResult) {
							result = CData::ToSEXP (pResult);
							PROTECT (result);
							if (pInfo) {
								result = CDataInfo::Apply (&oR, result, pInfo);
								CDataInfo::Release (pInfo);
							}
							UNPROTECT (1);
							CData::Release (pResult);
						} else {
							LOGERROR (ERR_INVOCATION);
						}
					} else {
						LOGERROR (ERR_PARAMETER_VALUE);
					}
					delete poParameters;
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
				}
			} else {
				LOGERROR (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return result;
}
