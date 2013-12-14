/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "folders.h"
#include "Log.h"

int CFolders::DeleteContents (PCTSTR pszPath) {
	WIN32_FIND_DATA wfd;
	size_t cchPath = _tcslen (pszPath);
	PTSTR pszSearch = new TCHAR[cchPath + 5];
	if (!pszSearch) {
		LogOutOfMemory ();
		return 0;
	}
	memcpy (pszSearch, pszPath, cchPath * sizeof (TCHAR));
	memcpy (pszSearch + cchPath, TEXT ("\\*.*"), 5 * sizeof (TCHAR));
	HANDLE hFind = FindFirstFile (pszSearch, &wfd);
	delete pszSearch;
	if (hFind == INVALID_HANDLE_VALUE) {
		DWORD dwError = GetLastError ();
		if (dwError != ERROR_PATH_NOT_FOUND) {
			LogDebug (TEXT ("Couldn't open %s, error %d"), pszPath, dwError);
		}
		return 0;
	}
	int nDeleted = 0;
	do {
		if (wfd.cFileName[0] == '.') {
			if (!wfd.cFileName[1] || ((wfd.cFileName[1] == '.') && !wfd.cFileName[2])) {
				continue;
			}
		}
		size_t cchFileName = _tcslen (wfd.cFileName);
		PTSTR pszNewPath = new TCHAR[cchPath + cchFileName + 2];
		if (pszNewPath) {
			memcpy (pszNewPath, pszPath, cchPath * sizeof (TCHAR));
			pszNewPath[cchPath] = '\\';
			memcpy (pszNewPath + cchPath + 1, wfd.cFileName, (cchFileName + 1) * sizeof (TCHAR));
			if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
				nDeleted += Delete (pszNewPath);
			} else {
				DeleteFile (pszNewPath);
			}
			delete pszNewPath;
		} else {
			LogOutOfMemory ();
		}
	} while (FindNextFile (hFind, &wfd));
	FindClose (hFind);
	return nDeleted;
}

int CFolders::Delete (PCTSTR pszPath) {
	int nDeleted = DeleteContents (pszPath);
	if (RemoveDirectory (pszPath)) {
		LogPrintf (TEXT ("%s\n"), pszPath);
		nDeleted++;
	}
	return nDeleted;
}

static void _Indent (DWORD dwIndent) {
	DWORD dw;
	for (dw = 0; dw < dwIndent; dw++) {
		LogPrintf (TEXT ("  "));
	}
}

int CFolders::Report (PCTSTR pszPath, DWORD dwIndent) {
	WIN32_FIND_DATA wfd;
	size_t cchPath = _tcslen (pszPath);
	PTSTR pszSearch = new TCHAR[cchPath + 5];
	if (!pszSearch) {
		LogOutOfMemory ();
		return 1;
	}
	memcpy (pszSearch, pszPath, cchPath * sizeof (TCHAR));
	memcpy (pszSearch + cchPath, TEXT ("\\*.*"), 5 * sizeof (TCHAR));
	HANDLE hFind = FindFirstFile (pszSearch, &wfd);
	delete pszSearch;
	if (hFind == INVALID_HANDLE_VALUE) {
		DWORD dwError = GetLastError ();
		if (dwError == ERROR_PATH_NOT_FOUND) {
			LogPrintf (TEXT ("%s does not exist\n"), pszPath);
		} else {
			LogDebug (TEXT ("Couldn't open %s, error %d"), pszPath, dwError);
		}
		return 1;
	}
	int nLines = 0;
	if (dwIndent == 0) {
		LogPrintf (TEXT ("%s\n"), pszPath);
		dwIndent++;
		nLines++;
	}
	int nFiles = 0;
	do {
		if (wfd.cFileName[0] == '.') {
			if (!wfd.cFileName[1] || ((wfd.cFileName[1] == '.') && !wfd.cFileName[2])) {
				continue;
			}
		}
		if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
			_Indent (dwIndent);
			LogPrintf (TEXT ("[%s]\n"), wfd.cFileName);
			nLines++;
			size_t cchFileName = _tcslen (wfd.cFileName);
			PTSTR pszNewPath = new TCHAR[cchPath + cchFileName + 2];
			if (pszNewPath) {
				memcpy (pszNewPath, pszPath, cchPath * sizeof (TCHAR));
				pszNewPath[cchPath] = '\\';
				memcpy (pszNewPath + cchPath + 1, wfd.cFileName, (cchFileName + 1) * sizeof (TCHAR));
				nLines += Report (pszNewPath, dwIndent + 1);
				delete pszNewPath;
			} else {
				LogOutOfMemory ();
			}
		} else {
			nFiles++;
			if (nFiles <= 5) {
				_Indent (dwIndent);
				LogPrintf (TEXT (" %s\n"), wfd.cFileName);
				nLines++;
			}
		}
	} while (FindNextFile (hFind, &wfd));
	FindClose (hFind);
	if (nFiles >= 6) {
		_Indent (dwIndent);
		LogPrintf (TEXT ("+%d other file(s)\n"), nFiles - 5);
		nLines++;
	}
	return nLines;
}

void CFolders::Delete () {
	int i, nDeleted = 0;
	for (i = 0; i < m_oWatch.Size (); i++) {
		PCTSTR pszPath = m_oWatch.Get (i);
		if (pszPath) {
			nDeleted += Delete (pszPath);
		}
	}
	LogPrintf (TEXT ("%d folder(s) deleted\n\n"), nDeleted);
}

void CFolders::Report () {
	int i, nLines = 0;
	for (i = 0; i < m_oWatch.Size (); i++) {
		PCTSTR pszPath = m_oWatch.Get (i);
		if (pszPath) {
			nLines += Report (pszPath, 0);
		}
	}
	if (nLines) {
		LogPrintf (TEXT ("\n"));
	}
}