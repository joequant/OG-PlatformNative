/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_functions_h
#define __inc_og_rstats_client_functions_h

#include "com_opengamma_rstats_msg_FunctionResult.h"
#define CLASS_com_opengamma_language_function_Result com_opengamma_rstats_msg_FunctionResult
#include <connector/cpp/Functions.h>
#include Client(Entities.h)

class CFunctionEntry : public CEntityEntry {
public:
	CFunctionEntry (int nInvocationId, const com_opengamma_language_function_Definition *pDefinition);
	~CFunctionEntry ();
	com_opengamma_language_Data *Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg, com_opengamma_rstats_msg_DataInfo **ppInfo) const;
};

class CFunctions : public CEntities {
private:
	CFunctions (const CConnector *poConnector, const com_opengamma_language_function_Available *pAvailable);
	~CFunctions ();
public:
	static const CFunctions *GetAvailable (CFunctionQueryAvailable *poQuery);
	const CFunctionEntry *Get (int n) const { return (const CFunctionEntry*)GetImpl (n); }
	com_opengamma_language_Data *Invoke (const CFunctionEntry *poEntry, const com_opengamma_language_Data * const *ppArg, com_opengamma_rstats_msg_DataInfo **ppInfo) const { return poEntry->Invoke (GetConnector (), ppArg, ppInfo); }
};

#endif /* ifndef __inc_og_rstats_client_functions_h */
