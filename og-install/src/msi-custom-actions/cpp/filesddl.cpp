/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "msisupport.h"
#include "sidsupport.h"
#include <tchar.h>
#include <strsafe.h>
#include <AclAPI.h>
#ifdef _DEBUG
#include <stdio.h>
#endif /* ifdef _DEBUG */

class CFileSecurity {
private:
	PTSTR m_pszFileName;
	SECURITY_DESCRIPTOR *m_psdFile;
	BOOL m_bDaclPresent;
	PACL m_pDacl;
	BOOL m_bDaclDefaulted;
	void Init0 ();
	BOOL _GetFileSecurity ();
	BOOL Init ();
public:
	CFileSecurity (PCTSTR pszPath);
	CFileSecurity (PCTSTR pszPath, PCTSTR pszFileName);
	~CFileSecurity ();
	BOOL IsPresent () { return m_bDaclPresent; }
	BOOL IsDefaulted () { return m_bDaclDefaulted; }
	BOOL IsProtected ();
	DWORD RemoveInvalidACEs ();
	DWORD EnforceDacl ();
};

void CFileSecurity::Init0 () {
	m_pszFileName = NULL;
	m_psdFile = NULL;
	m_bDaclPresent = FALSE;
	m_pDacl = NULL;
	m_bDaclDefaulted = FALSE;
}

BOOL CFileSecurity::_GetFileSecurity () {
	DWORD cbAlloc = 1024;
	DWORD cbRequired = 0;
	m_psdFile = (SECURITY_DESCRIPTOR*)HeapAlloc (GetProcessHeap (), 0, cbAlloc);
	if (!m_psdFile) return FALSE;
	while (!GetFileSecurity (m_pszFileName, DACL_SECURITY_INFORMATION, m_psdFile, cbAlloc, &cbRequired)) {
		DWORD dwError = GetLastError ();
		switch (dwError) {
		case ERROR_FILE_NOT_FOUND :
			HeapFree (GetProcessHeap (), 0, m_psdFile);
			return FALSE;
		default :
			if (cbAlloc >= cbRequired) {
				HeapFree (GetProcessHeap (), 0, m_psdFile);
				return FALSE;
			} else {
				m_psdFile = (SECURITY_DESCRIPTOR*)HeapReAlloc (GetProcessHeap (), 0, m_psdFile, cbRequired);
				if (!m_psdFile) return FALSE;
				cbAlloc = cbRequired;
			}
		}
	}
	return TRUE;
}

BOOL CFileSecurity::Init () {
	return _GetFileSecurity () && GetSecurityDescriptorDacl (m_psdFile, &m_bDaclPresent, &m_pDacl, &m_bDaclDefaulted);
}

CFileSecurity::CFileSecurity (PCTSTR pszPath) {
	Init0 ();
	// Lose the trailing slash
	size_t cchPath = _tcslen (pszPath);
	m_pszFileName = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchPath * sizeof (TCHAR));
	if (!m_pszFileName) return;
	CopyMemory (m_pszFileName, pszPath, (cchPath - 1) * sizeof (TCHAR));
	m_pszFileName[cchPath - 1] = 0;
	// Look up the security information
	Init ();
}

CFileSecurity::CFileSecurity (PCTSTR pszPath, PCTSTR pszFileName) {
	Init0 ();
	// Form the filename
	size_t cchPath = _tcslen (pszPath);
	size_t cchFileName = _tcslen (pszFileName);
	m_pszFileName = (PTSTR)HeapAlloc (GetProcessHeap (), 0, (cchPath + cchFileName + 1) * sizeof (TCHAR));
	if (!m_pszFileName) return;
	CopyMemory (m_pszFileName, pszPath, cchPath * sizeof (TCHAR));
	CopyMemory (m_pszFileName + cchPath, pszFileName, (cchFileName + 1) * sizeof (TCHAR));
	// Lookup the security information
	Init ();
}

CFileSecurity::~CFileSecurity () {
	if (m_pszFileName) HeapFree (GetProcessHeap (), 0, m_pszFileName);
	if (m_psdFile) HeapFree (GetProcessHeap (), 0, m_psdFile);
}

