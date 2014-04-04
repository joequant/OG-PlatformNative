/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "service.h"
#include "environment.h"
#include <param.h>
#include <errorref.h>

#define DEFAULT_WAIT_HINT		30000

static CParamString g_oConfigFile ("config", NULL, TRUE);
static CParamString g_oServiceName ("service", NULL, TRUE);
static CParam *g_apoParams[2] = { &g_oConfigFile, &g_oServiceName };
static CParams g_oParams (sizeof (g_apoParams) / sizeof (*g_apoParams), g_apoParams);

static CRITICAL_SECTION g_cs;
static SERVICE_STATUS_HANDLE g_hStatus;
static CJavaVM * volatile g_poJVM = NULL;
static volatile HANDLE g_hStopThread = NULL;

static DWORD CALLBACK _stopThread (PVOID pReserved) {
	EnterCriticalSection (&g_cs);
	const CJavaVM *poJVM = g_poJVM ? g_poJVM->Attach ("stop signal") : NULL;
	LeaveCriticalSection (&g_cs);
	DWORD dwError = CService::Stop (poJVM);
	if (dwError) {
		ReportErrorReference (dwError);
	}
	CJavaVM::Release (poJVM);
	return 0;
}

static DWORD CALLBACK _killThread (PVOID pReserved) {
	Sleep (DEFAULT_WAIT_HINT);
	HANDLE hStopThread = InterlockedExchangePointer (&g_hStopThread, NULL);
	if (hStopThread) {
		// Main thread is still running (it will have nulled out the stop handle otherwise) - do a hard terminate
		SERVICE_STATUS sta;
		ZeroMemory (&sta, sizeof (sta));
		sta.dwControlsAccepted = 0;
		sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		sta.dwCurrentState = SERVICE_STOPPED;
		sta.dwWaitHint = DEFAULT_WAIT_HINT;
		sta.dwServiceSpecificExitCode = ERROR_TIMEOUT;
		SetServiceStatus (g_hStatus, &sta);
		TerminateProcess (GetCurrentProcess (), 1);
	}
	return 0;
}

static void WINAPI _ServiceHandler (DWORD dwAction) {
	if (dwAction == SERVICE_CONTROL_STOP) {
		SERVICE_STATUS sta;
		ZeroMemory (&sta, sizeof (sta));
		sta.dwControlsAccepted = 0;
		sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
		sta.dwCurrentState = SERVICE_STOP_PENDING;
		sta.dwWaitHint = DEFAULT_WAIT_HINT + (DEFAULT_WAIT_HINT / 2);
		SetServiceStatus (g_hStatus, &sta);
		g_hStopThread = CreateThread (NULL, 0, _stopThread, NULL, 0, NULL);
		if (g_hStopThread) {
			HANDLE hKillThread = CreateThread (NULL, 0, _killThread, NULL, 0, NULL);
			if (hKillThread) {
				CloseHandle (hKillThread);
			}
		} else {
			ReportErrorReference (ERROR_REF_MAIN);
			sta.dwControlsAccepted = SERVICE_CONTROL_STOP;
			sta.dwCurrentState = SERVICE_RUNNING;
			sta.dwWaitHint = DEFAULT_WAIT_HINT;
			SetServiceStatus (g_hStatus, &sta);
		}
	}
}

static void JNICALL _exitHook (JNIEnv *pEnv, jclass cls) {
	SERVICE_STATUS sta;
	ZeroMemory (&sta, sizeof (sta));
	sta.dwControlsAccepted = 0;
	sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
	sta.dwCurrentState = SERVICE_STOPPED;
	sta.dwWaitHint = DEFAULT_WAIT_HINT;
	SetServiceStatus (g_hStatus, &sta);
}

static void WINAPI _ServiceMain (DWORD dwArgs, char **pspzArgs) {
	SERVICE_STATUS sta;
	ZeroMemory (&sta, sizeof (sta));
	CJavaRT *poRuntime = CJavaRT::Init ();
	if (poRuntime) {
		g_poJVM = poRuntime->CreateVM ();
		if (g_poJVM) {
			InitializeCriticalSection (&g_cs);
			g_hStatus = RegisterServiceCtrlHandler (g_oServiceName.GetString (), _ServiceHandler);
			DWORD dwError = CService::RegisterShutdownHook (g_poJVM, _exitHook);
			if (!dwError) {
				sta.dwControlsAccepted = SERVICE_ACCEPT_STOP;
				sta.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
				sta.dwCurrentState = SERVICE_RUNNING;
				sta.dwWaitHint = DEFAULT_WAIT_HINT;
				SetServiceStatus (g_hStatus, &sta);
				DWORD dwError = CService::Run (g_poJVM);
				HANDLE hStopThread = InterlockedExchangePointer (&g_hStopThread, NULL);
				if (hStopThread) {
					// Wait a couple of seconds for the stopping thread to finish
					WaitForSingleObject (hStopThread, DEFAULT_WAIT_HINT / 8);
					CloseHandle (hStopThread);
				}
				sta.dwControlsAccepted = 0;
				sta.dwCurrentState = SERVICE_STOPPED;
				if (dwError) {
					ReportErrorReference (dwError);
					sta.dwWin32ExitCode = ERROR_INTERNAL_ERROR;
				}
				SetServiceStatus (g_hStatus, &sta);
			} else {
				ReportErrorReference (dwError);
				sta.dwWin32ExitCode = ERROR_INTERNAL_ERROR;
				SetServiceStatus (g_hStatus, &sta);
			}
			EnterCriticalSection (&g_cs);
			CJavaVM *poJVM = g_poJVM;
			g_poJVM = NULL;
			LeaveCriticalSection (&g_cs);
			CJavaVM::Release (poJVM);
		} else {
			ReportErrorReference (ERROR_REF_MAIN);
			sta.dwWin32ExitCode = ERROR_INTERNAL_ERROR;
			SetServiceStatus (g_hStatus, &sta);
		}
		delete poRuntime;
	} else {
		ReportErrorReference (ERROR_REF_MAIN);
		sta.dwWin32ExitCode = ERROR_INTERNAL_ERROR;
		SetServiceStatus (g_hStatus, &sta);
	}
}

/// Service executable entry point.
///
/// @return 1 if there is a problem, 0 if all is okay
int main () {
	SERVICE_TABLE_ENTRY ste[2];
	if (!g_oParams.Process (GetCommandLineW ())) {
		ReportErrorReference (ERROR_REF_MAIN);
		return 1;
	}
	if (!CJavaRT::s_oConfig.Read (g_oConfigFile.GetString ())) {
		ReportErrorReference (ERROR_REF_MAIN);
		return 1;
	}
	if (!CService::s_oConfig.Read (g_oConfigFile.GetString ())) {
		ReportErrorReference (ERROR_REF_MAIN);
		return 1;
	}
	if (!EnvironmentInit (g_oConfigFile.GetString ())) {
		ReportErrorReference (ERROR_REF_MAIN);
		return 1;
	}
	ste[0].lpServiceName = (PSTR)g_oServiceName.GetString ();
	ste[0].lpServiceProc = _ServiceMain;
	ste[1].lpServiceName = NULL;
	ste[1].lpServiceProc = NULL;
	StartServiceCtrlDispatcher (ste);
	return 0;
}
