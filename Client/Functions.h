/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_functions_h
#define __inc_og_pirate_client_functions_h

#include <Connector/Functions.h>
#include Client(Entities.h)

class CFunctionEntry : public CEntityEntry {
public:
	CFunctionEntry (int nInvocationId, com_opengamma_language_function_Definition *pDefinition);
	~CFunctionEntry ();
	com_opengamma_language_Data *Invoke (CConnector *poConnector, com_opengamma_language_Data **ppArg);
};

class CFunctions : public CEntities {
private:
	CFunctions (CConnector *poConnector, com_opengamma_language_function_Available *pAvailable);
	~CFunctions ();
public:
	static CFunctions *GetAvailable (CFunctionQueryAvailable *poQuery);
	CFunctionEntry *Get (int n) { return (CFunctionEntry*)GetImpl (n); }
	com_opengamma_language_Data *Invoke (CFunctionEntry *poEntry, com_opengamma_language_Data **ppArg) { return poEntry->Invoke (GetConnector (), ppArg); }
};

#endif /* ifndef __inc_og_pirate_client_functions_h */
