/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_procedures_h
#define __inc_og_pirate_client_procedures_h

#include <Connector/Procedures.h>
#include <Connector/com_opengamma_language_Data.h>
#include Client(Entities.h)

class CProcedureEntry : public CEntityEntry {
public:
	CProcedureEntry (int nInvocationId, com_opengamma_language_procedure_Definition *pDefinition);
	~CProcedureEntry ();
	com_opengamma_language_Data *Invoke (CConnector *poConnector, com_opengamma_language_Data **ppArg);
};

class CProcedures : public CEntities {
private:
	CProcedures (CConnector *poConnector, com_opengamma_language_procedure_Available *pAvailable);
	~CProcedures ();
public:
	static CProcedures *GetAvailable (CProcedureQueryAvailable *poQuery);
	CProcedureEntry *Get (int n) { return (CProcedureEntry*)GetImpl (n); }
	com_opengamma_language_Data *Invoke (CProcedureEntry *poEntry, com_opengamma_language_Data **ppArg) { return poEntry->Invoke (GetConnector (), ppArg); }
};

#endif /* ifndef __inc_og_pirate_client_procedures_h */
