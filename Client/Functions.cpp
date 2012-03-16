/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Functions.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.Functions);

/// Creates a new function entry wrapper.
///
/// @param[in] nInvocationId invocation ID to include in the Invoke message to the Java stack
/// @param[in] pDefinition function definition
CFunctionEntry::CFunctionEntry (int nInvocationId, const com_opengamma_language_function_Definition *pDefinition)
: CEntityEntry (nInvocationId, &pDefinition->fudgeParent) {
}

/// Destroys a function entry wrapper.
CFunctionEntry::~CFunctionEntry () {
}

/// Invokes a function by sending a message to the Java stack and waiting for the corresponding response.
///
/// @param[in] poConnector connector instance for communication with the Java stack, never NULL
/// @param[in] ppArg array of arguments to send to the Java stack. Never NULL, values must never be NULL and there must be a value for each of the expected arguments (as returned by GetParameterCount)
/// @param[out] ppInfo receives a pointer to any additional information about the result, left unchanged if there is a problem. Can be NULL if the caller does not require the additional information.
/// @return the result, or NULL if there was a problem
com_opengamma_language_Data *CFunctionEntry::Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg, com_opengamma_rstats_msg_DataInfo **ppInfo) const {
	LOGDEBUG ("Invoking " << GetName ());
	CFunctionInvoke invoke (poConnector);
	invoke.SetInvocationId (GetInvocationId ());
	invoke.SetParameters (GetParameterCount (), ppArg);
	if (!invoke.Send ()) {
		LOGWARN (TEXT ("Could not send invocation request"));
		return NULL;
	}
	com_opengamma_rstats_msg_FunctionResult *pResult = invoke.Recv (0x7FFFFFFF);
	if (!pResult) {
		LOGWARN (TEXT ("Did not receive invocation response"));
		return NULL;
	}
	if (pResult->fudgeParent.fudgeCountResult != 1) {
		// Detect the error case, plus nothing works with >1 at the moment
		LOGWARN (TEXT ("Invocation response contained ") << pResult->fudgeParent.fudgeCountResult << TEXT (" result(s)"));
		return NULL;
	}
	// Note: we can steal the pointers from the Data structure as long as we NULL then so they won't be
	// free'd. The caller to Invoke is now responsible for releasing the memory.
	com_opengamma_language_Data *pReturnResult = pResult->fudgeParent._result[0];
	pResult->fudgeParent._result[0] = NULL;
	if (ppInfo) {
		if (pResult->fudgeCountInfo == 1) {
			*ppInfo = pResult->_info[0];
			pResult->_info[0] = NULL;
		} else {
			*ppInfo = NULL;
		}
	}
	return pReturnResult;
}

/// Creates a new collection of function entries.
///
/// @param[in] poConnector connector instance for communication with the Java stack, never NULL
/// @param[in] pAvailable the availability message from the Java stack describing the functions available from this collection
CFunctions::CFunctions (const CConnector *poConnector, const com_opengamma_language_function_Available *pAvailable)
: CEntities (poConnector, pAvailable->fudgeCountFunction) {
	LOGINFO (TEXT ("Creating function repository"));
	int n, count = pAvailable->fudgeCountFunction;
	for (n = 0; n < count; n++) {
		SetImpl (n, new CFunctionEntry (pAvailable->_function[n]->_identifier, pAvailable->_function[n]->_definition));
	}
}

/// Destroys a collection of function entries.
CFunctions::~CFunctions () {
	LOGINFO (TEXT ("Destroying function repository"));
}

/// Creates a function entry collection from a pending query message object.
///
/// @param[in] poQuery pending query object
/// @return a collection instance or NULL if there was a problem
const CFunctions *CFunctions::GetAvailable (CFunctionQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for available functions"));
	const com_opengamma_language_function_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout () * 2);
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available function response"));
		return NULL;
	}
	if (pAvailable->fudgeCountFunction > 0) {
		const CConnector *poConnector = poQuery->GetConnector ();
		CFunctions *poFunctions = new CFunctions (poQuery->GetConnector (), pAvailable);
		CConnector::Release (poConnector);
		return poFunctions;
	} else {
		LOGWARN (TEXT ("No functions available"));
		return NULL;
	}
}