BOOL CFileSecurity::IsProtected () {
	SECURITY_DESCRIPTOR_CONTROL sdc;
	DWORD dwRevision;
	if (!GetSecurityDescriptorControl (m_psdFile, &sdc, &dwRevision)) {
		return FALSE;
	}
	return sdc & SE_DACL_PROTECTED;
}

DWORD CFileSecurity::RemoveInvalidACEs () {
	ACL_SIZE_INFORMATION size;
	if (!GetAclInformation (m_pDacl, &size, sizeof (size), AclSizeInformation)) {
		DWORD dwError = GetLastError ();
#ifdef _DEBUG
		_tprintf (TEXT ("Couldn't get size of ACL for %s, error %u\n"), m_pszFileName, dwError);
#endif /* ifdef _DEBUG */
		return dwError;
	}
	DWORD dw;
	for (dw = 0; dw < size.AceCount; dw++) {
		PACCESS_ALLOWED_ACE pAce;
		if (GetAce (m_pDacl, dw, (LPVOID*)&pAce)) {
			if (pAce->Header.AceType == ACCESS_ALLOWED_ACE_TYPE) {
				CSecurityAccount oAccount ((PSID)&pAce->SidStart);
				if (!oAccount.GetName ()) {
#ifdef _DEBUG
					_tprintf (TEXT ("Invalid SID at slot %u for %s\n"), dw, m_pszFileName);
#endif /* ifdef _DEBUG */
					DeleteAce (m_pDacl, dw);
					size.AceCount--;
					dw--;
				}
			}
		} else {
		DWORD dwError = GetLastError ();
#ifdef _DEBUG
		_tprintf (TEXT ("Couldn't get ACE %u for %s, error %u\n"), dw, m_pszFileName, dwError);
#endif /* ifdef _DEBUG */
		return dwError;
		}
	}
	return ERROR_SUCCESS;
}

DWORD CFileSecurity::EnforceDacl () {
#ifdef _DEBUG
	_tprintf (TEXT ("Enforcing DACL on %s\n"), m_pszFileName);
#endif /* ifdef _DEBUG */
	return SetNamedSecurityInfo (m_pszFileName, SE_FILE_OBJECT, DACL_SECURITY_INFORMATION, NULL, NULL, m_pDacl, NULL);
}

static HANDLE _FileIterator (PCTSTR pszPath, WIN32_FIND_DATA *pwfd) {
	size_t cchSearch = _tcslen (pszPath) + 4;
	PTSTR pszSearch = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchSearch * sizeof (TCHAR));
	if (!pszSearch) {
		SetLastError (ERROR_OUTOFMEMORY);
		return NULL;
	}
	StringCchPrintf (pszSearch, cchSearch, TEXT ("%s%s"), pszPath, TEXT ("*.*"));
	HANDLE hFind = FindFirstFile (pszSearch, pwfd);
	HeapFree (GetProcessHeap (), 0, pszSearch);
	return hFind;
}

