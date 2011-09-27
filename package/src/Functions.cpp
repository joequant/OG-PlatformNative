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

const CEntityEntry *RFunctions::GetEntryImpl (int index) const {
	return g_poFunctions->Get (index);
}

SEXP RFunctions::Invoke (SEXP index, SEXP args, SEXP envir) const {
	const CFunctionEntry *poEntry = (const CFunctionEntry*)GetEntry (index);
	SEXP result = R_NilValue;
	if (poEntry) {
		CRCallback oR (envir);
		CParameters *poParameters = CParameters::Decode (&oR, args);
		if (poParameters) {
			if (poParameters->Count () == poEntry->GetParameterCount ()) {
				LOGINFO ("Invoke " << poEntry->GetName ());
				com_opengamma_rstats_msg_DataInfo *pInfo;
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
	}
	return result;
}
