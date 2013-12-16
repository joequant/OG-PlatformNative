/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "services.h"
#include "registry.h"
#include "Log.h"

SC_HANDLE CServices::GetServiceManager () {
	if (!m_hSCM) {
		// TODO: Want read/write probably
		m_hSCM = OpenSCManager (NULL, NULL, GENERIC_READ);
		if (!m_hSCM) {
			LogPrintf (TEXT ("Can't open service control manager, error %d\n"), GetLastError ());
		}
	}
	return m_hSCM;
}

void CServices::Stop (SC_HANDLE hService, PCTSTR pszShortName) {
	SERVICE_STATUS sta;
	if (ControlService (hService, SERVICE_CONTROL_STOP, &sta)) {
		LogPrintf (TEXT ("STOP signal sent to %s\n"), pszShortName);
	}
}

void CServices::Kill (SC_HANDLE hService) {
	SERVICE_STATUS_PROCESS staProcess;
	DWORD dwBytesNeeded;
	if (QueryServiceStatusEx (hService, SC_STATUS_PROCESS_INFO, (LPBYTE)&staProcess, sizeof (staProcess), &dwBytesNeeded)) {
		if (staProcess.dwProcessId) {
			HANDLE hProcess = OpenProcess (PROCESS_TERMINATE, FALSE, staProcess.dwProcessId);
			if (hProcess) {
				if (TerminateProcess (hProcess, 0)) {
					LogPrintf (TEXT ("TERMINATE signal sent to %d\n"), staProcess.dwProcessId);
				} else {
					LogDebug (TEXT ("Couldn't terminate process %d"), GetLastError ());
				}
				CloseHandle (hProcess);
			} else {
				LogDebug (TEXT ("Couldn't open process %d, error %d"), staProcess.dwProcessId, GetLastError ());
			}
		}
	} else {
		LogDebug (TEXT ("Couldn't query service status, error %d"), GetLastError ());
	}
}

void CServices::DeleteNicely (SC_HANDLE hService, PCTSTR pszShortName) {
	if (DeleteService (hService)) {
		LogPrintf (TEXT ("%s marked for deletion\n"), pszShortName);
	}
}

void CServices::DeleteForcefully (PCTSTR pszShortName) {
	size_t cch = _tcslen (pszShortName) + 64;
	PTSTR psz = new TCHAR[cch];
	if (!psz) {
		LogOutOfMemory ();
		return;
	}
	StringCchPrintf (psz, cch, TEXT ("%s%s"), TEXT ("HKLM\\SYSTEM\\CurrentControlSet\\services\\"), pszShortName);
	if (CRegistry::Delete (psz)) {
		LogPrintf (TEXT ("Forceful delete of registry keys for %s\n"), pszShortName);
	}
	delete psz;
}

BOOL CServices::StopKillAndDelete (PCTSTR pszShortName) {
	SC_HANDLE hService = OpenService (GetServiceManager (), pszShortName, SERVICE_STOP | GENERIC_READ | DELETE);
	if (hService) {
		Stop (hService, pszShortName);
		Kill (hService);
		DeleteNicely (hService, pszShortName);
		CloseServiceHandle (hService);
		DeleteForcefully (pszShortName);
		return TRUE;
	} else {
		DWORD dwError = GetLastError ();
		if (dwError = ERROR_NOT_FOUND) {
			LogPrintf (TEXT ("Service %s not installed\n"), pszShortName);
		} else {
			LogDebug (TEXT ("Couldn't open %s, error %d"), pszShortName, dwError);
		}
		return FALSE;
	}
}

void CServices::DetailedReport (LPENUM_SERVICE_STATUS_PROCESS lpService) {
	LogPrintf (TEXT ("\ts.DisplayName = %s\n"), lpService->lpDisplayName);
	LogPrintf (TEXT ("\ts.ServiceName = %s\n"), lpService->lpServiceName);
	LogPrintf (TEXT ("\ts.CheckPoint = 0x%X\n"), lpService->ServiceStatusProcess.dwCheckPoint);
	LogPrintf (TEXT ("\ts.ControlsAccepted = 0x%X\n"), lpService->ServiceStatusProcess.dwControlsAccepted);
	LogPrintf (TEXT ("\ts.CurrentState = 0x%X\n"), lpService->ServiceStatusProcess.dwCurrentState);
	LogPrintf (TEXT ("\ts.ProcessId = 0x%X\n"), lpService->ServiceStatusProcess.dwProcessId);
	LogPrintf (TEXT ("\ts.ServiceFlags = 0x%X\n"), lpService->ServiceStatusProcess.dwServiceFlags);
	LogPrintf (TEXT ("\ts.ServiceSpecificExitCode = 0x%X\n"), lpService->ServiceStatusProcess.dwServiceSpecificExitCode);
	LogPrintf (TEXT ("\ts.ServiceType = 0x%X\n"), lpService->ServiceStatusProcess.dwServiceType);
	LogPrintf (TEXT ("\ts.WaitHint = 0x%X\n"), lpService->ServiceStatusProcess.dwWaitHint);
	LogPrintf (TEXT ("\ts.Win32ExitCode = 0x%X\n"), lpService->ServiceStatusProcess.dwWin32ExitCode);
	SC_HANDLE hService = OpenService (GetServiceManager (), lpService->lpServiceName, GENERIC_READ);
	if (hService) {
		DWORD cbBuffer = 8192;
		LPQUERY_SERVICE_CONFIG lpConfig = (LPQUERY_SERVICE_CONFIG)new BYTE[cbBuffer];
		if (lpConfig) {
			DWORD cbBytesNeeded;
			if (QueryServiceConfig (hService, lpConfig, cbBuffer, &cbBytesNeeded)) {
				LogPrintf (TEXT ("\tc.ErrorControl = 0x%X\n"), lpConfig->dwErrorControl);
				LogPrintf (TEXT ("\tc.ServiceType = 0x%X\n"), lpConfig->dwServiceType);
				LogPrintf (TEXT ("\tc.StartType = 0x%X\n"), lpConfig->dwStartType);
				LogPrintf (TEXT ("\tc.TagId = 0x%X\n"), lpConfig->dwTagId);
				LogPrintf (TEXT ("\tc.BinaryPathName = %s\n"), lpConfig->lpBinaryPathName);
				LogPrintf (TEXT ("\tc.Dependencies = %s\n"), lpConfig->lpDependencies);
				LogPrintf (TEXT ("\tc.DisplayName = %s\n"), lpConfig->lpDisplayName);
				LogPrintf (TEXT ("\tc.LoadOrderGroup = %s\n"), lpConfig->lpLoadOrderGroup);
				LogPrintf (TEXT ("\tc.ServiceStartName = %s\n"), lpConfig->lpServiceStartName);
			} else {
				LogPrintf (TEXT ("\tCouldn't query configuration, error %d\n"), GetLastError ());
			}
		} else {
			LogOutOfMemory ();
		}
		CloseServiceHandle (hService);
	} else {
		LogPrintf (TEXT ("\tCouldn't open service, error %d\n"), GetLastError ());
	}
}

