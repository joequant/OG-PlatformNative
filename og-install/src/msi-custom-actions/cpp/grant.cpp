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

/// Grants or removes the "Log on as a service" right to the account.
///
/// @param[in] hPolicy the Local Security Policy object
/// @param[in] psid the account to update
/// @param[in] bGrant TRUE to grant the right, FALSE to revoke it
/// @return TRUE if the right was granted, FALSE otherwise (call GetLastError)
static BOOL _GrantOrRevoke (LSA_HANDLE hPolicy, PSID psid, BOOL bGrant) {
	BOOL bResult = FALSE;
	LSA_UNICODE_STRING rights;
	NTSTATUS sta;
	ZeroMemory (&rights, sizeof (rights));
	rights.Buffer = L"SeServiceLogonRight";
	rights.Length = 19 * sizeof (WCHAR); // not including the null
	rights.MaximumLength = 20 * sizeof (WCHAR); // including the null
	sta = bGrant ? LsaAddAccountRights (hPolicy, psid, &rights, 1) : LsaRemoveAccountRights (hPolicy, psid, FALSE, &rights, 1);
	if (sta == 0) {
		return TRUE;
	} else {
		SetLastError (LsaNtStatusToWinError (sta));
		return FALSE;
	}
}

/// Implements the grant/revoke - the behaviour controlled by a boolean flag. This is
/// the main implementation of both GrantServicePrivileges and RevokeServicePrivileges.
///
/// @param[in] hInstall the installation context
/// @param[in] bGrant TRUE to grant the right, FALSE to revoke
/// @return 0 if successful, otherwise a Win32 error code
static DWORD _SetServicePrivileges (MSIHANDLE hInstall, BOOL bGrant) {
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
				if (_GrantOrRevoke (hPolicy, psid, bGrant)) {
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

/// Fetch the account (or group) name and grant it the privileges needed to run as
/// a service. The account is passed as the "Action Data" part of a custom action
/// from the MSI.
///
/// @param[in] hInstall the installation context
/// @return 0 if successful, otherwise a Win32 error code
DWORD __declspec(dllexport) __stdcall GrantServicePrivileges (MSIHANDLE hInstall) {
	// Note that we can't check for "REMOVE=ALL" to automatically revoke the privilege
	// during uninstall because Advanced Installer has already deleted the account at
	// this point on the uninstall. An explicit REVOKE must be specified.
	return _SetServicePrivileges (hInstall, TRUE);
}

/// Fetch the account (or group) name and remove the privileges needed to run as
/// a service. The account is passed as the "Action Data" part of a custom action
/// from the MSI.
///
/// @param[in] hInstall the installation context
/// @return 0 if successful, otherwise a Win32 error code
DWORD __declspec(dllexport) __stdcall RevokeServicePrivileges (MSIHANDLE hInstall) {
	return _SetServicePrivileges (hInstall, FALSE);
}