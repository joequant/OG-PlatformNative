/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_procedures_h
#define __inc_og_pirate_client_procedures_h

#include <Connector/Procedures.h>
#include <Util/Atomic.h>

class CProcedureEntry {
private:
	int m_nInvocationId;
	char *m_pszName;
public:
	CProcedureEntry (int nInvocationId, com_opengamma_language_procedure_Definition *pDefinition);
	~CProcedureEntry ();
	const char *GetName () { return m_pszName; }
};

class CProcedures {
private:
	CAtomicInt m_oRefCount;
	int m_nProcedure;
	CProcedureEntry **m_ppoProcedure;
	CProcedures (com_opengamma_language_procedure_Available *pAvailable);
	~CProcedures ();
public:
	static CProcedures *GetAvailable (CProcedureQueryAvailable *poQuery);
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CProcedures *poProcedures) { if (!poProcedures->m_oRefCount.DecrementAndGet ()) delete poProcedures; }
	int Size () { return m_nProcedure; }
	CProcedureEntry *Get (int n);
};

#endif /* ifndef __inc_og_pirate_client_procedures_h */