/// Processes the given path, and all files and sub-folders, removing any invalid ACEs
/// from the DACLs and fixing any that should be inherited.
///
/// @param[in] pszPath the path to scan and fix, not NULL
/// @param[in] nDepth the recursion depth, 0 for a top level folder
/// @return 0 if successful, otherwise a Win32 error code
static DWORD _FixInheritanceSingle (PCTSTR pszPath, int nDepth = 0) {
#ifdef _DEBUG
	_tprintf (TEXT ("%s\n"), pszPath);
#endif /* ifdef _DEBUG */
	DWORD dwResult;
	HANDLE hFind = INVALID_HANDLE_VALUE;
	// Open the folder & process the DACL
	{
		CFileSecurity oFolder (pszPath);
		// If the DACL is "P" then:
		if (!oFolder.IsPresent ()) {
			if (nDepth == 0) {
				// This is the case where the path doesn't exist and it's the root
#ifdef _DEBUG
				_tprintf (TEXT ("%s doesn't exist - skipping\n"), pszPath);
#endif /* ifdef _DEBUG */
				dwResult = ERROR_SUCCESS;
				goto cleanup;
			}
#ifdef _DEBUG
			_tprintf (TEXT ("%s has no DACL\n"), pszPath);
#endif /* ifdef _DEBUG */
			dwResult = ERROR_INVALID_ACL;
			goto cleanup;
		}
		if (oFolder.IsProtected ()) {
			// Remove any invalid SIDs from the ACL
			dwResult = oFolder.RemoveInvalidACEs ();
			if (dwResult) goto cleanup;
			// Force re-application of the ACL
			dwResult = oFolder.EnforceDacl ();
			if (dwResult) goto cleanup;
		}
	}
	// Iterate through the folder applying the DACL updates
	WIN32_FIND_DATA wfd;
	hFind = _FileIterator (pszPath, &wfd);
	if (hFind == INVALID_HANDLE_VALUE) goto fail_win32;
	do {
		if ((wfd.cFileName[0] == '.') && (!wfd.cFileName[1] || ((wfd.cFileName[1] == '.') && !wfd.cFileName[2]))) {
			// Ignore "." and ".."
			continue;
		}
		if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
			// Recurse into sub-folder
			size_t cchSubFolder = _tcslen (pszPath) + _tcslen (wfd.cFileName) + 2;
			PTSTR pszSubFolder = (PTSTR)HeapAlloc (GetProcessHeap (), 0, cchSubFolder * sizeof (TCHAR));
			if (!pszSubFolder) {
				dwResult = ERROR_OUTOFMEMORY;
				goto cleanup;
			}
			StringCchPrintf (pszSubFolder, cchSubFolder, TEXT ("%s%s\\"), pszPath, wfd.cFileName);
			dwResult = _FixInheritanceSingle (pszSubFolder, nDepth + 1);
			HeapFree (GetProcessHeap (), 0, pszSubFolder);
			if (dwResult) goto cleanup;
		}
	} while (FindNextFile (hFind, &wfd));
	dwResult = ERROR_SUCCESS;
	goto cleanup;
fail_win32:
	dwResult = GetLastError ();
cleanup:
	if (hFind != INVALID_HANDLE_VALUE) FindClose (hFind);
	return dwResult;
}

/// Splits a ';' delimited set of paths into individual calls to _FixInheritanceSingle.
///
/// @param[in] pszPaths the paths to fix, not NULL
/// @return 0 if successful, otherwise a Win32 error code
static DWORD _FixInheritanceMulti (PCTSTR pszPaths) {
	TCHAR *pszPathsCopy = _tcsdup (pszPaths);
	if (!pszPathsCopy) return ERROR_OUTOFMEMORY;
	TCHAR *ctx;
	TCHAR *pszPath = _tcstok_s (pszPathsCopy, TEXT (";"), &ctx);
	while (pszPath) {
		DWORD dwError = _FixInheritanceSingle (pszPath);
		if (dwError) {
			free (pszPathsCopy);
			return dwError;
		}
		pszPath = _tcstok_s (NULL, TEXT (";"), &ctx);
	}
	free (pszPathsCopy);
	return ERROR_SUCCESS;
}

/// Fix the inherited ACEs on file DACLs. Any with invalid account SIDs are removed, and any
/// which should be inherited are fixed to the correct values. The paths to scan are passed
/// as the "Action Data" part of a custom action, with multiple paths separated by ';'.
///
/// @param[in] hInstall the installation context
/// @return 0 if successful, otherwise a Win32 error code
DWORD __declspec(dllexport) __stdcall FixInheritence (MSIHANDLE hInstall) {
	CCustomActionData oCustomActionData (hInstall);
	if (!oCustomActionData.Get ()) return GetLastError ();
	return _FixInheritanceMulti (oCustomActionData.Get ());
}

#ifdef _DEBUG
/// When built for debugging, this entry point can be used to test the behaviour outside of the
/// MSI environment.
extern "C" void __declspec(dllexport) __stdcall FixInheritence_debug () {
	_FixInheritanceMulti (TEXT ("C:\\Program Files\\OpenGamma Ltd\\"));
	_FixInheritanceMulti (TEXT ("C:\\Program Files (x86)\\OpenGamma Ltd\\;C:\\Program Files\\OpenGamma Ltd\\;C:\\ProgramData\\OpenGamma Ltd\\"));
}
#endif /*ifdef _DEBUG */