void CServices::Check (PCTSTR pszShortName) {
	SC_HANDLE hService = OpenService (GetServiceManager (), pszShortName, GENERIC_READ);
	if (hService) {
		LogPrintf (TEXT ("Service %s exists after clean-up\n"), pszShortName);
		DWORD cbBuffer = 8192;
		LPQUERY_SERVICE_CONFIG lpConfig = (LPQUERY_SERVICE_CONFIG)new BYTE[cbBuffer];
		if (lpConfig) {
			DWORD cbBytesNeeded;
			if (QueryServiceConfig (hService, lpConfig, cbBuffer, &cbBytesNeeded)) {
				LogPrintf (TEXT ("Service %s is configured after clean-up\n"), pszShortName);
			} else {
				DWORD dwError = GetLastError ();
				LogPrintf (TEXT ("Error %d querying service configuration\n"), dwError);
				if (dwError == ERROR_FILE_NOT_FOUND) {
					PTSTR pszMessage = new TCHAR[512];
					if (pszMessage) {
						StringCchPrintf (pszMessage, 512, TEXT ("The %s service cannot be removed prior to re-installation. There are either programs using it which must be closed, or a reboot is needed before this installation can proceed."), pszShortName);
						LogFatalProblem (pszMessage);
						delete pszMessage;
					} else {
						LogOutOfMemory ();
					}
				}
			}
		} else {
			LogOutOfMemory ();
		}
		CloseServiceHandle (hService);
	}
}

CServices::CServices () {
	m_hSCM = NULL;
}

CServices::~CServices () {
	if (m_hSCM) CloseServiceHandle (m_hSCM);
}

void CServices::StopKillAndDelete () {
	int i = 0, nCount = 0;
	for (i = 0; i < m_oWatch.Size (); i++) {
		if (StopKillAndDelete (m_oWatch.Get (i))) {
			nCount++;
		}
	}
	LogPrintf (TEXT ("%d service(s) stopped, killed and/or deleted\n\n"), nCount);
}

void CServices::Report () {
	int nCount = 0;
	LPBYTE lpServices;
	DWORD dwServicesReturned;
	DWORD dwResumeHandle = 0;
	DWORD cbServices = 1024;
	DWORD cbBytesNeeded;
	BOOL bMoreData;
retry:
	lpServices = new BYTE[cbServices];
	if (!lpServices) {
		LogOutOfMemory ();
		return;
	}
	if (EnumServicesStatusEx (GetServiceManager (), SC_ENUM_PROCESS_INFO, SERVICE_WIN32, SERVICE_STATE_ALL, lpServices, cbServices, &cbBytesNeeded, &dwServicesReturned, &dwResumeHandle, NULL)) {
		bMoreData = FALSE;
	} else {
		DWORD dwError = GetLastError ();
		if (dwError != ERROR_MORE_DATA) {
			LogPrintf (TEXT ("Couldn't enumerate services, error %d\n"), dwError);
			delete lpServices;
			return;
		}
		cbServices = cbBytesNeeded;
		bMoreData = TRUE;
	}
	LPENUM_SERVICE_STATUS_PROCESS lpService = (LPENUM_SERVICE_STATUS_PROCESS)lpServices;
	while (dwServicesReturned > 0) {
		LogPrintf (
			TEXT ("%d\t%u/%u\t%s\t%s\n"),
			lpService->ServiceStatusProcess.dwProcessId,
			lpService->ServiceStatusProcess.dwCurrentState,
			lpService->ServiceStatusProcess.dwControlsAccepted,
			lpService->lpServiceName,
			lpService->lpDisplayName);
		if (m_oWatch.Contains (lpService->lpServiceName)) {
			DetailedReport (lpService);
		}
		lpService++;
		dwServicesReturned--;
		nCount++;
	}
	delete lpServices;
	if (bMoreData) {
		goto retry;
	}
	LogPrintf (TEXT ("%d services found\n\n"), nCount);
}

void CServices::Check () {
	int i = 0;
	for (i = 0; i < m_oWatch.Size (); i++) {
		Check (m_oWatch.Get (i));
	}
}