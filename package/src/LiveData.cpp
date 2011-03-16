/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "LiveData.h"
#include "globals.h"
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.LiveData);

SEXP LiveData_count0 () {
    SEXP count = R_NilValue;
    if (g_poLiveData) {
        count = allocVector (INTSXP, 1);
        *INTEGER (count) = g_poLiveData->Size ();
    } else {
		LOGERROR (ERR_INITIALISATION);
    }
    return count;
}

SEXP LiveData_getName1 (SEXP index) {
    SEXP name = R_NilValue;
    if (g_poLiveData) {
        if (isInteger (index)) {
            CLiveDataEntry *poEntry = g_poLiveData->Get (*INTEGER (index));
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
