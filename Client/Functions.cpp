/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Functions.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.Functions);

CFunctionEntry::CFunctionEntry (int nInvocationId, com_opengamma_language_function_Definition *pDefinition)
: CEntityEntry (nInvocationId, &pDefinition->fudgeParent) {
}

CFunctionEntry::~CFunctionEntry () {
}

com_opengamma_language_Data *CFunctionEntry::Invoke (CConnector *poConnector, com_opengamma_language_Data **ppArg) {
	LOGDEBUG ("Invoking " << GetName ());
	CFunctionInvoke invoke (poConnector);
	invoke.SetInvocationId (GetInvocationId ());
	invoke.SetParameters (GetParameterCount (), ppArg);
	if (!invoke.Send ()) {
		LOGWARN (TEXT ("Could not send invocation request"));
		return NULL;
	}
	com_opengamma_language_function_Result *pResult = invoke.Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pResult) {
		LOGWARN (TEXT ("Did not receive invocation response"));
		return NULL;
	}
	if (pResult->fudgeCountResult != 1) {
		// Detect the error case, plus nothing works with >1 at the moment
		LOGWARN (TEXT ("Invocation response contained ") << pResult->fudgeCountResult << TEXT (" result(s)"));
		return NULL;
	}
	// Note: we can steal the pointer from the Data structure as long as we NULL it so it won't be
	// free'd. The caller to Invoke is now responsible for releasing the memory
	com_opengamma_language_Data *pReturnResult = pResult->_result[0];
	pResult->_result[0] = NULL;
	return pReturnResult;
}

CFunctions::CFunctions (CConnector *poConnector, com_opengamma_language_function_Available *pAvailable)
: CEntities (poConnector, pAvailable->fudgeCountFunction) {
	LOGINFO (TEXT ("Creating function repository"));
	int n, count = pAvailable->fudgeCountFunction;
	for (n = 0; n < count; n++) {
		SetImpl (n, new CFunctionEntry (pAvailable->_function[n]->_identifier, pAvailable->_function[n]->_definition));
	}
}

CFunctions::~CFunctions () {
	LOGINFO (TEXT ("Destroying function repository"));
}

CFunctions *CFunctions::GetAvailable (CFunctionQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for available functions"));
	com_opengamma_language_function_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available function response"));
		return NULL;
	}
	if (pAvailable->fudgeCountFunction > 0) {
		CConnector *poConnector = poQuery->GetConnector ();
		CFunctions *poFunctions = new CFunctions (poQuery->GetConnector (), pAvailable);
		CConnector::Release (poConnector);
		return poFunctions;
	} else {
		LOGWARN (TEXT ("No functions available"));
		return NULL;
	}
}
