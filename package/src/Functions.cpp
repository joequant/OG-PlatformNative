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

LOGGING (com.opengamma.rstats.package.Functions);

SEXP Functions_count0 () {
	SEXP count = R_NilValue;
	if (g_poFunctions) {
		count = allocVector (INTSXP, 1);
		*INTEGER (count) = g_poFunctions->Size ();
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return count;
}

SEXP Functions_getName1 (SEXP index) {
	SEXP name = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
			if (poEntry) {
				name = mkString (poEntry->GetName ());
			} else {
				LOGWARN (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return name;
}

SEXP Functions_getParameterFlags1 (SEXP index) {
    SEXP flags = R_NilValue;
    if (g_poFunctions) {
        if (isInteger (index)) {
            CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
            if (poEntry) {
                flags = allocVector (INTSXP, poEntry->GetParameterCount ());
                int n;
                for (n = 0; n < poEntry->GetParameterCount (); n++) {
                    INTEGER (flags)[n] = poEntry->GetParameter (n)->GetFlags ();
                }
            } else {
                LOGWARN (ERR_PARAMETER_VALUE);
            }
        } else {
            LOGERROR (ERR_PARAMETER_TYPE);
        }
    } else {
        LOGERROR (ERR_INITIALISATION);
    }
    return flags;
}

SEXP Functions_getParameterNames1 (SEXP index) {
	SEXP names = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
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
				LOGWARN (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return names;
}

SEXP Functions_invoke2 (SEXP index, SEXP args) {
	SEXP result = R_NilValue;
	if (g_poFunctions) {
		if (isInteger (index)) {
			CFunctionEntry *poEntry = g_poFunctions->Get (*INTEGER (index));
			if (poEntry) {
				CParameters *poParameters = CParameters::Decode (args);
				if (poParameters) {
					if (poParameters->Count () == poEntry->GetParameterCount ()) {
						LOGINFO ("Invoke " << poEntry->GetName ());
						com_opengamma_language_Data *pResult = g_poFunctions->Invoke (poEntry, poParameters->GetData ());
						if (pResult) {
							result = CData::ToSEXP (pResult);
							CData::Release (pResult);
						} else {
							LOGERROR (ERR_INVOCATION);
						}
					} else {
						LOGWARN (ERR_PARAMETER_VALUE);
					}
					delete poParameters;
				} else {
					LOGWARN (ERR_PARAMETER_VALUE);
				}
			} else {
				LOGWARN (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
	}
	return result;
}
