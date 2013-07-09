/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_procedures_h
#define __inc_og_rstats_client_procedures_h

#include "com_opengamma_rstats_msg_ProcedureResult.h"
#define CLASS_com_opengamma_language_procedure_Result com_opengamma_rstats_msg_ProcedureResult
#include <connector/cpp/Procedures.h>
#include Client(Entities.h)

class CProcedureEntry : public CEntityEntry {
public:
	CProcedureEntry (int nInvocationId, const com_opengamma_language_procedure_Definition *pDefinition);
	~CProcedureEntry ();
	bool Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg, com_opengamma_language_Data **ppResult, com_opengamma_rstats_msg_DataInfo **ppInfo) const;
};

class CProcedures : public CEntities {
private:
	CProcedures (const CConnector *poConnector, const com_opengamma_language_procedure_Available *pAvailable);
	~CProcedures ();
public:
	static const CProcedures *GetAvailable (CProcedureQueryAvailable *poQuery);
	const CProcedureEntry *Get (int n) const { return (const CProcedureEntry*)GetImpl (n); }
	const CProcedureEntry *Get (const char *pszName) const { return (const CProcedureEntry*)GetImpl (pszName); }
	bool Invoke (const CProcedureEntry *poEntry, const com_opengamma_language_Data * const *ppArg, com_opengamma_language_Data **ppResult, com_opengamma_rstats_msg_DataInfo **ppInfo) const { return poEntry->Invoke (GetConnector (), ppArg, ppResult, ppInfo); }
};

#endif /* ifndef __inc_og_rstats_client_procedures_h */
