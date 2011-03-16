/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Functions.h)
#include Client(Connector.h)

LOGGING (com.opengamma.pirate.client.Functions);

CFunctionEntry::CFunctionEntry (int nInvocationId, com_opengamma_language_function_Definition *pDefinition) {
	m_nInvocationId = nInvocationId;
	m_pszName = _tcsAsciiDup (pDefinition->fudgeParent._name);
	m_nParameter = pDefinition->fudgeParent.fudgeCountParameter;
	m_ppoParameter = new CParameter*[m_nParameter];
	int n;
	for (n = 0; n < m_nParameter; n++) {
		m_ppoParameter[n] = new CParameter (pDefinition->fudgeParent._parameter[n]);
	}
}

CFunctionEntry::~CFunctionEntry () {
	free (m_pszName);
	int n;
	for (n = 0; n < m_nParameter; n++) {
		delete m_ppoParameter[n];
	}
	delete m_ppoParameter;
}

CParameter *CFunctionEntry::GetParameter (int n) {
	if ((n < 0) || (n >= m_nParameter)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nParameter << TEXT (")"));
		return NULL;
	}
	return m_ppoParameter[n];
}

com_opengamma_language_Data *CFunctionEntry::Invoke (CConnector *poConnector, com_opengamma_language_Data **ppArg) {
	LOGDEBUG ("Invoking " << m_pszName);
	CFunctionInvoke invoke (poConnector);
	invoke.SetInvocationId (m_nInvocationId);
	invoke.SetParameters (m_nParameter, ppArg);
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
	: m_oRefCount (1) {
	LOGINFO (TEXT ("Creating function repository"));
	m_nFunction = pAvailable->fudgeCountFunction;
	m_ppoFunction = new CFunctionEntry*[m_nFunction];
	int n;
	for (n = 0; n < m_nFunction; n++) {
		m_ppoFunction[n] = new CFunctionEntry (pAvailable->_function[n]->_identifier, pAvailable->_function[n]->_definition);
	}
	// poConnector not released in GetAvailable, so don't need to retain
	m_poConnector = poConnector;
}

CFunctions::~CFunctions () {
	LOGINFO (TEXT ("Destroying function repository"));
	assert (m_oRefCount.Get () == 0);
	int n;
	for (n = 0; n < m_nFunction; n++) {
		delete m_ppoFunction[n];
	}
	delete m_ppoFunction;
}

CFunctions *CFunctions::GetAvailable (CFunctionQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for available functions"));
	com_opengamma_language_function_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available function response"));
		return NULL;
	}
	if (pAvailable->fudgeCountFunction > 0) {
		// CFunctions will not retain, so we don't need to release
		return new CFunctions (poQuery->GetConnector (), pAvailable);
	} else {
		LOGWARN (TEXT ("No functions available"));
		return NULL;
	}
}

CFunctionEntry *CFunctions::Get (int n) {
	if ((n < 0) || (n >= m_nFunction)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nFunction << TEXT (")"));
		return NULL;
	}
	return m_ppoFunction[n];
}
