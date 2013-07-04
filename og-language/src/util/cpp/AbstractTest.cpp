/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "AbstractTest.h"
#include "Fudge.h"
#include "Error.h"
#include <log4cxx/propertyconfigurator.h>
#include <log4cxx/basicconfigurator.h>

LOGGING (com.opengamma.language.util.AbstractTest);

/// Makes sure Fudge is initialised when the test starts.
static CFudgeInitialiser g_oInitialiseFudge;

#ifndef __cplusplus_cli

/// Maximum number of test classes; this must be large enough to allow all tests to be declared.
#define MAX_TESTS	50

/// Number of declared tests.
static int g_nTests = 0;

/// Number of tests successfully run.
static int g_nSuccessfulTests = 0;

/// Declared test class instances.
static CAbstractTest *g_poTests[MAX_TESTS];

/// Creates a test class instance.
///
/// @param[in] bAutomatic true if the test should normally run, false if it is to run 'on request' only
/// @param[in] pszName name of the test - for logging purposes and selecting ones to run manually 
CAbstractTest::CAbstractTest (bool bAutomatic, const TCHAR *pszName) {
	ASSERT (g_nTests < MAX_TESTS);
	m_pszName = pszName;
	m_bAutomatic = bAutomatic;
	g_poTests[g_nTests++] = this;
}

/// Destroys a test instance.
CAbstractTest::~CAbstractTest () {
}

#ifndef _WIN32

/// Dummy handler to ignore a signal.
///
/// @param[in] signal the signal to ignore
static void _IgnoreSignal (int signal) {
	__unused (signal)
	// No-op
}

/// Handler for atexit to shutdown any spawned processes (e.g. the JVM service runner).
static void exitProc () {
	pid_t grp = getpgid (0);
	if (grp > 1) {
		sigset (SIGHUP, _IgnoreSignal); // but not us
		kill (-grp, SIGHUP);
	}
}

#endif /* ifndef _WIN32 */

/// Program entry point implementation. A test harness executable should call this from
/// its own entry point.
///
/// @param[in] argc the number of command line arguments passed
/// @param[in] argv the command line arguments
void CAbstractTest::Main (int argc, TCHAR **argv) {
	int nTest;
	InitialiseLogs ();
#ifndef _WIN32
	setpgrp ();
	atexit (exitProc);
#endif /* ifndef _WIN32 */
	for (nTest = 0; nTest < g_nTests; nTest++) {
		if (argc > 1) {
			int i;
			bool bRun = false;
			for (i = 1; i < argc; i++) {
				if (!_tcscmp (argv[i], g_poTests[nTest]->m_pszName)) {
					bRun = true;
					break;
				}
			}
			if (!bRun) {
				LOGINFO (TEXT ("Skipping test ") << (nTest + 1) << TEXT (" - ") << g_poTests[nTest]->m_pszName);
				continue;
			}
		} else {
			if (!g_poTests[nTest]->m_bAutomatic) {
				LOGINFO (TEXT ("Skipping test ") << (nTest + 1) << TEXT (" - ") << g_poTests[nTest]->m_pszName);
				continue;
			}
		}
		LOGINFO (TEXT ("Running test ") << (nTest + 1) << TEXT (" - ") << g_poTests[nTest]->m_pszName);
		g_poTests[nTest]->BeforeAll ();
		g_poTests[nTest]->Run ();
		g_poTests[nTest]->AfterAll ();
	}
	LOGINFO (TEXT ("Successfully executed ") << g_nSuccessfulTests << TEXT (" in ") << g_nTests << TEXT (" components"));
	LOGDEBUG (TEXT ("Exiting with error code 0"));
	exit (0);
}

/// Called after each completed test method. A test class may override this to perform any cleanup
/// actions, but must invoke the superclass method.
void CAbstractTest::After () {
	g_nSuccessfulTests++;
}

#endif /* ifndef __cplusplus_cli */

void LoggingInitImpl (const TCHAR *pszLogConfiguration, bool bApplyDefault);

