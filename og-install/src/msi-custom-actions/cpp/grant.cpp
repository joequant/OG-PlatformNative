/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "msisupport.h"
#include "sidsupport.h"
#include <NTSecAPI.h>

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
	DWORD dwResult;
	LSA_HANDLE hPolicy = NULL;
	PSID psid = NULL;
	CCustomActionData oCustomActionData (hInstall);
	PTSTR pszAccountName = oCustomActionData.Get ();
	if (pszAccountName) {
		hPolicy = _OpenPolicy ();
		if (hPolicy) {
			CSecurityAccount oSecurityAccount (pszAccountName);
			PSID psid = oSecurityAccount.GetSID ();
			if (psid) {
				if (_Grant (hPolicy, psid)) {
					dwResult = ERROR_SUCCESS;
					goto cleanup;
				}
			}
		}
	}
	dwResult = GetLastError ();
cleanup:
	if (hPolicy) LsaClose (hPolicy);
	return dwResult;
}