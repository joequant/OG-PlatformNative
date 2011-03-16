/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(LiveData.h)
#include Client(Connector.h)

LOGGING (com.opengamma.pirate.client.LiveData);

CLiveDataEntry::CLiveDataEntry (int nInvocationId, com_opengamma_language_livedata_Definition *pDefinition) {
	m_nInvocationId = nInvocationId;
	m_pszName = _tcsAsciiDup (pDefinition->fudgeParent._name);
}

CLiveDataEntry::~CLiveDataEntry () {
	free (m_pszName);
}

CLiveData::CLiveData (com_opengamma_language_livedata_Available *pAvailable)
	: m_oRefCount (1) {
	LOGINFO (TEXT ("Creating live data repository"));
	m_nLiveData = pAvailable->fudgeCountLiveData;
	m_ppoLiveData = new CLiveDataEntry*[m_nLiveData];
	int n;
	for (n = 0; n < m_nLiveData; n++) {
		m_ppoLiveData[n] = new CLiveDataEntry (pAvailable->_liveData[n]->_identifier, pAvailable->_liveData[n]->_definition);
	}
}

CLiveData::~CLiveData () {
	LOGINFO (TEXT ("Destroying live data repository"));
	assert (m_oRefCount.Get () == 0);
	int n;
	for (n = 0; n < m_nLiveData; n++) {
		delete m_ppoLiveData[n];
	}
	delete m_ppoLiveData;
}

CLiveData *CLiveData::GetAvailable (CLiveDataQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for live data definitions"));
	com_opengamma_language_livedata_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available live data response"));
		return NULL;
	}
	if (pAvailable->fudgeCountLiveData > 0) {
		return new CLiveData (pAvailable);
	} else {
		LOGWARN (TEXT ("No live data available"));
		return NULL;
	}
}

CLiveDataEntry *CLiveData::Get (int n) {
	if ((n < 0) || (n >= m_nLiveData)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nLiveData << TEXT (")"));
		return NULL;
	}
	return m_ppoLiveData[n];
}
