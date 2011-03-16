/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Procedures.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.Procedures);

CProcedureEntry::CProcedureEntry (int nInvocationId, com_opengamma_language_procedure_Definition *pDefinition) {
	m_nInvocationId = nInvocationId;
	m_pszName = _tcsAsciiDup (pDefinition->fudgeParent._name);
}

CProcedureEntry::~CProcedureEntry () {
	free (m_pszName);
}

CProcedures::CProcedures (com_opengamma_language_procedure_Available *pAvailable)
	: m_oRefCount (1) {
	LOGINFO (TEXT ("Creating procedure repository"));
	m_nProcedure = pAvailable->fudgeCountProcedure;
	m_ppoProcedure = new CProcedureEntry*[m_nProcedure];
	int n;
	for (n = 0; n < m_nProcedure; n++) {
		m_ppoProcedure[n] = new CProcedureEntry (pAvailable->_procedure[n]->_identifier, pAvailable->_procedure[n]->_definition);
	}
}

CProcedures::~CProcedures () {
	LOGINFO (TEXT ("Destroying procedure repository"));
	assert (m_oRefCount.Get () == 0);
	int n;
	for (n = 0; n < m_nProcedure; n++) {
		delete m_ppoProcedure[n];
	}
	delete m_ppoProcedure;
}

CProcedures *CProcedures::GetAvailable (CProcedureQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for procedure definitions"));
	com_opengamma_language_procedure_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available procedures response"));
		return NULL;
	}
	if (pAvailable->fudgeCountProcedure > 0) {
		return new CProcedures (pAvailable);
	} else {
		LOGWARN (TEXT ("No procedures available"));
		return NULL;
	}
}

CProcedureEntry *CProcedures::Get (int n) {
	if ((n < 0) || (n >= m_nProcedure)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nProcedure << TEXT (")"));
		return NULL;
	}
	return m_ppoProcedure[n];
}
