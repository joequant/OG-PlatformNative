/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_livedata_h
#define __inc_og_pirate_client_livedata_h

#include <Connector/LiveData.h>
#include Client(Entities.h)

class CLiveDataEntry : public CEntityEntry {
public:
	CLiveDataEntry (int nInvocationId, com_opengamma_language_livedata_Definition *pDefinition);
	~CLiveDataEntry ();
};

class CLiveData : public CEntities {
private:
	CLiveData (CConnector *poConnector, com_opengamma_language_livedata_Available *pAvailable);
	~CLiveData ();
public:
	static CLiveData *GetAvailable (CLiveDataQueryAvailable *poQuery);
	CLiveDataEntry *Get (int n) { return (CLiveDataEntry*)GetImpl (n); }
};

#endif /* ifndef __inc_og_pirate_client_livedata_h */
