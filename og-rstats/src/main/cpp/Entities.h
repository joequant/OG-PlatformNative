/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_entities_h
#define __inc_og_rstats_client_entities_h

#include <connector/cpp/com_opengamma_language_definition_Definition.h>
#include <util/cpp/Atomic.h>
#include Client(Parameter.h)

class CEntityEntry {
private:
	int m_nInvocationId;
	char *m_pszCategory;
	char *m_pszDescription;
	char *m_pszName;
	int m_nParameter;
	const CParameter **m_ppoParameter;
protected:
	CEntityEntry (int nInvocationId, const com_opengamma_language_definition_Definition *pDefinition);
	int GetInvocationId () const { return m_nInvocationId; }
public:
	virtual ~CEntityEntry ();
	const char *GetCategory () const { return m_pszCategory; }
	const char *GetDescription () const { return m_pszDescription; }
	const char *GetName () const { return m_pszName; }
	int GetParameterCount () const { return m_nParameter; }
	const CParameter *GetParameter (int n) const;
};

class CEntities {
private:
	mutable CAtomicInt m_oRefCount;
	int m_nEntity;
	const CEntityEntry **m_ppoEntity;
	const CConnector *m_poConnector;
protected:
	CEntities (const CConnector *poConnector, int nEntity);
	virtual ~CEntities ();
	const CEntityEntry *GetImpl (int n) const;
	const CEntityEntry *GetImpl (const char *pszName) const;
	void SetImpl (int n, const CEntityEntry *poEntity);
	const CConnector *GetConnector () const { return m_poConnector; } // Does not increment the refcount
public:
	void Retain () const { m_oRefCount.IncrementAndGet (); }
	static void Release (const CEntities *poEntities) { if (!poEntities->m_oRefCount.DecrementAndGet ()) delete poEntities; }
	int Size () const { return m_nEntity; }
};

#endif /* ifndef __inc_og_rstats_client_entities_h */
