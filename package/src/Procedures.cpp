/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Procedures.h"
#include "Parameters.h"
#include "globals.h"
#include "Errors.h"
#include "DataInfo.h"

LOGGING (com.opengamma.rstats.package.Procedures);

const CEntityEntry *RProcedures::GetEntryImpl (int index) const {
	return g_poProcedures->Get (index);
}

SEXP RProcedures::Invoke (SEXP index, SEXP args, SEXP envir) const {
	const CProcedureEntry *poEntry = (CProcedureEntry*)GetEntry (index);
	SEXP result = R_NilValue;
	if (poEntry) {
		CRCallback oR (envir);
		CParameters *poParameters = CParameters::Decode (&oR, args);
		if (poParameters) {
			if (poParameters->Count () == poEntry->GetParameterCount ()) {
				LOGINFO ("Invoke " << poEntry->GetName ());
				com_opengamma_rstats_msg_DataInfo *pInfo;
				com_opengamma_language_Data *pResult;
				if (g_poProcedures->Invoke (poEntry, poParameters->GetData (), &pResult, &pInfo)) {
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
			}
			delete poParameters;
		} else {
			LOGERROR (ERR_PARAMETER_VALUE);
		}
	}
	return result;
}
