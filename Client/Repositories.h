/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_repositories_h
#define __inc_og_pirate_client_repositories_h

#include <Connector/Connector.h>
#include Client(Functions.h)
#include Client(LiveData.h)
#include Client(Procedures.h)

class CRepositories {
private:
	CFunctions *m_poFunctions;
	CLiveData *m_poLiveData;
	CProcedures *m_poProcedures;
public:
	CRepositories (CConnector *poConnector);
	~CRepositories ();
	CFunctions *GetFunctions ();
	CLiveData *GetLiveData ();
	CProcedures *GetProcedures ();
};

#endif /* ifndef __inc_og_pirate_client_repositories_h */
