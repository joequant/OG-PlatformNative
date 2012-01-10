/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(LiveData.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.LiveData);

/// Creates a new live data entry wrapper.
///
/// @param[in] nComponentId live data component ID to identify the element in the Java stack
/// @param[in] pDefinition live data definition
CLiveDataEntry::CLiveDataEntry (int nComponentId, const com_opengamma_language_livedata_Definition *pDefinition)
: CEntityEntry (nComponentId, &pDefinition->fudgeParent) {
}

/// Destroys a live data entry wrapper.
CLiveDataEntry::~CLiveDataEntry () {
}

/// Connects to a live data component. The Java stack is assumed to block until a result is available and
/// then cancel the connection.
///
/// @param[in] poConnector connector instance for communication with the Java stack, never NULL
/// @param[in] ppArg array of arguments to send to the Java stack. Never NULL, values must never by NULL and there must be a value for each of the expected arguments (as returned by GetParameterCount)
/// @param[out] ppInfo receives a pointer to any additional information about the result, left unchanged if there is a problem. Can be NULL if the caller does not require the additional information
/// @return the first result or NULL if there was a problem
com_opengamma_language_Data *CLiveDataEntry::Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg, com_opengamma_rstats_msg_DataInfo **ppInfo) const {
	LOGDEBUG ("Connecting to " << GetName ());
	CLiveDataConnect connect (poConnector);
	connect.SetComponentId (GetInvocationId ());
	connect.SetParameters (GetParameterCount (), ppArg);
	if (!connect.Send ()) {
		LOGWARN (TEXT ("Could not send connect request"));
		return NULL;
	}
	com_opengamma_rstats_msg_LiveDataResult *pResult = connect.Recv (0x7FFFFFFF);
	if (!pResult) {
		LOGWARN (TEXT ("Did not receive connection response"));
		return NULL;
	}
	if (!pResult->fudgeParent._connection) {
		LOGWARN ("Could not connect to " << GetName ());
		return NULL;
	}
	// Note: we can steal the pointers from the Data structure as long as we NULL them so they won't be
	// free'd. The caller to Invoke is now responsible for releasing the memory.
	com_opengamma_language_Data *pReturnResult = pResult->fudgeParent._result;
	if (pReturnResult) {
		pResult->fudgeParent._result = NULL;
	}
	if (ppInfo) {
		*ppInfo = pResult->_info;
		if (*ppInfo) {
			pResult->_info = NULL;
		}
	}
	return pReturnResult;
}

/// Creates a new collection of live data entries
///
/// @param[in] poConnector connector instance for communication with the Java stack, never NULL
/// @param[in] pAvailable the availability message from the Java stack describing the live data components available from this collection
CLiveData::CLiveData (const CConnector *poConnector, const com_opengamma_language_livedata_Available *pAvailable)
: CEntities (poConnector, pAvailable->fudgeCountLiveData) {
	LOGINFO (TEXT ("Creating live data repository"));
	int n, count = pAvailable->fudgeCountLiveData;
	for (n = 0; n < count; n++) {
		SetImpl (n, new CLiveDataEntry (pAvailable->_liveData[n]->_identifier, pAvailable->_liveData[n]->_definition));
	}
}

/// Destroys a collection of live data entries
CLiveData::~CLiveData () {
	LOGINFO (TEXT ("Destroying live data repository"));
}

/// Creates a live data entry collection from a pending query message object.
///
/// @param[in] poQuery pending query object
/// @return a collection instance or NULL if there was a problem
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
