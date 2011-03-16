/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Repositories.h)

LOGGING (com.opengamma.pirate.client.Repositories);

CRepositories::CRepositories (CConnector *poConnector) {
	LOGINFO (TEXT ("Creating function, live data and procedure repositories"));
	CFunctionQueryAvailable queryFunctions (poConnector);
	bool bQueryFunctions = queryFunctions.Send ();
	CLiveDataQueryAvailable queryLiveData (poConnector);
	bool bQueryLiveData = queryLiveData.Send ();
	CProcedureQueryAvailable queryProcedures (poConnector);
	bool bQueryProcedures = queryProcedures.Send ();
	m_poFunctions = bQueryFunctions ? CFunctions::GetAvailable (&queryFunctions) : NULL;
	m_poLiveData = bQueryLiveData ? CLiveData::GetAvailable (&queryLiveData) : NULL;
	m_poProcedures = bQueryProcedures ? CProcedures::GetAvailable (&queryProcedures) : NULL;
	if (m_poFunctions) {
		LOGINFO (TEXT ("Loaded ") << m_poFunctions->Size () << TEXT (" function definitions"));
	} else {
		LOGWARN (TEXT ("Couldn't load function definitions"));
	}
	if (m_poLiveData) {
		LOGINFO (TEXT ("Loaded ") << m_poLiveData->Size () << TEXT (" live data definitions"));
	} else {
		LOGWARN (TEXT ("Couldn't load live data definitions"));
	}
	if (m_poProcedures) {
		LOGINFO (TEXT ("Loaded ") << m_poProcedures->Size () << TEXT (" procedure definitions"));
	} else {
		LOGWARN (TEXT ("Couldn't load procedure definitions"));
	}
}

CRepositories::~CRepositories () {
	if (m_poFunctions) CFunctions::Release (m_poFunctions);
	if (m_poLiveData) CLiveData::Release (m_poLiveData);
	if (m_poProcedures) CProcedures::Release (m_poProcedures);
}

CFunctions *CRepositories::GetFunctions () {
	if (!m_poFunctions) return NULL;
	m_poFunctions->Retain ();
	return m_poFunctions;
}

CLiveData *CRepositories::GetLiveData () {
	if (!m_poLiveData) return NULL;
	m_poLiveData->Retain ();
	return m_poLiveData;
}

CProcedures *CRepositories::GetProcedures () {
	if (!m_poProcedures) return NULL;
	m_poProcedures->Retain ();
	return m_poProcedures;
}
