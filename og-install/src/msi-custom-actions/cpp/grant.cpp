/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <windows.h>
#include <NTSecAPI.h>
#include <MsiQuery.h>

#define ACTION_DATA TEXT ("CustomActionData")

#include <stdio.h>

/// Fetches the account name from the "Action Data" part of the installation context.
///
/// The string is allocated on the process heap - the caller must free it when done.
///
/// @param[in] hInstall the installation context
/// @return the account name, or NULL if there is an error (call GetLastError)
static PTSTR _GetAccountName (MSIHANDLE hInstall) {
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

/// Opens the Local Security Policy object.
///
/// The caller must close it when done.
///
/// @return the LSP, or NULL if there is a problem (call GetLastError)
static LSA_HANDLE _OpenPolicy () {
	LSA_OBJECT_ATTRIBUTES attribs;
	LSA_HANDLE handle;
	NTSTATUS sta;
	ZeroMemory (&attribs, sizeof (attribs));
	sta = LsaOpenPolicy (NULL, &attribs, POLICY_LOOKUP_NAMES | POLICY_CREATE_ACCOUNT, &handle);
	if (sta != 0) {
		SetLastError (LsaNtStatusToWinError (sta));
		return NULL;
	}
	return handle;
}

/// Resolves an account name to a SID needed by the LSA APIs.
///
/// The SID is allocated on the process heap - the caller must free it when done
///
/// @param[in] pszAccountName the account name to look up
/// @return the SID or NULL if there is a problem
static PSID _LookupSid (PCTSTR pszAccountName) {
	DWORD cbSID = 256;
	DWORD cchDomain = 256;
	PSID psid;
	PTSTR pszDomain;
	SID_NAME_USE eUse;
	psid = (PSID)HeapAlloc (GetProcessHeap (), 0, cbSID);
	if (!psid) {
		return NULL;
	}
	pszDomain = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchDomain * sizeof (TCHAR));
	if (!pszDomain) {
		HeapFree (GetProcessHeap (), 0, psid);
		return NULL;
	}
	while (!LookupAccountName (NULL, pszAccountName, psid, &cbSID, pszDomain, &cchDomain, &eUse)) {
		switch (GetLastError ()) {
		case ERROR_INSUFFICIENT_BUFFER :
			psid = HeapReAlloc (GetProcessHeap (), 0, psid, cbSID);
			if (psid == NULL) goto cleanup;
			break;
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

/// Grants the "Log on as a service" right to the account.
///
/// @param[in] hPolicy the Local Security Policy object
/// @param[in] psid the account to update
/// @return TRUE if the right was granted, FALSE otherwise (call GetLastError)
static BOOL _Grant (LSA_HANDLE hPolicy, PSID psid) {
	BOOL bResult = FALSE;
	LSA_UNICODE_STRING rights;
	NTSTATUS sta;
	ZeroMemory (&rights, sizeof (rights));
	rights.Buffer = L"SeServiceLogonRight";
	rights.Length = 19 * sizeof (WCHAR); // not including the null
	rights.MaximumLength = 20 * sizeof (WCHAR); // including the null
	sta = LsaAddAccountRights (hPolicy, psid, &rights, 1);
	if (sta == 0) {
		return TRUE;
	} else {
		SetLastError (LsaNtStatusToWinError (sta));
		return FALSE;
	}
}

/// Fetch the account (or group) name and grant it the privileges needed to run as
/// a service. The account is passed as the "Action Data" part of a custom action
/// from the MSI.
///
/// @param[in] hInstall the installation context
/// @return 0 if successful, otherwise a Win32 error code
DWORD __declspec(dllexport) __stdcall GrantServicePrivileges (MSIHANDLE hInstall) {
	DWORD dwResult = 0;
	PTSTR pszAccountName = NULL;
	LSA_HANDLE hPolicy = NULL;
	PSID psid = NULL;
	do {
		pszAccountName = _GetAccountName (hInstall);
		if (!pszAccountName) goto fail;
		hPolicy = _OpenPolicy ();
		if (!hPolicy) goto fail;
		psid = _LookupSid (pszAccountName);
		if (!psid) goto fail;
		if (!_Grant (hPolicy, psid)) goto fail;
		dwResult = 0;
		break;
fail:
		dwResult = GetLastError ();
	} while (FALSE);
	if (pszAccountName) HeapFree (GetProcessHeap (), 0, pszAccountName);
	if (hPolicy) LsaClose (hPolicy);
	if (psid) HeapFree (GetProcessHeap (), 0, psid);
	return dwResult;
}