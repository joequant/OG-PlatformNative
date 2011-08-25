/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(LiveData.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.LiveData);

CLiveDataEntry::CLiveDataEntry (int nInvocationId, const com_opengamma_language_livedata_Definition *pDefinition)
: CEntityEntry (nInvocationId, &pDefinition->fudgeParent) {
}

CLiveDataEntry::~CLiveDataEntry () {
}

CLiveData::CLiveData (const CConnector *poConnector, const com_opengamma_language_livedata_Available *pAvailable)
: CEntities (poConnector, pAvailable->fudgeCountLiveData) {
	LOGINFO (TEXT ("Creating live data repository"));
	int n, count = pAvailable->fudgeCountLiveData;
	for (n = 0; n < count; n++) {
		SetImpl (n, new CLiveDataEntry (pAvailable->_liveData[n]->_identifier, pAvailable->_liveData[n]->_definition));
	}
}

CLiveData::~CLiveData () {
	LOGINFO (TEXT ("Destroying live data repository"));
}

const CLiveData *CLiveData::GetAvailable (CLiveDataQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for live data definitions"));
	const com_opengamma_language_livedata_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available live data response"));
		return NULL;
	}
	if (pAvailable->fudgeCountLiveData > 0) {
		const CConnector *poConnector = poQuery->GetConnector ();
		CLiveData *poLiveData = new CLiveData (poConnector, pAvailable);
		CConnector::Release (poConnector);
		return poLiveData;
	} else {
		LOGWARN (TEXT ("No live data available"));
		return NULL;
	}
}
