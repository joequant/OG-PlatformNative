/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Entities.h)

LOGGING (com.opengamma.rstats.client.Entities);

CEntityEntry::CEntityEntry (int nInvocationId, const com_opengamma_language_definition_Definition *pDefinition) {
	m_nInvocationId = nInvocationId;
	m_pszName = _tcsAsciiDup (pDefinition->_name);
	m_nParameter = pDefinition->fudgeCountParameter;
	m_ppoParameter = new const CParameter*[m_nParameter];
	int n;
	for (n = 0; n < m_nParameter; n++) {
		m_ppoParameter[n] = new CParameter (pDefinition->_parameter[n]);
	}
}

CEntityEntry::~CEntityEntry () {
	free (m_pszName);
	int n;
	for (n = 0; n < m_nParameter; n++) {
		delete m_ppoParameter[n];
	}
	delete m_ppoParameter;
}

const CParameter *CEntityEntry::GetParameter (int n) const {
	if ((n < 0) || (n >= m_nParameter)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nParameter << TEXT (")"));
		return NULL;
	}
	return m_ppoParameter[n];
}

CEntities::CEntities (const CConnector *poConnector, int nEntity)
: m_oRefCount (1) {
	LOGINFO (TEXT ("Creating ") << nEntity << TEXT (" entity repository"));
	m_nEntity = nEntity;
	m_ppoEntity = new const CEntityEntry*[nEntity];
	memset (m_ppoEntity, 0, sizeof (CEntityEntry*) * nEntity);
	poConnector->Retain ();
	m_poConnector = poConnector;
}

CEntities::~CEntities () {
	assert (m_oRefCount.Get () == 0);
	int n;
	for (n = 0; n < m_nEntity; n++) {
		delete m_ppoEntity[n];
	}
	delete m_ppoEntity;
}

const CEntityEntry *CEntities::GetImpl (int n) const {
	if ((n < 0) || (n >= m_nEntity)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nEntity << TEXT (")"));
		return NULL;
	}
	return m_ppoEntity[n];
}

/// Fetches an entry by name. Note that this is currently implemented as a linear search and could therefore
/// be quite a costly operation on large entity repositories.
///
/// @param[in] pszName name to match
/// @return the entity found, or NULL if none
const CEntityEntry *CEntities::GetImpl (const char *pszName) const {
	int n;
	for (n = 0; n < m_nEntity; n++) {
		if (!strcmp (m_ppoEntity[n]->GetName (), pszName)) {
			return m_ppoEntity[n];
		}
	}
	LOGWARN ("No entity found for '" << pszName << "'");
	return NULL;
}

void CEntities::SetImpl (int n, const CEntityEntry *poEntry) {
	if ((n < 0) || (n >= m_nEntity)) {
		LOGWARN (TEXT ("Index ") << n << TEXT (" out of range (") << m_nEntity << TEXT (")"));
		return;
	}
	if (m_ppoEntity[n]) {
		delete m_ppoEntity[n];
	}
	m_ppoEntity[n] = poEntry;
}
