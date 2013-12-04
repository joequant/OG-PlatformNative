/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "ErrorFeedback.h"
#include "Public.h"

LOGGING(com.opengamma.language.service.ErrorFeedback);

static TCHAR const **_Split2 (TCHAR **ppszNext, int *pnCount) {
	TCHAR *pszLine = _tcstok_s (NULL, TEXT ("\n"), ppszNext);
	if (pszLine) {
		int nIndex = (*pnCount)++;
		TCHAR const **ppszResult = _Split2 (ppszNext, pnCount);
		if (ppszResult) {
			ppszResult[nIndex] = pszLine;
			return ppszResult;
		} else {
			return NULL;
		}
	} else {
		return (TCHAR const **)malloc (sizeof (TCHAR const *) * *pnCount);
	}
}

static TCHAR const **_Split (TCHAR *psz, int *pnCount) {
	TCHAR *pszNext;
	TCHAR *pszLine = _tcstok_s (psz, TEXT ("\n"), &pszNext);
	if (pszLine) {
		(*pnCount)++;
		TCHAR const **ppszResult = _Split2 (&pszNext, pnCount);
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

#ifdef _WIN32
#define FEEDBACK_EVENT_CATEGORY	0
#define FEEDBACK_EVENT_ID		0
#endif /* ifdef _WIN32 */

static void _Report (TCHAR const **ppszLines, int nLines) {
#ifdef _WIN32
	LOGINFO (TEXT ("Writing failure to start to Windows event log"));
	HANDLE hEventLog = RegisterEventSource (NULL, ServiceDefaultServiceName ());
	if (hEventLog) {
		if (!ReportEvent (hEventLog, EVENTLOG_ERROR_TYPE, FEEDBACK_EVENT_CATEGORY, FEEDBACK_EVENT_ID, NULL, nLines, 0, ppszLines, NULL)) {
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

#ifdef _WIN32
/// Fetches a reason why the service didn't start. The feedback messages are written to the event log by
/// _Report - this fetches the last failure record written, looking back upto 3 minutes.
///
/// @param[out] ppszSummary updated to point into the allocated buffer where the summary text is
/// @param[out] pppszDetail updated to point into the allocated buffer where the detail lines are
/// @param[out] pdwDetail updated to indicate the number of detail lines
/// @param[in] pBuffer the caller allocated buffer to receive the data, never NULL
/// @param[in] cbBuffer the size of the caller allocated buffer
BOOL ServiceGetErrorLog (PCTSTR *ppszSummary, PCTSTR **pppszDetail, DWORD *pdwDetail, PVOID pBuffer, DWORD cbBuffer) {
	BOOL bResult = FALSE;
	DWORD dwError = 0;
	HANDLE hLog = NULL;
	PVOID pLocalBuffer = NULL;
	do {
		hLog = OpenEventLog (NULL, TEXT ("Application"));
		if (!hLog) {
			dwError = GetLastError ();
			LOGWARN (TEXT( "Couldn't open Windows event log, error ") << dwError);
			break;
		}
		DWORD cbBytesAllocated = 0x8000;
localBufferAlloc:
		LOGDEBUG (TEXT ("Allocating ") << cbBytesAllocated << TEXT (" byte buffer"));
		pLocalBuffer = malloc (cbBytesAllocated);
		if (!pLocalBuffer) {
			LOGFATAL (TEXT ("Out of memory"));
			dwError = ERROR_OUTOFMEMORY;
			break;
		}
		DWORD cbBytesRead, cbBytesNeeded;
		DWORD dwTimeLimit = 0;
eventLogRead:
		LOGDEBUG (TEXT ("Reading windows event log"));
		if (!ReadEventLog (hLog, EVENTLOG_SEQUENTIAL_READ | EVENTLOG_BACKWARDS_READ, 0, pLocalBuffer, 0x8000, &cbBytesRead, &cbBytesNeeded)) {
			dwError = GetLastError ();
			if ((dwError == ERROR_INSUFFICIENT_BUFFER) && (cbBytesNeeded > cbBytesAllocated)) {
				LOGDEBUG (TEXT ("Reallocating buffer"));
				free (pLocalBuffer);
				pLocalBuffer = NULL;
				cbBytesAllocated = cbBytesNeeded;
				goto localBufferAlloc;
			}
			LOGWARN (TEXT ("Couldn't read event log, error ") << dwError);
			break;
		}
		PBYTE pLogData = (PBYTE)pLocalBuffer;
#ifdef _UNICODE
		const WCHAR *pszSource = ServiceDefaultServiceName ();
#else /* ifdef _UNICODE */
# error "TODO"
#endif /* ifdef _UNICODE */
		while (cbBytesRead > 0) {
			PEVENTLOGRECORD pLogRecord = (PEVENTLOGRECORD)pLogData;
			LOGDEBUG (L"Source = " << (WCHAR*)(pLogRecord + 1) << L", type=" << pLogRecord->EventType << L", category=" << pLogRecord->EventCategory << L", id=" << pLogRecord->EventID);
			if ((pLogRecord->EventType == EVENTLOG_ERROR_TYPE)
			 && (pLogRecord->EventID == FEEDBACK_EVENT_ID)
			 && (pLogRecord->EventCategory == FEEDBACK_EVENT_CATEGORY)
			 && !wcscmp (pszSource, (WCHAR*)(pLogRecord + 1))) {
				LOGINFO (TEXT ("Feedback event found in Windows event log at ") << pLogRecord->TimeGenerated);
				if (pLogRecord->NumStrings > 0) {
					*pdwDetail = pLogRecord->NumStrings - 1;
					*ppszSummary = (TCHAR*)pBuffer;
					WCHAR *pString = (WCHAR*)((PBYTE)pLogData + pLogRecord->StringOffset);
					size_t cch = wcslen (pString);
					if ((cch + 1) * sizeof (TCHAR) < cbBuffer) {
						LOGDEBUG (TEXT ("Copying ") << ((cch + 1) * sizeof (TCHAR)) << TEXT (" byte summary to buffer (") << cbBuffer << TEXT (" available)"));
#ifdef _UNICODE
						memcpy (pBuffer, pString, (cch + 1) * sizeof (WCHAR));
#else /* ifdef _UNICODE */
# error "TODO"
#endif /* ifdef _UNICODE */
						LOGINFO (TEXT ("Summary: ") << (PCTSTR)pBuffer);
						pString += cch + 1;
						pBuffer = (PBYTE)pBuffer + (cch + 1) * sizeof (TCHAR);
						cbBuffer -= (DWORD)((cch + 1) * sizeof (TCHAR));
						if (*pdwDetail) {
							*pppszDetail = (PCTSTR*)pBuffer;
							if (*pdwDetail * sizeof (TCHAR*) <= cbBuffer) {
								LOGDEBUG (TEXT ("Copying ") << (*pdwDetail * sizeof (PCTSTR)) << TEXT (" byte index to buffer (") << cbBuffer << TEXT (" available)"));
								pBuffer = (PBYTE)pBuffer + *pdwDetail * sizeof (PCTSTR);
								cbBuffer -= *pdwDetail * sizeof (PCTSTR);
								bResult = TRUE;
								DWORD i;
								for (i = 0; i < *pdwDetail; i++) {
									cch = wcslen (pString);
									if ((cch + 1) * sizeof (TCHAR) <= cbBuffer) {
										LOGDEBUG (TEXT ("Copying ") << ((cch + 1) * sizeof (TCHAR)) << TEXT (" byte detail to buffer (") << cbBuffer << TEXT (" available)"));
#ifdef _UNICODE
										memcpy (pBuffer, pString, (cch + 1) * sizeof (WCHAR));
#else /* ifdef _UNICODE */
# error "TODO"
#endif /* ifdef _UNICODE */
										LOGINFO (TEXT ("Detail:  ") << (PCTSTR)pBuffer);
										(*pppszDetail)[i] = (PCTSTR)pBuffer;
										pBuffer = (PBYTE)pBuffer + (cch + 1) * sizeof (TCHAR);
										cbBuffer -= (DWORD)((cch + 1) * sizeof (TCHAR));
									} else {
										(*pppszDetail)[i] = TEXT ("");
										LOGWARN (TEXT ("Buffer too small"));
									}
									pString += cch + 1;
								}
							} else {
								LOGWARN (TEXT ("Buffer too small"));
							}
						}
					} else {
						LOGWARN (TEXT ("Buffer too small"));
					}
				}
				break;
			}
			DWORD cbLogRecord = pLogRecord->Length;
			if (dwTimeLimit) {
				if (pLogRecord->TimeGenerated < dwTimeLimit) {
					LOGINFO (TEXT ("No feedback event found in time window"));
					break;
				}
			} else {
				dwTimeLimit = pLogRecord->TimeGenerated - 180;
			}
			if (cbLogRecord >= cbBytesRead) {
				goto eventLogRead;
			} else {
				cbBytesRead -= cbLogRecord;
				pLogData += cbLogRecord;
			}
		}
	} while (FALSE);
	if (hLog) CloseEventLog (hLog);
	if (pLocalBuffer) free (pLocalBuffer);
	if (bResult) {
		return TRUE;
	} else {
		SetLastError (dwError);
		return FALSE;
	}
}
#endif /* ifdef _WIN32 */

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
		TCHAR const **ppszLines = _Split (psz, &nLines);
		if (ppszLines) {
			_Report (ppszLines, nLines);
			free(ppszLines);
		}
		free (psz);
	} else {
		LOGFATAL (TEXT ("Out of memory"));
	}
}
