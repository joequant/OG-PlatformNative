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
	PTSTR m_pszFatal;
public:
	CLog () {
		m_hFile = NULL;
		m_pszFatal = NULL;
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
		delete m_pszFatal;
	}
	void SetFatal (PCTSTR pszMessage) {
		PCTSTR pszSeriousness;
		if (m_pszFatal) {
			pszSeriousness = TEXT ("Secondary");
		} else {
			pszSeriousness = TEXT ("Primary");
			m_pszFatal = _tcsdup (pszMessage);
		}
		LogPrintf (TEXT ("%s fatal problem reported: %s\n"), pszSeriousness, pszMessage);
	}
	PCTSTR GetFatal () {
		return m_pszFatal;
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

void LogFatalProblem (PCTSTR pszMessage) {
	g_oLog.SetFatal (pszMessage);
}

PCTSTR LogGetFatal () {
	return g_oLog.GetFatal ();
}