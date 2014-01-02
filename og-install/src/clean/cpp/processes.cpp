/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "processes.h"
#include "Log.h"

BOOL CProcesses::Snapshot () {
	if (m_hSnapshot) return TRUE;
	m_hSnapshot = CreateToolhelp32Snapshot (TH32CS_SNAPPROCESS, 0);
	if (m_hSnapshot == INVALID_HANDLE_VALUE) {
		m_hSnapshot = NULL;
		LogDebug (TEXT ("Couldn't snapshot processes, error %d"), GetLastError ());
		return FALSE;
	}
	return TRUE;
}

CProcesses::CProcesses () {
	m_hSnapshot = NULL;
}

CProcesses::~CProcesses () {
	if (m_hSnapshot) CloseHandle (m_hSnapshot);
}

void CProcesses::Report () {
	if (!Snapshot ()) return;
	PROCESSENTRY32 pe32;
	ZeroMemory (&pe32, sizeof (pe32));
	pe32.dwSize = sizeof (pe32);
	if (!Process32First (m_hSnapshot, &pe32)) {
		LogDebug (TEXT ("Couldn't open system process list, error %d"), GetLastError ());
		return;
	}
	int nCount = 0;
	do {
		LogPrintf (TEXT ("%d\t%s (%d threads)\n"), pe32.th32ProcessID, pe32.szExeFile, pe32.cntThreads);
		nCount++;
	} while (Process32Next (m_hSnapshot, &pe32));
	LogPrintf (TEXT ("Found %d active processes\n\n"), nCount);
}