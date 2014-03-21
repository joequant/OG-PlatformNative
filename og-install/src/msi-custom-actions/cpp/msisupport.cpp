/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "msisupport.h"

#define ACTION_DATA TEXT ("CustomActionData")

/// Creates an instance, querying the data. The data is held in the object and freed
/// at object destruction.
///
/// @param[in] hInstall the installation context
CCustomActionData::CCustomActionData (MSIHANDLE hInstall) {
	m_pszData = Get (hInstall);
	if (m_pszData) {
		m_dwError = ERROR_SUCCESS;
	} else {
		m_dwError = GetLastError ();
	}
}

/// Destroys the instance, freeing any data that was allocated.
CCustomActionData::~CCustomActionData () {
	if (m_pszData) {
		HeapFree (m_pszData, 0, NULL);
	}
}

/// Fetches the parameter from the "Action Data" part of the installation context.
///
/// The string is allocated on the process heap - the caller must free it when done.
///
/// @param[in] hInstall the installation context
/// @return the account name, or NULL if there is an error (call GetLastError)
PTSTR CCustomActionData::Get (MSIHANDLE hInstall) {
	DWORD cchBuffer = 0;
	DWORD dwResult;
	dwResult = MsiGetProperty (hInstall, ACTION_DATA, TEXT (""), &cchBuffer);
	if (dwResult != ERROR_MORE_DATA) {
		SetLastError (dwResult);
		return NULL;
	}
	// Add room for the NULL, not counted in first return
	cchBuffer++;
	PTSTR pszAccountName = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchBuffer * sizeof (TCHAR));
	if (!pszAccountName) {
		SetLastError (ERROR_OUTOFMEMORY);
		return NULL;
	}
	dwResult = MsiGetProperty (hInstall, ACTION_DATA, pszAccountName, &cchBuffer);
	if (dwResult == ERROR_SUCCESS) {
		return pszAccountName;
	} else {
		HeapFree (pszAccountName, 0, NULL);
		SetLastError (dwResult);
		return NULL;
	}
}

/// Returns the data string held in the object. The string will not exist beyond the lifetime
/// of this object instance.
///
/// @return the string, or NULL if there was none (use GetLastError to investigate why)
PTSTR CCustomActionData::Get () {
	if (!m_pszData) {
		SetLastError (m_dwError);
	}
	return m_pszData;
}
