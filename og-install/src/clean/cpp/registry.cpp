/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "registry.h"
#include "Log.h"

static HKEY _Parent (PCTSTR *ppszPath) {
	if (!_tcsncmp (*ppszPath, TEXT ("HKLM\\"), 5)) {
		*ppszPath += 5;
		return HKEY_LOCAL_MACHINE;
	} else {
		LogDebug (TEXT ("Can't handle %s"), *ppszPath);
		return NULL;
	}
}

static HKEY _Open (PCTSTR pszPath) {
	HKEY hKey = _Parent (&pszPath);
	if (!hKey) return NULL;
	HKEY hSubKey;
	LONG lResult;
	if ((lResult = RegOpenKey (hKey, pszPath, &hSubKey)) != ERROR_SUCCESS) {
		if ((lResult == ERROR_NOT_FOUND) || (lResult == ERROR_FILE_NOT_FOUND)) {
			LogPrintf (TEXT ("%s does not exist\n"), pszPath);
		} else {
			LogDebug (TEXT ("Couldn't open %s, error %d"), pszPath, lResult);
		}
		return NULL;
	}
	return hSubKey;
}

static BOOL _Delete (PCTSTR pszPath) {
	HKEY hKey = _Parent (&pszPath);
	if (hKey && (RegDeleteKey (hKey, pszPath) == ERROR_SUCCESS)) {
		LogPrintf (TEXT ("%s\n"), pszPath);
		return TRUE;
	} else {
		return FALSE;
	}
}

static void _Indent (DWORD dwIndent) {
	DWORD dw;
	for (dw = 0; dw < dwIndent; dw++) {
		LogPrintf (TEXT ("  "));
	}
}

static int _DeleteSubKeys (HKEY hKey, DWORD dwIndent) {
	int nDeleted = 0;
	DWORD cSubKeys;
	DWORD cchMaxSubKeyLen;
	if (RegQueryInfoKey (hKey, NULL, NULL, NULL, &cSubKeys, &cchMaxSubKeyLen, NULL, NULL, NULL, NULL, NULL, NULL) != ERROR_SUCCESS) {
		LogDebug (TEXT ("Couldn't query key, error %d"), GetLastError ());
		return 0;
	}
	DWORD dw;
	PTSTR pszName = new TCHAR[cchMaxSubKeyLen + 1];
	if (pszName) {
		for (dw = 0; dw < cSubKeys; dw++) {
			if (RegEnumKey (hKey, dw, pszName, cchMaxSubKeyLen + 1) == ERROR_SUCCESS) {
				HKEY hSubKey;
				if (RegOpenKey (hKey, pszName, &hSubKey) == ERROR_SUCCESS) {
					nDeleted += _DeleteSubKeys (hSubKey, dwIndent + 1);
					RegCloseKey (hSubKey);
				}
				if (RegDeleteKey (hKey, pszName) == ERROR_SUCCESS) {
					_Indent (dwIndent);
					LogPrintf (TEXT ("%s\n"), pszName);
					nDeleted++;
				}
			} else {
				break;
			}
		}
		delete pszName;
	} else {
		LogOutOfMemory ();
	}
	return nDeleted;
}

int CRegistry::Delete (PCTSTR pszPath) {
	HKEY hKey = _Open (pszPath);
	if (!hKey) return 0;
	int nDeleted = _DeleteSubKeys (hKey, 1);
	RegCloseKey (hKey);
	_Delete (pszPath);
	return nDeleted;
}

static int _Enum (HKEY hkey, DWORD dwIndent) {
	int nLines = 0;
	DWORD cSubKeys;
	DWORD cchMaxSubKeyLen;
	DWORD cValues;
	DWORD cchMaxValueNameLen;
	DWORD cbMaxValueLen;
	if (RegQueryInfoKey (hkey, NULL, NULL, NULL, &cSubKeys, &cchMaxSubKeyLen, NULL, &cValues, &cchMaxValueNameLen, &cbMaxValueLen, NULL, NULL) != ERROR_SUCCESS) {
		LogDebug (TEXT ("Couldn't query key, error %d"), GetLastError ());
		return 1;
	}
	DWORD dw;
	PTSTR pszName = new TCHAR[cchMaxSubKeyLen + 1];
	if (pszName) {
		for (dw = 0; dw < cSubKeys; dw++) {
			if (RegEnumKey (hkey, dw, pszName, cchMaxSubKeyLen + 1) == ERROR_SUCCESS) {
				_Indent (dwIndent);
				LogPrintf (TEXT ("[%s]\n"), pszName);
				nLines++;
				HKEY hSubKey;
				if (RegOpenKey (hkey, pszName, &hSubKey) == ERROR_SUCCESS) {
					nLines += _Enum (hSubKey, dwIndent + 1);
					RegCloseKey (hSubKey);
				}
			} else {
				break;
			}
		}
		delete pszName;
	} else {
		LogOutOfMemory ();
		nLines++;
	}
	pszName = new TCHAR[cchMaxValueNameLen + 1];
	if (pszName) {
		PBYTE pbData = new BYTE[cbMaxValueLen];
		if (pbData) {
			for (dw = 0; dw < cValues; dw++) {
				DWORD cchValueName = cchMaxValueNameLen + 1;
				DWORD dwType;
				DWORD cbData = cbMaxValueLen;
				if (RegEnumValue (hkey, dw, pszName, &cchValueName, NULL, &dwType, pbData, &cbData) == ERROR_SUCCESS) {
					_Indent (dwIndent);
					LogPrintf (TEXT (" %s "), pszName);
					switch (dwType) {
					case REG_DWORD :
						LogPrintf (TEXT ("DWORD 0x%X\n"), *(PDWORD)pbData);
						break;
					case REG_EXPAND_SZ :
						LogPrintf (TEXT ("EXPAND_SZ \"%s\"\n"), (PTSTR)pbData);
						break;
					case REG_SZ :
						LogPrintf (TEXT ("SZ \"%s\"\n"), (PTSTR)pbData);
						break;
					default :
						LogPrintf (TEXT ("(type %d)\n"), dwType);
						break;
					}
					nLines++;
				} else {
					break;
				}
			}
			delete pbData;
		} else {
			LogOutOfMemory ();
			nLines++;
		}
		delete pszName;
	} else {
		LogOutOfMemory ();
		nLines++;
	}
	return nLines;
}

int CRegistry::Report (PCTSTR pszPath) {
	HKEY hKey = _Open (pszPath);
	if (!hKey) return 1;
	LogPrintf (TEXT ("%s\n"), pszPath);
	int nLines = 1 + _Enum (hKey, 1);
	RegCloseKey (hKey);
	return nLines;
}

void CRegistry::Delete () {
	int i, nDeleted = 0;
	for (i = 0; i < m_oWatch.Size (); i++) {
		PCTSTR pszPath = m_oWatch.Get (i);
		if (pszPath) {
			nDeleted += Delete (pszPath);
		}
	}
	LogPrintf (TEXT ("%d registry key(s) deleted\n\n"), nDeleted);
}

void CRegistry::Report () {
	int i, nLines = 0;
	for (i = 0; i < m_oWatch.Size (); i++) {
		PCTSTR pszPath = m_oWatch.Get (i);
		if (pszPath) {
			nLines += Report (pszPath);
		}
	}
	if (nLines) {
		LogPrintf (TEXT ("\n"));
	}
}