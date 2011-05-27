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
	CProcedureEntry (int nInvocationId, const com_opengamma_language_procedure_Definition *pDefinition);
	~CProcedureEntry ();
	com_opengamma_language_Data *Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg) const;
};

class CProcedures : public CEntities {
private:
	CProcedures (const CConnector *poConnector, const com_opengamma_language_procedure_Available *pAvailable);
	~CProcedures ();
public:
	static const CProcedures *GetAvailable (CProcedureQueryAvailable *poQuery);
	const CProcedureEntry *Get (int n) { return (const CProcedureEntry*)GetImpl (n); }
	com_opengamma_language_Data *Invoke (const CProcedureEntry *poEntry, const com_opengamma_language_Data * const *ppArg) const { return poEntry->Invoke (GetConnector (), ppArg); }
};

#endif /* ifndef __inc_og_pirate_client_procedures_h */
