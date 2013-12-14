/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "StringSet.h"
#include "Log.h"

CStringSet::CStringSet () {
	m_nSize = 8;
	m_nCount = 0;
	m_apsz = new PTSTR[m_nSize];
	if (!m_apsz) {
		LogOutOfMemory ();
	}
}

CStringSet::~CStringSet () {
	if (m_apsz) {
		int i;
		for (i = 0; i < m_nCount; i++) {
			delete m_apsz[i];
		}
		delete m_apsz;
	}
}

void CStringSet::Add (PCTSTR psz) {
	if (m_nCount >= m_nSize) {
		int nNewSize = m_nSize * 2;
		PTSTR *apsz = new PTSTR[nNewSize];
		if (!apsz) {
			LogOutOfMemory ();
			return;
		}
		memcpy (apsz, m_apsz, m_nCount * sizeof (PTSTR));
		m_nSize = nNewSize;
		delete m_apsz;
		m_apsz = apsz;
	}
	if (m_apsz) {
		m_apsz[m_nCount++] = _tcsdup (psz);
	}
}

BOOL CStringSet::Contains (PCTSTR psz) {
	if (!m_apsz) return FALSE;
	int i;
	for (i = 0; i < m_nCount; i++) {
		if (m_apsz[i] && !_tcscmp (m_apsz[i], psz)) {
			return TRUE;
		}
	}
	return FALSE;
}

int CStringSet::Size () {
	return m_nCount;
}

PCTSTR CStringSet::Get (int nIndex) {
	if ((nIndex < 0) || (nIndex >= m_nCount) || !m_apsz) {
		LogDebug (TEXT ("Invalid index %d"), nIndex);
		return NULL;
	}
	return m_apsz[nIndex];
}