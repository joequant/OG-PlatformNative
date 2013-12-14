/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Log.h"

static void _EnsureFolderExists (PCTSTR pszFilename) {
	PTSTR pszCopy = _tcsdup (pszFilename);
	if (!pszCopy) return;
	size_t cch = _tcslen (pszCopy);
	while ((cch > 0) && (pszCopy[--cch] != '\\'));
	if (cch) {
		pszCopy[cch] = 0;
		_EnsureFolderExists (pszCopy);
		CreateDirectory (pszCopy, NULL);
	}
	free (pszCopy);
}

class CLog {
private:
	FILE *m_hFile;
public:
	CLog () {
		m_hFile = NULL;
	}
	void Close () {
		if (m_hFile) {
			fclose (m_hFile);
			m_hFile = NULL;
		}
	}
	void Open (PCTSTR pszFilename) {
		Close ();
		if (pszFilename) {
			_EnsureFolderExists (pszFilename);
			if (_tfopen_s (&m_hFile, pszFilename, TEXT ("wt"))) {
				m_hFile = NULL;
			}
		}
	}
	void vprintf (PCTSTR pszFormat, va_list args) {
		_vftprintf (m_hFile ? m_hFile : stdout, pszFormat, args);
		if (m_hFile) {
			fflush (m_hFile);
		}
	}
	~CLog () {
		Close ();
	}
};

static CLog g_oLog;

void LogToFile (PCTSTR pszFilename) {
	g_oLog.Open (pszFilename);
}

void LogPrintf (PCTSTR pszFormat, ...) {
	va_list args;
	va_start (args, pszFormat);
	g_oLog.vprintf (pszFormat, args);
}
