/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_repositories_h
#define __inc_og_rstats_client_repositories_h

#include <connector/cpp/Connector.h>
#include Client(Functions.h)
#include Client(LiveData.h)
#include Client(Procedures.h)

class CRepositories {
private:
	const CFunctions *m_poFunctions;
	const CLiveData *m_poLiveData;
	const CProcedures *m_poProcedures;
public:
	CRepositories (const CConnector *poConnector);
	~CRepositories ();
	const CFunctions *GetFunctions ();
	const CLiveData *GetLiveData ();
	const CProcedures *GetProcedures ();
};

#endif /* ifndef __inc_og_rstats_client_repositories_h */
