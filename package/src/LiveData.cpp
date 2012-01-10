/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "LiveData.h"
#include "globals.h"
#include "Errors.h"
#include "Parameters.h"
#include "DataInfo.h"

LOGGING (com.opengamma.rstats.package.LiveData);

// Note that the live data implementation is incomplete. It connects to a component, blocks
// for the first value and returns that.

const CEntityEntry *RLiveData::GetEntryImpl (int index) const {
	return g_poLiveData->Get (index);
}

SEXP RLiveData::Invoke (SEXP index, SEXP args, SEXP envir) const {
	const CLiveDataEntry *poEntry = (const CLiveDataEntry*)GetEntry (index);
	SEXP result = R_NilValue;
	if (poEntry) {
		CRCallback oR (envir);
		CParameters *poParameters = CParameters::Decode (&oR, args);
		if (poParameters) {
			if (poParameters->Count () == poEntry->GetParameterCount ()) {
				LOGINFO ("Connect " << poEntry->GetName ());
				com_opengamma_rstats_msg_DataInfo *pInfo;
				com_opengamma_language_Data *pResult = g_poLiveData->Invoke (poEntry, poParameters->GetData (), &pInfo);
				if (pResult) {
					result = ProcessResult (&oR, pResult, pInfo);
					if (pInfo) {
						CDataInfo::Release (pInfo);
					}
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
