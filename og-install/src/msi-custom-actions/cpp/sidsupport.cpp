/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "sidsupport.h"

/// Creates a new instance, looking up the SID for the given name.
///
/// The account name parameter must remain valid for the lifetime of this object.
///
/// @param[in] pszName the account name, not NULL
CSecurityAccount::CSecurityAccount (PCTSTR pszName) {
	m_pszName = (PTSTR)pszName;
	m_bFreeName = FALSE;
	m_psid = GetSID (pszName);
	m_bFreeSID = TRUE;
	if (m_psid) {
		m_dwError = ERROR_SUCCESS;
	} else {
		m_dwError = GetLastError ();
	}
}

/// Creates a new instance, looking up the account name for the given SID.
///
/// The SID parameter must remain valid for the lifetime of this object.
///
/// @param[in] psid the SID, not NULL
CSecurityAccount::CSecurityAccount (PSID psid) {
	m_pszName = GetName (psid);
	m_bFreeName = TRUE;
	m_psid = psid;
	m_bFreeSID = FALSE;
	if (m_pszName) {
		m_dwError = ERROR_SUCCESS;
	} else {
		m_dwError = GetLastError ();
	}
}

/// Destroys the instance, freeing any data that was allocated.
CSecurityAccount::~CSecurityAccount () {
	if (m_bFreeName && m_pszName) HeapFree (GetProcessHeap (), 0, m_pszName);
	if (m_bFreeSID && m_psid) HeapFree (GetProcessHeap (), 0, m_psid);
}

/// Returns the account name held in the object. The string will not exist beyond the
/// lifetime of the object. This will either be the name resolved from the SID, or have
/// been passed directly in at construction.
///
/// @return the name, or NULL if there is a problem (use GetLastError to investigate why)
PCTSTR CSecurityAccount::GetName () {
	if (!m_pszName) {
		SetLastError (m_dwError);
	}
	return m_pszName;
}

/// Returns the SID held in the object. The SID will not exist beyond the lifetime of
/// the object. This will either be the SID resolved from the name, or have been passed
/// directly in at construction.
///
/// @return the SID, or NULL if there is a problem (use GetLastError to investigate why)
PSID CSecurityAccount::GetSID () {
	if (!m_psid) {
		SetLastError (m_dwError);
	}
	return m_psid;
}

/// Resolves a SID to an account name.
///
/// The account name is allocated on the process heap - the caller must free it when done
///
/// @param[in] psid the SID to look up
/// @return the name or NULL if there is a problem, call GetLastError to find out why
PTSTR CSecurityAccount::GetName (PSID psid) {
	DWORD cchName = 256;
	DWORD cchDomain = 256;
	PTSTR pszName = NULL;
	PTSTR pszDomain = NULL;
	SID_NAME_USE eUse;
	pszName = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchName * sizeof (TCHAR));
	if (!pszName) goto cleanup;
	pszDomain = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchDomain * sizeof (TCHAR));
	if (!pszDomain) {
		HeapFree (GetProcessHeap (), 0, pszName);
		SetLastError (ERROR_OUTOFMEMORY);
		return NULL;
	}
	while (!LookupAccountSid (NULL, psid, pszName, &cchName, pszDomain, &cchDomain, &eUse)) {
		switch (GetLastError ()) {
		case ERROR_INSUFFICIENT_BUFFER :
			pszName = (PTSTR)HeapReAlloc (GetProcessHeap (), 0, pszName, cchName * sizeof (TCHAR));
			if (pszName) {
				pszDomain = (PTSTR)HeapReAlloc (GetProcessHeap (), 0, pszDomain, cchDomain * sizeof (TCHAR));
				if (pszName) {
					break;
				}
			}
			SetLastError (ERROR_OUTOFMEMORY);
			goto cleanup;
		default :
			HeapFree (GetProcessHeap (), 0, pszName);
			pszName = NULL;
			goto cleanup;
		}
	}
cleanup:
	if (pszDomain) HeapFree (GetProcessHeap (), 0, pszDomain);
	return pszName;
}

/// Resolves an account name to a SID.
///
/// The SID is allocated on the process heap - the caller must free it when done
///
/// @param[in] pszName the account name to look up
/// @return the SID or NULL if there is a problem, call GetLastError to find out why
PSID CSecurityAccount::GetSID (PCTSTR pszName) {
	DWORD cbSID = 256;
	DWORD cchDomain = 256;
	PSID psid = NULL;
	PTSTR pszDomain = NULL;
	SID_NAME_USE eUse;
	psid = (PSID)HeapAlloc (GetProcessHeap (), 0, cbSID);
	if (!psid) goto cleanup;
	pszDomain = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchDomain * sizeof (TCHAR));
	if (!pszDomain) {
		HeapFree (GetProcessHeap (), 0, psid);
		SetLastError (ERROR_OUTOFMEMORY);
		return NULL;
	}
	while (!LookupAccountName (NULL, pszName, psid, &cbSID, pszDomain, &cchDomain, &eUse)) {
		switch (GetLastError ()) {
		case ERROR_INSUFFICIENT_BUFFER :
			psid = HeapReAlloc (GetProcessHeap (), 0, psid, cbSID);
			if (psid) {
				pszDomain = (PTSTR)HeapReAlloc (GetProcessHeap (), 0, pszDomain, cchDomain * sizeof (TCHAR));
				if (pszDomain) {
					break;
				}
			}
			SetLastError (ERROR_OUTOFMEMORY);
			goto cleanup;
		default :
			HeapFree (GetProcessHeap (), 0, psid);
			psid = NULL;
			goto cleanup;
		}
	}
cleanup:
	if (pszDomain) HeapFree (GetProcessHeap (), 0, pszDomain);
	return psid;
}
