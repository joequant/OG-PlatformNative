/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_livedata_h
#define __inc_og_pirate_client_livedata_h

#include <Connector/LiveData.h>
#include <Util/Atomic.h>

class CLiveDataEntry {
private:
	int m_nInvocationId;
	char *m_pszName;
public:
	CLiveDataEntry (int nInvocationId, com_opengamma_language_livedata_Definition *pDefinition);
	~CLiveDataEntry ();
	const char *GetName () { return m_pszName; }
};

class CLiveData {
private:
	CAtomicInt m_oRefCount;
	int m_nLiveData;
	CLiveDataEntry **m_ppoLiveData;
	CLiveData (com_opengamma_language_livedata_Available *pAvailable);
	~CLiveData ();
public:
	static CLiveData *GetAvailable (CLiveDataQueryAvailable *poQuery);
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CLiveData *poLiveData) { if (!poLiveData->m_oRefCount.DecrementAndGet ()) delete poLiveData; }
	int Size () { return m_nLiveData; }
	CLiveDataEntry *Get (int n);
};

#endif /* ifndef __inc_og_pirate_client_livedata_h */
