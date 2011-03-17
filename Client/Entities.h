/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_entities_h
#define __inc_og_pirate_client_entities_h

#include <Connector/com_opengamma_language_definition_Definition.h>
#include <Util/Atomic.h>
#include Client(Parameter.h)

class CEntityEntry {
private:
	int m_nInvocationId;
	char *m_pszName;
	int m_nParameter;
	CParameter **m_ppoParameter;
protected:
	CEntityEntry (int nInvocationId, com_opengamma_language_definition_Definition *pDefinition);
	int GetInvocationId () { return m_nInvocationId; }
public:
	virtual ~CEntityEntry ();
	const char *GetName () { return m_pszName; }
	int GetParameterCount () { return m_nParameter; }
	CParameter *GetParameter (int n);
};

class CEntities {
private:
	CAtomicInt m_oRefCount;
	int m_nEntity;
	CEntityEntry **m_ppoEntity;
	CConnector *m_poConnector;
protected:
	CEntities (CConnector *poConnector, int nEntity);
	virtual ~CEntities ();
	CEntityEntry *GetImpl (int n);
	void SetImpl (int n, CEntityEntry *poEntity);
	CConnector *GetConnector () { return m_poConnector; } // Does not increment the refcount
public:
	void Retain () { m_oRefCount.IncrementAndGet (); }
	static void Release (CEntities *poEntities) { if (!poEntities->m_oRefCount.DecrementAndGet ()) delete poEntities; }
	int Size () { return m_nEntity; }
};

#endif /* ifndef __inc_og_pirate_client_entities_h */