/// Initialises the logging subsystem with a path to the LOG4CXX configuration file taken from the
/// LOG4CXX_CONFIGURATION environment variable. It is safe to call this multiple times - the first
/// is acted upon, later calls are ignored.
void CAbstractTest::InitialiseLogs () {
	static bool bFirst = true;
	if (bFirst) {
		bFirst = false;
	} else {
		return;
	}
#ifdef _WIN32
	TCHAR szConfigurationFile[MAX_PATH];
	const TCHAR *pszConfigurationFile = (GetEnvironmentVariable (TEXT ("LOG4CXX_CONFIGURATION"), szConfigurationFile, MAX_PATH) != 0) ? szConfigurationFile : NULL;
#else /* ifdef _WIN32 */
	const char *pszConfigurationFile = getenv ("LOG4CXX_CONFIGURATION");
#endif /* ifdef _WIN32 */
	LoggingInitImpl (pszConfigurationFile, false);
}

class CTestGroup {
private:

	/// The number of groups to execute
	int m_nGroups;

	/// The array of test groups
	TCHAR *m_apszGroups[4];

	void InitGroups () {
		m_nGroups = 0;
#ifdef _WIN32
		TCHAR szGroups[MAX_PATH];
		if (GetEnvironmentVariable (TEXT ("TEST_GROUPS"), szGroups, MAX_PATH) == 0) {
			LOGINFO (TEXT ("No TEST_GROUPS environment variable set, all tests enabled"));
			return;
		}
		if ((szGroups[0] == '*') && !szGroups[1]) {
			LOGINFO (TEXT ("All test groups enabled"));
			return;
		}
		LOGDEBUG (TEXT ("Got TEST_GROUPS = ") << szGroups);
#define PSZ_GROUPS szGroups
#else /* ifdef _WIN32 */
		const char *pszGroups = getenv ("TEST_GROUPS");
		if (!pszGroups) {
			LOGINFO (TEXT ("No TEST_GROUPS environment variable set, all tests enabled"));
			return;
		}
		if ((pszGroups[0] == '*') && !pszGroups[1]) {
			LOGINFO (TEXT ("All test groups enabled"));
			return;
		}
		char *pszGroupsCopy = strdup (pszGroups);
		if (!pszGroupsCopy) {
			LOGFATAL (TEXT ("Out of memory"));
			return;
		}
#define PSZ_GROUPS pszGroupsCopy
#endif /* ifdef _WIN32 */
		TCHAR *pszContext;
		TCHAR *psz = _tcstok_s (PSZ_GROUPS, TEXT (","), &pszContext);
#undef PSZ_GROUPS
		while (psz) {
			if (m_nGroups > (int)(sizeof (m_apszGroups) / sizeof (TCHAR*))) {
				LOGFATAL (TEXT ("Too many test groups (") << m_nGroups << TEXT (") specified"));
				break;
			}
			psz = _tcsdup (psz);
			if (!psz) {
				LOGFATAL (TEXT ("Out of memory"));
				break;
			}
			LOGINFO (TEXT ("Enabling test ") << psz);
			m_apszGroups[m_nGroups++] = psz;
			psz = _tcstok_s (NULL, TEXT (","), &pszContext);
		}
#ifndef _WIN32
		free (pszGroupsCopy);
#endif /* ifndef _WIN32 */
	}

public:

	CTestGroup () {
		m_nGroups = -1;
	}

	~CTestGroup () {
		int i;
		for (i = 0; i < m_nGroups; i++) {
			free (m_apszGroups[i]);
		}
	}

	bool IsGroup (const TCHAR *pszGroup) {
		if (m_nGroups < 0) InitGroups ();
		if (m_nGroups == 0) return true;
		int i;
		for (i = 0; i < m_nGroups; i++) {
			if (!_tcscmp (pszGroup, m_apszGroups[i])) return true;
		}
		return false;
	}

};

static CTestGroup g_oTestGroup;

/// Tests whether a given test "group" is active or not. By default, all groups are active.
///
/// @return TRUE if the group is active, FALSE otherwise
bool CAbstractTest::IsGroup (const TCHAR *pszGroup) {
	return g_oTestGroup.IsGroup (pszGroup);
}
