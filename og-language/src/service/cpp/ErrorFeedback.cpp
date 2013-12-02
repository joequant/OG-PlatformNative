/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "ErrorFeedback.h"
#include "Public.h"

LOGGING(com.opengamma.language.service.ErrorFeedback);

static PCTSTR *_Split2 (TCHAR **ppszNext, int *pnCount) {
	TCHAR *pszLine = _tcstok_s (NULL, TEXT ("\n"), ppszNext);
	if (pszLine) {
		int nIndex = (*pnCount)++;
		PCTSTR *ppszResult = _Split2 (ppszNext, pnCount);
		if (ppszResult) {
			ppszResult[nIndex] = pszLine;
			return ppszResult;
		} else {
			return NULL;
		}
	} else {
		return (PCTSTR*)malloc (sizeof (PCTSTR) * *pnCount);
	}
}

static PCTSTR *_Split (TCHAR *psz, int *pnCount) {
	TCHAR *pszNext;
	TCHAR *pszLine = _tcstok_s (psz, TEXT ("\n"), &pszNext);
	if (pszLine) {
		(*pnCount)++;
		PCTSTR *ppszResult = _Split2 (&pszNext, pnCount);
		if (ppszResult) {
			ppszResult[0] = pszLine;
			return ppszResult;
		} else {
			return NULL;
		}
	} else {
		*pnCount = 0;
		return NULL;
	}
}

static void _Report (PCTSTR *ppszLines, int nLines) {
#ifdef _WIN32
	LOGINFO (TEXT ("Writing failure to start to Windows event log"));
	HANDLE hEventLog = RegisterEventSource (NULL, ServiceDefaultServiceName ());
	if (hEventLog) {
		if (!ReportEvent (hEventLog, EVENTLOG_ERROR_TYPE, 0, 0, NULL, nLines, 0, ppszLines, NULL)) {
			LOGERROR (TEXT ("Couldn't write to Windows event log, error ") << GetLastError ());
		}
		DeregisterEventSource (hEventLog);
	} else {
		LOGERROR (TEXT ("Couldn't open Windows event log, error ") << GetLastError ())
	}
#else /* ifdef _WIN32 */
	int n;
	for (n = 0; n < nLines; n++) {
		LOGWARN (TEXT (">> ") << ppszLines[n]);
	}
#endif /* ifdef _WIN32 */
}

/// Creates a new feedback object.
CErrorFeedback::CErrorFeedback () {
}

/// Destroys the feedback object.
CErrorFeedback::~CErrorFeedback () {
}

/// Writes a feedback entry to the mechanism.
///
/// @param[in] pszMessage the string to write
void CErrorFeedback::Write (const TCHAR *pszMessage) {
	LOGERROR (TEXT ("A major error occurred"));
	TCHAR *psz = _tcsdup (pszMessage);
	if (psz) {
		int nLines = 0;
		PCTSTR *ppszLines = _Split (psz, &nLines);
		if (ppszLines) {
			_Report (ppszLines, nLines);
			free(ppszLines);
		}
		free (psz);
	} else {
		LOGFATAL (TEXT ("Out of memory"));
	}
}
