/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Procedures.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.Procedures);

CProcedureEntry::CProcedureEntry (int nInvocationId, const com_opengamma_language_procedure_Definition *pDefinition)
: CEntityEntry (nInvocationId, &pDefinition->fudgeParent) {
}

CProcedureEntry::~CProcedureEntry () {
}

/// Invokes a procedure by sending a message to the Java stack and waiting for the corresponding response.
///
/// @param[in] poConntector connector instance for communication with the Java stack, never NULL
/// @param[in] ppArg array of arguments to send to the Java stack. Never NULL, values must never be NULL and there must be a value for each of the expected arguments (as returned by GetParameterCount)
/// @param[out] ppResult receives a pointer to the result, left unchanged if there is a problem, NULL if there were no results. Can be NULL if the caller does not want the response
/// @param[out] ppInfo receives a pointer to any additional information about the result, left unchanged if there is a problem. Callers should set to NULL before calling this method. Can be NULL if the caller does not require the additional information.
/// @return TRUE if a result was returned, FALSE if there was a problem
bool CProcedureEntry::Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg, com_opengamma_language_Data **ppResult, com_opengamma_rstats_msg_DataInfo **ppInfo) const {
	LOGDEBUG ("Invoking " << GetName ());
	CProcedureInvoke invoke (poConnector);
	invoke.SetInvocationId (GetInvocationId ());
	invoke.SetParameters (GetParameterCount (), ppArg);
	if (!invoke.Send ()) {
		LOGWARN (TEXT ("Could not send invocation request"));
		return false;
	}
	com_opengamma_rstats_msg_ProcedureResult *pResult = invoke.Recv (0x7FFFFFFF);
	if (!pResult) {
		LOGWARN (TEXT ("Did not receive invocation response"));
		return false;
	}
	if (pResult->fudgeParent.fudgeCountResult == 0) {
		if (ppResult) {
			*ppResult = NULL;
		}
		if (ppInfo) {
			*ppInfo = NULL;
		}
		return true;
	} else if (pResult->fudgeParent.fudgeCountResult == 1) {
		if (ppResult) {
			*ppResult = pResult->fudgeParent._result[0];
			pResult->fudgeParent._result[0] = NULL;
		}
		if (ppInfo) {
			if (pResult->fudgeCountInfo == 1) {
				*ppInfo = pResult->_info[0];
				pResult->_info[0] = NULL;
			} else {
				*ppInfo = NULL;
			}
		}
		return true;
	} else {
		LOGWARN (TEXT ("Invocation response contained ") << pResult->fudgeParent.fudgeCountResult << TEXT (" results"));
		return false;
	}
}

CProcedures::CProcedures (const CConnector *poConnector, const com_opengamma_language_procedure_Available *pAvailable)
: CEntities (poConnector, pAvailable->fudgeCountProcedure) {
	LOGINFO (TEXT ("Creating procedure repository"));
	int n, count = pAvailable->fudgeCountProcedure;
	for (n = 0; n < count; n++) {
		SetImpl (n, new CProcedureEntry (pAvailable->_procedure[n]->_identifier, pAvailable->_procedure[n]->_definition));
	}
}

CProcedures::~CProcedures () {
	LOGINFO (TEXT ("Destroying procedure repository"));
}

const CProcedures *CProcedures::GetAvailable (CProcedureQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for procedure definitions"));
	const com_opengamma_language_procedure_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available procedures response"));
		return NULL;
	}
	if (pAvailable->fudgeCountProcedure > 0) {
		const CConnector *poConnector = poQuery->GetConnector ();
		CProcedures *poProcedures = new CProcedures (poConnector, pAvailable);
		CConnector::Release (poConnector);
		return poProcedures;
	} else {
		LOGWARN (TEXT ("No procedures available"));
		return NULL;
	}
}
