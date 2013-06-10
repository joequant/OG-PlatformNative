/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_livedata_h
#define __inc_og_rstats_client_livedata_h

#include "com_opengamma_rstats_msg_LiveDataResult.h"
#define CLASS_com_opengamma_language_livedata_Result com_opengamma_rstats_msg_LiveDataResult
#include <Connector/LiveData.h>
#include Client(Entities.h)

class CLiveDataEntry : public CEntityEntry {
public:
	CLiveDataEntry (int nComponentId, const com_opengamma_language_livedata_Definition *pDefinition);
	~CLiveDataEntry ();
	com_opengamma_language_Data *Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg, com_opengamma_rstats_msg_DataInfo **ppInfo) const;
};

class CLiveData : public CEntities {
private:
	CLiveData (const CConnector *poConnector, const com_opengamma_language_livedata_Available *pAvailable);
	~CLiveData ();
public:
	static const CLiveData *GetAvailable (CLiveDataQueryAvailable *poQuery);
	const CLiveDataEntry *Get (int n) const { return (const CLiveDataEntry*)GetImpl (n); }
	com_opengamma_language_Data *Invoke (const CLiveDataEntry *poEntry, const com_opengamma_language_Data * const *ppArg, com_opengamma_rstats_msg_DataInfo **ppInfo) const { return poEntry->Invoke (GetConnector (), ppArg, ppInfo); }
};

#endif /* ifndef __inc_og_rstats_client_livedata_h */
