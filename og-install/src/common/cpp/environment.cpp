/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include "environment.h"
#include "config.h"

#define PARENT_OF_CONFIG_FILE_DIR	"dir(%config%)\\.."

static CConfigString g_oWorkingFolder ("cwd", PARENT_OF_CONFIG_FILE_DIR);
static CConfigString g_oTempFolder ("temp", NULL);
static CConfigEntry *g_apoEnvironmentSection[2] = { &g_oWorkingFolder, &g_oTempFolder };
static CConfigSection g_oEnvironmentSection ("Environment", sizeof (g_apoEnvironmentSection) / sizeof (*g_apoEnvironmentSection), g_apoEnvironmentSection);
static CConfigSection *g_apoConfig[1] = { &g_oEnvironmentSection };
static CConfig g_oConfigContent (sizeof (g_apoConfig) / sizeof (*g_apoConfig), g_apoConfig);

static void _setWorkingFolder (const char *pszConfigFile) {
	const char *pszPath = g_oWorkingFolder.GetValue ();
	if (strcmp (pszPath, PARENT_OF_CONFIG_FILE_DIR)) {
		SetCurrentDirectory (pszPath);
	} else {
		pszPath = pszConfigFile;
		size_t i;
		int n;
		if (*pszPath == '\"') pszPath++;
		i = strlen (pszPath);
		n = 2;
		while (--i > 0) {
			if (pszPath[i] == '\\') {
				if (!--n) {
					char *pszWorkingFolder = (char*)malloc (i + 1);
					if (pszWorkingFolder) {
						memcpy (pszWorkingFolder, pszPath, i);
						pszWorkingFolder[i] = 0;
						SetCurrentDirectory (pszWorkingFolder);
						free (pszWorkingFolder);
					}
					break;
				}
			}
		}
	}
}

static void _setTemporaryFolder () {
	const char *pszPath = g_oTempFolder.GetValue ();
	if (pszPath) {
		SetEnvironmentVariable ("TMP", pszPath);
		SetEnvironmentVariable ("TEMP", pszPath);
		// Note: The JVM seems to pick these either (or both) of these up and set java.io.tmpdir accordingly
	}
}

/// Initialises the environment. This can include setting the working directory and
/// temporary folder environment variables.
///
/// @param[in] pszConfig path to the config file
/// @return TRUE if the environment was initialized, FALSE if there was a problem
BOOL EnvironmentInit (const char *pszConfig) {
	if (!g_oConfigContent.Read (pszConfig)) {
		return FALSE;
	}
	_setWorkingFolder (pszConfig);
	_setTemporaryFolder ();
	return TRUE;
}