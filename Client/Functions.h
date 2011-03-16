/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_functions_h
#define __inc_og_pirate_client_functions_h

#include <Connector/Functions.h>
#include <Connector/com_opengamma_language_Data.h>
#include <Util/Atomic.h>
#include Client(Parameter.h)

class CFunctionEntry {
private:
	int m_nInvocationId;
	char *m_pszName;
	int m_nParameter;
	CParameter **m_ppoParameter;
public:
	CFunctionEntry (int nInvocationId, com_opengamma_language_function_Definition *pDefinition);
	~CFunctionEntry ();
	const char *GetName () { return m_pszName; }
	int GetParameterCount () { return m_nParameter; }
	CParameter *GetParameter (int n);
	com_opengamma_language_Data *Invoke (CConnector *poConnector, com_opengamma_language_Data **ppArg);
};

class CFunctions {
private:
	CAtomicInt m_oRefCount;
	int m_nFunction;
	CFunctionEntry **m_ppoFunction;
	CConnector *m_poConnector;
	CFunctions (CConnector *poConnector, com_opengamma_language_function_Available *pAvailable);
	~CFunctions ();
public:
	static CFunctions *GetAvailable (CFunctionQueryAvailable *poQuery);
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CFunctions *poFunctions) { if (!poFunctions->m_oRefCount.DecrementAndGet ()) delete poFunctions; }
	int Size () { return m_nFunction; }
	CFunctionEntry *Get (int n);
	com_opengamma_language_Data *Invoke (CFunctionEntry *poEntry, com_opengamma_language_Data **ppArg) { return poEntry->Invoke (m_poConnector, ppArg); }
};

#endif /* ifndef __inc_og_pirate_client_functions_h */
