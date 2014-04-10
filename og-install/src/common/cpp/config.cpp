/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "config.h"

CConfigString::CConfigString (PCSTR pszParameter, PCSTR pszDefault) {
	m_pszParameter = pszParameter;
	m_pszDefault = pszDefault;
	m_pszValue = NULL;
}

CConfigString::~CConfigString () {
	delete m_pszValue;
}

BOOL CConfigString::Read (CConfigSourceSection *poConfig) {
	char szValue[256];
	if (poConfig->ReadString (m_pszParameter, szValue, sizeof (szValue), m_pszDefault) > 0) {
		delete m_pszValue;
		m_pszValue = _strdup (szValue);
		return TRUE;
	} else {
		return FALSE;
	}
}

CConfigMultiString::CConfigMultiString (PCSTR pszCount, PCSTR pszParameter) {
	m_pszCount = pszCount;
	m_pszParameter = pszParameter;
	m_nValues = 0;
	m_ppszValues = NULL;
}

CConfigMultiString::~CConfigMultiString () {
	if (m_ppszValues) {
		UINT n;
		for (n = 0; n < m_nValues; n++) {
			delete m_ppszValues[n];
		}
		delete m_ppszValues;
	}
}

/// Processes the value, allocating a copy.
///
/// @param[in] pszValue the value to process, not NULL
/// @return a heap allocated copy of the value (or a transformed version), NULL if out of memory
PSTR CConfigMultiString::Malloc (PCSTR pszValue) {
	return _strdup (pszValue);
}

BOOL CConfigMultiString::Read (CConfigSourceSection *poConfig) {
	if (m_ppszValues) {
		UINT n;
		for (n = 0; n < m_nValues; n++) {
			delete m_ppszValues[n];
		}
		delete m_ppszValues;
		m_ppszValues = NULL;
	}
	int nCount = poConfig->ReadInteger (m_pszCount, 0);
	if (nCount < 0) {
		return FALSE;
	}
	if (!nCount) {
		return TRUE;
	}
	m_ppszValues = (PSTR*)malloc (sizeof (PSTR) * nCount);
	if (!m_ppszValues) {
		return FALSE;
	}
	ZeroMemory (m_ppszValues, sizeof (PSTR) * nCount);
	m_nValues = (UINT)nCount;
	UINT nIndex;
	for (nIndex = 0; nIndex < m_nValues; nIndex++) {
		char szKey[16];
		char szValue[256];
		StringCbPrintf (szKey, sizeof (szKey), m_pszParameter, nIndex);
		if (poConfig->ReadString (szKey, szValue, sizeof (szValue), NULL) == 0) {
			return FALSE;
		}
		m_ppszValues[nIndex] = Malloc (szValue);
		if (!m_ppszValues[nIndex]) return FALSE;
	}
	return TRUE;
}

CConfigSection::CConfigSection (PCSTR pszSection, UINT nEntries, CConfigEntry **ppEntries) {
	m_pszSection = pszSection;
	m_nEntries = nEntries;
	m_ppEntries = ppEntries;
}

BOOL CConfigSection::Read (CConfigSource *poConfig) {
	if (!m_ppEntries || !m_pszSection) {
		return FALSE;
	}
	CConfigSourceSection *poSection = poConfig->OpenSection (m_pszSection);
	if (!poSection) {
		return FALSE;
	}
	BOOL bResult = TRUE;
	UINT nIndex;
	for (nIndex = 0; nIndex < m_nEntries; nIndex++) {
		if (m_ppEntries[nIndex]) {
			if (!m_ppEntries[nIndex]->Read (poSection)) {
				bResult = FALSE;
				break;
			}
		} else {
			bResult = FALSE;
			break;
		}
	}
	delete poSection;
	return bResult;
}

CConfig::CConfig (UINT nSections, CConfigSection **ppSections) {
	m_nSections = nSections;
	m_ppSections = ppSections;
}

BOOL CConfig::Read (CConfigSource *poConfig) {
	if (!m_ppSections) {
		return FALSE;
	}
	UINT nIndex;
	for (nIndex = 0; nIndex < m_nSections; nIndex++) {
		if (m_ppSections[nIndex]) {
			if (!m_ppSections[nIndex]->Read (poConfig)) {
				return FALSE;
			}
		} else {
			return FALSE;
		}
	}
	return TRUE;
}

BOOL CConfig::Read (PCSTR pszFilename) {
	CFileConfigSource oConfig (pszFilename);
	return Read (&oConfig);
}
