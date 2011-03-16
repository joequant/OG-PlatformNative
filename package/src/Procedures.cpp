/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Procedures.h"
#include "globals.h"
#include "Errors.h"

LOGGING (com.opengamma.pirate.package.Procedures);

SEXP Procedures_count0 () {
    SEXP count = R_NilValue;
    if (g_poProcedures) {
        count = allocVector (INTSXP, 1);
        *INTEGER (count) = g_poProcedures->Size ();
    } else {
		LOGERROR (ERR_INITIALISATION);
    }
    return count;
}

SEXP Procedures_getName1 (SEXP index) {
    SEXP name = R_NilValue;
    if (g_poProcedures) {
        if (isInteger (index)) {
            CProcedureEntry *poEntry = g_poProcedures->Get (*INTEGER (index));
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
