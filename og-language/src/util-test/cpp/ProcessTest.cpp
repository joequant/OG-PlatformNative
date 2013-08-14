/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include <util/cpp/Process.h>

LOGGING (com.opengamma.language.util.ProcessTest);

static void GetCurrentModule () {
	TCHAR szBuffer[MAX_PATH];
	ASSERT (CProcess::GetCurrentModule (szBuffer, MAX_PATH));
	LOGINFO (TEXT ("Current process = ") << szBuffer);
}

static void MyProcess () {
	TCHAR szBuffer[MAX_PATH];
#ifdef _WIN32
	// This should give the MSTEST.EXE test harness
	ASSERT (GetModuleFileName (NULL, szBuffer, MAX_PATH));
#else /* ifdef _WIN32 */
	// This should give the UtilTest process
	ASSERT (readlink ("/proc/self/exe", szBuffer, cchBuffer) > 0);
#endif /* ifdef _WIN32 */
	LOGDEBUG (TEXT ("Searching for '") << szBuffer << TEXT ("'"));
	CProcess *poProcess = CProcess::FindByName (szBuffer);
	ASSERT (poProcess);
	LOGINFO (TEXT ("Found ") << szBuffer << TEXT (" - PID = ") << poProcess->GetProcessId ());
	ASSERT (poProcess->GetProcessId ());
	ASSERT (poProcess->IsAlive ());
	ASSERT (!poProcess->Wait (100));
	delete poProcess;
}

static void NonExistantProcess () {
#ifdef _WIN32
	ASSERT (!CProcess::FindByName (TEXT ("C:\\This Path\\Does not\\Exist\\Or This Test\\Will Fail\\Foo.exe")));
#else
	ASSERT (!CProcess::FindByName (TEXT ("/this_path/does_not/exist/or_this_test/will_fail/foo")));
#endif
}

/// Test the functions and objects in Util/Process.cpp
BEGIN_TESTS (ProcessTest)
	UNIT_TEST (GetCurrentModule)
	UNIT_TEST (MyProcess)
	UNIT_TEST (NonExistantProcess)
	// TODO: Integration tests that launch another process and try to terminate/synchronise etc
END_TESTS
