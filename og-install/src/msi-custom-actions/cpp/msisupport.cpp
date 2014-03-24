/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "msisupport.h"
#include <tchar.h>

/// Creates an instance, querying the data. The data is held in the object and freed
/// at object destruction.
///
/// @param[in] hInstall the installation context
/// @param[in] pszProperty the name of the property
CMsiPropertyValue::CMsiPropertyValue (MSIHANDLE hInstall, PCTSTR pszProperty) {
	m_pszData = Get (hInstall, pszProperty);
	if (m_pszData) {
		m_dwError = ERROR_SUCCESS;
	} else {
		m_dwError = GetLastError ();
	}
}

/// Destroys the instance, freeing any data that was allocated.
CMsiPropertyValue::~CMsiPropertyValue () {
	if (m_pszData) {
		HeapFree (m_pszData, 0, NULL);
	}
}

/// Fetches a property from the installation context.
///
/// The string is allocated on the process heap - the caller must free it when done.
///
/// @param[in] hInstall the installation context
/// @param[in] pszProperty the property name to query, not NULL
/// @return the account name, or NULL if there is an error (call GetLastError)
PTSTR CMsiPropertyValue::Get (MSIHANDLE hInstall, PCTSTR pszProperty) {
	DWORD cchBuffer = 0;
	DWORD dwResult;
	dwResult = MsiGetProperty (hInstall, pszProperty, TEXT (""), &cchBuffer);
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
	dwResult = MsiGetProperty (hInstall, pszProperty, pszAccountName, &cchBuffer);
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
PTSTR CMsiPropertyValue::Get () {
	if (!m_pszData) {
		SetLastError (m_dwError);
	}
	return m_pszData;
}

/// Tests if the value of the property is equal to the given text. Comparison is case-insensitive.
///
/// @param[in] pszText the text to test, not NULL
/// @return TRUE if the value is equal to the text, FALSE otherwise
BOOL CMsiPropertyValue::Equals (PCTSTR pszText) {
	PCTSTR pszValue = Get ();
	return pszValue && !_tcsicmp (Get (), pszText);
}