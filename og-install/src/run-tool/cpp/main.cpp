/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include <Windows.h>
#include <strsafe.h>
#include "resource.h"
#include <jvm.h>
#include <config.h>
#include <errorref.h>
#include "environment.h"

static BOOL _ConnectToConsole (int *pargc, char **argv) {
	if (*pargc < 2) return TRUE;
	if (strncmp (argv[1], "-console", 8)) return TRUE;
	DWORD dwParent = atoi (argv[1] + 8);
	(*pargc)--;
	memmove (argv + 1, argv + 2, sizeof (char*) * *pargc);
	FreeConsole ();
	if (!AttachConsole (dwParent)) {
		ReportErrorReference (ERROR_REF_MAIN);
		return FALSE;
	}
	return TRUE;
}

static BOOL _RegistryElevate (HKEY hkey, BOOL *pbResult) {
	DWORD dwType, dwValue, dwSize = sizeof (dwValue);
	if (RegGetValue (hkey, "SOFTWARE\\OpenGammaLtd\\RunTool", "elevate", RRF_RT_REG_DWORD, &dwType, &dwValue, &dwSize) == ERROR_SUCCESS) {
		*pbResult = (dwValue != 0);
		return TRUE;
	} else {
		return FALSE;
	}
}

static BOOL _ElevateSelf (int *pargc, char **argv) {
	if (*pargc < 2) return FALSE;
	if (!strcmp (argv[1], "-elevate")) {
		(*pargc)--;
		memmove (argv + 1, argv + 2, sizeof (char*) * *pargc);
		return TRUE;
	} else if (!strcmp (argv[1], "-no-elevate")) {
		(*pargc)--;
		memmove (argv + 1, argv + 2, sizeof (char*) * *pargc);
		return FALSE;
	}
	BOOL bResult;
	if (_RegistryElevate (HKEY_CURRENT_USER, &bResult) || _RegistryElevate (HKEY_LOCAL_MACHINE, &bResult)) {
		return bResult;
	}
	return FALSE;
}

static int _RunElevated (int argc, char **argv) {
	SHELLEXECUTEINFO sei;
	ZeroMemory (&sei, sizeof (sei));
	sei.cbSize = sizeof (sei);
	sei.fMask = SEE_MASK_NOCLOSEPROCESS | SEE_MASK_NO_CONSOLE;
	sei.lpVerb = "runas";
	char szExecutable[MAX_PATH];
	if (GetModuleFileName (NULL, szExecutable, MAX_PATH) == 0) {
		ReportErrorReference (ERROR_REF_MAIN);
		return ERROR_INTERNAL_ERROR;
	}
	sei.lpFile = szExecutable;
	size_t cchParameters = (argc * 3) + 32;
	int i;
	for (i = 1; i < argc; i++) {
		cchParameters += strlen (argv[i]);
	}
	char *pszParameters = new char[cchParameters];
	if (!pszParameters) {
		ReportErrorReference (ERROR_REF_MAIN);
		return ERROR_INTERNAL_ERROR;
	}
	StringCchPrintf (pszParameters, cchParameters, "%s%d%s", "-console", GetProcessId (GetCurrentProcess ()), " -no-elevate");
	for (i = 1; i < argc; i++) {
		if (i > 1) {
			StringCchCat (pszParameters, cchParameters, "\" \"");
		} else {
			StringCchCat (pszParameters, cchParameters, " \"");
		}
		StringCchCat (pszParameters, cchParameters, argv[i]);
	}
	StringCchCat (pszParameters, cchParameters, "\"");
	sei.lpParameters = pszParameters;
	if (!ShellExecuteEx (&sei)) {
		delete pszParameters;
		ReportErrorReference (ERROR_REF_MAIN);
		return ERROR_INTERNAL_ERROR;
	}
	delete pszParameters;
	WaitForSingleObject (sei.hProcess, INFINITE);
	DWORD dwExitCode;
	if (!GetExitCodeProcess (sei.hProcess, &dwExitCode)) {
		ReportErrorReference (ERROR_REF_MAIN);
		dwExitCode = ERROR_INTERNAL_ERROR;
	}
	CloseHandle (sei.hProcess);
	return (int)dwExitCode;
}

/// Sets up the class path configuration as per the old-style (broken) file system layout.
/// This is not used if RunTool.exe.defaults is present.
class CLegacyRunToolClasspath : public CConfigSourceSection {
private:
	char m_szConfig[MAX_PATH];
	char m_szProject[MAX_PATH];
	char m_szLib[MAX_PATH];
	char *m_apsz[3];
	int m_nCount;
	size_t Path (PSTR pszValue, PSTR pszBuffer, size_t cbBuffer) {
		StringCbCopy (pszBuffer, cbBuffer, pszValue);
		return strlen (pszValue);
	}
public:
	CLegacyRunToolClasspath (int *pargc, char **argv) {
		char szBaseDir[MAX_PATH];
		m_szConfig[0] = 0;
		m_szProject[0] = 0;
		m_szLib[0] = 0;
		m_nCount = 0;
		if (GetModuleFileName (NULL, szBaseDir, MAX_PATH) > 0) {
			size_t cch = strlen (szBaseDir);
			int nSlashes = 2;
			while (cch > 0) {
				if (szBaseDir[cch] == '\\') {
					nSlashes--;
					if (!nSlashes) break;
				}
				cch--;
			}
			if (cch > 0) {
				szBaseDir[cch] = 0;
				// Set the working directory
				SetCurrentDirectory (szBaseDir);
				// Configuration
				StringCbPrintf (m_szConfig, sizeof (m_szConfig), "%s%s", szBaseDir, "\\config");
				if (GetFileAttributes (m_szConfig) == INVALID_FILE_ATTRIBUTES) {
					m_szConfig[0] = 0;
				} else {
					m_apsz[m_nCount++] = m_szConfig;
				}
				// Project JAR
				if ((*pargc > 1) && !strncmp (argv[1], "-p", 2)) {
					StringCbPrintf (m_szProject, sizeof (m_szProject), "%s\\%s", szBaseDir, argv[1] + 2);
					if (GetFileAttributes (m_szProject) == INVALID_FILE_ATTRIBUTES) {
						StringCbPrintf (m_szProject, sizeof (m_szProject), "%s%s%s", szBaseDir, "\\target\\", argv[1] + 2);
						if (GetFileAttributes (m_szProject) == INVALID_FILE_ATTRIBUTES) {
							m_szProject[0] = 0;
						} else {
							m_apsz[m_nCount++] = m_szProject;
						}
					} else {
						m_apsz[m_nCount++] = m_szProject;
					}
					(*pargc)--;
					memmove (argv + 1, argv + 2, sizeof (char*) * *pargc);
				}
				// Other libraries
				StringCbPrintf (m_szLib, sizeof (m_szLib), "%s%s", szBaseDir, "\\lib\\*");
				if (GetFileAttributes (m_szConfig) == INVALID_FILE_ATTRIBUTES) {
					m_szLib[0] = 0;
				} else {
					m_apsz[m_nCount++] = m_szLib;
				}
			} else {
				ReportErrorReference (ERROR_REF_MAIN);
			}
		} else {
			ReportErrorReference (ERROR_REF_MAIN);
		}
	}
	int ReadInteger (PCSTR pszName, int nDefault) {
		if (!strcmp (pszName, "count")) return m_nCount;
		return nDefault;
	}
	size_t ReadString (PCSTR pszName, PSTR pszBuffer, size_t cbBuffer, PCSTR pszDefault) {
		if (!strcmp (pszName, "path0")) return Path (m_apsz[0], pszBuffer, cbBuffer);
		if (!strcmp (pszName, "path1")) return Path (m_apsz[1], pszBuffer, cbBuffer);
		if (!strcmp (pszName, "path2")) return Path (m_apsz[2], pszBuffer, cbBuffer);
		if (pszDefault) {
			StringCbCopy (pszBuffer, cbBuffer, pszDefault);
			return strlen (pszDefault);
		} else {
			return 0;
		}
	}
};

/// Presents command line arguments as an INI config section for the shared libraries.
///
/// Default MEM_OPTS and GC_OPTS values are hard-coded which can be overridden by the
/// registry. This is for compatibility with old style installers.
///
/// A default config section is passed - these options are presented ahead of anything
/// from the command line. This is taken from, for example, RunTool.exe.defaults if it
/// is present.
class CRunToolOptions : public CConfigSourceSection {
private:
	CConfigSourceSection *m_poDefaults;
	char **m_apsz;
	int m_nLength;
	int m_nCount;
	BOOL GetRegistryOpts (HKEY hkey, PCSTR pszName, PSTR pszOpts, DWORD cbOpts) {
		DWORD dwType;
		return RegGetValue (hkey, "SOFTWARE\\OpenGammaLtd\\RunTool", pszName, RRF_RT_REG_SZ, &dwType, pszOpts, &cbOpts) == ERROR_SUCCESS;
	}
	BOOL GetRegistryOpts (PCSTR pszName, PSTR pszOpts, DWORD cbOpts) {
		return GetRegistryOpts (HKEY_CURRENT_USER, pszName, pszOpts, cbOpts) || GetRegistryOpts (HKEY_LOCAL_MACHINE, pszName, pszOpts, cbOpts);
	}
	BOOL GetRegistryMemOpts (PSTR pszOpts, DWORD cbOpts) {
		return GetRegistryOpts ("MEM_OPTS", pszOpts, cbOpts);
	}
	BOOL GetDefaultMemOpts (PSTR pszOpts, DWORD cbOpts) {
		// TODO: These were the defaults that were hardcoded in run-tool.bat. Might want lower for 32-bit builds. Might want % based settings.
		StringCbCopy (pszOpts, cbOpts, "-Xms512m -Xmx1024m -XX:MaxPermSize=256M");
		return TRUE;
	}
	BOOL GetRegistryGCOpts (PSTR pszOpts, DWORD cbOpts) {
		return GetRegistryOpts ("GC_OPTS", pszOpts, cbOpts);
	}
	BOOL GetDefaultGCOpts (PSTR pszOpts, DWORD cbOpts) {
		// TODO: These were the defaults that were hardcoded in run-tool.bat
		StringCbCopy (pszOpts, cbOpts, "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing");
		return TRUE;
	}
	void ensureCapacity () {
		if (m_nCount >= m_nLength) {
			m_nLength = m_nCount * 2;
			char **apsz = new char*[m_nLength];
			memcpy (apsz, m_apsz, m_nCount * sizeof (char*));
			delete m_apsz;
			m_apsz = apsz;
		}
	}
	void ParseOptions (PSTR psz) {
		char *pszState;
		ensureCapacity ();
		m_apsz[m_nCount] = strtok_s (psz, " ", &pszState);
		while (m_apsz[m_nCount]) {
			m_apsz[m_nCount] = _strdup (m_apsz[m_nCount]);
			m_nCount++;
			ensureCapacity ();
			m_apsz[m_nCount] = strtok_s (NULL, " ", &pszState);
		}
	}
public:
	CRunToolOptions (CConfigSourceSection *poDefaults, int *pargc, char **argv) {
		char sz[MAX_PATH];
		m_poDefaults = poDefaults;
		m_apsz = new char*[m_nLength = 8];
		m_nCount = 0;
		if (GetRegistryMemOpts (sz, sizeof (sz)) || GetDefaultMemOpts (sz, sizeof (sz))) {
			ParseOptions (sz);
		}
		if (GetRegistryGCOpts (sz, sizeof (sz)) || GetDefaultGCOpts (sz, sizeof (sz))) {
			ParseOptions (sz);
		}
		while ((*pargc > 1) && !strncmp (argv[1], "-D", 2)) {
			if (!strncmp (argv[1] + 2, "tool.gui", 8)) {
				if (!strcmp (argv[1] + 10, "=true")) {
					// Suppress the console window
					FreeConsole ();
				} else if (!strncmp (argv[1] + 10, ".console=", 9)) {
					// Treat -Dtool.gui.console as -Dtool.gui but without console suppression
					memmove (argv[1] + 10, argv[1] + 18, strlen (argv[1] + 18) + 1);
				}
			}
			ensureCapacity ();
			m_apsz[m_nCount++] = _strdup (argv[1]);
			(*pargc)--;
			memmove (argv + 1, argv + 2, sizeof (char*) * *pargc);
		}
	}
	~CRunToolOptions () {
		int i;
		for (i = 0; i < m_nCount; i++) {
			delete m_apsz[i];
		}
		delete m_apsz;
		if (m_poDefaults) delete m_poDefaults;
	}
	int ReadInteger (PCSTR pszName, int nDefault) {
		if (!strcmp (pszName, "count")) {
			int nCount = m_nCount;
			if (m_poDefaults) nCount += m_poDefaults->ReadInteger (pszName, 0);
			return nCount;
		}
		return m_poDefaults ? m_poDefaults->ReadInteger (pszName, nDefault) : nDefault;
	}
	size_t ReadString (PCSTR pszName, PSTR pszBuffer, size_t cbBuffer, PCSTR pszDefault) {
		if (!strncmp (pszName, "opt", 3)) {
			int nOpt = atoi (pszName + 3);
			if (m_poDefaults) {
				int nDefaultCount = m_poDefaults->ReadInteger ("count", 0);
				if (nOpt < nDefaultCount) {
					return m_poDefaults->ReadString (pszName, pszBuffer, cbBuffer, pszDefault);
				}
				nOpt -= nDefaultCount;
			}
			if ((nOpt >= 0) && nOpt < m_nCount) {
				StringCbCopy (pszBuffer, cbBuffer, m_apsz[nOpt]);
				return strlen (m_apsz[nOpt]);
			}
		}
		if (pszDefault) {
			StringCbCopy (pszBuffer, cbBuffer, pszDefault);
			return strlen (pszDefault);
		} else {
			return 0;
		}
	}
};

/// Presents command line arguments as an INI config section for the shared libraries.
class CRunToolArgs : public CConfigSourceSection {
private:
	int m_nArgs;
	char **m_pszArgs;
public:
	CRunToolArgs (int argc, char **argv) {
		m_nArgs = argc;
		m_pszArgs = argv;
	}
	int ReadInteger (PCSTR pszName, int nDefault) {
		if (!strcmp (pszName, "argc")) return m_nArgs;
		return nDefault;
	}
	size_t ReadString (PCSTR pszName, PSTR pszBuffer, size_t cbBuffer, PCSTR pszDefault) {
		if (!strncmp (pszName, "arg", 3)) {
			int nArg = atoi (pszName + 3);
			if ((nArg >= 0) && nArg < m_nArgs) {
				StringCbCopy (pszBuffer, cbBuffer, m_pszArgs[nArg]);
				return strlen (m_pszArgs[nArg]);
			}
		}
		if (pszDefault) {
			StringCbCopy (pszBuffer, cbBuffer, pszDefault);
			return strlen (pszDefault);
		} else {
			return 0;
		}
	}
};

/// Presents a config source to the shared libraries that contain data from the command
/// line, assumed data (when running in a legacy mode), and specific defaults from an
/// INI file.
///
/// A file called, for example, RunTool.exe.defaults is checked for and used if found.
/// Otherwise the previous behaviour is used (see LegacyRunToolClasspath).
class CRunToolConfigSource : public CConfigSource {
private:
	int *m_pargc;
	char **m_argv;
	char *m_pszDefaults;
	CConfigSource *m_poDefaults;
public:
	CRunToolConfigSource (int *pargc, char **argv) {
		char szModule[MAX_PATH];
		m_pargc = pargc;
		m_argv = argv;
		m_pszDefaults = NULL;
		m_poDefaults = NULL;
		if (GetModuleFileName (NULL, szModule, MAX_PATH) > 0) {
			StringCbCat (szModule, sizeof (szModule), TEXT (".defaults"));
			if (GetFileAttributes (szModule) != INVALID_FILE_ATTRIBUTES) {
				m_pszDefaults = _strdup (szModule);
				EnvironmentInit (m_pszDefaults);
				m_poDefaults = new CFileConfigSource (m_pszDefaults);
			}
		}
	}
	~CRunToolConfigSource () {
		if (m_pszDefaults) free (m_pszDefaults);
		if (m_poDefaults) delete m_poDefaults;
	}
	CConfigSourceSection *OpenSection (PCSTR pszSection) {
		CConfigSourceSection *poDefaults = m_poDefaults ? m_poDefaults->OpenSection (pszSection) : NULL;
		if (!strcmp (pszSection, "Classpath")) {
			if (poDefaults) {
				return poDefaults;
			} else {
				return new CLegacyRunToolClasspath (m_pargc, m_argv);
			}
		}
		if (!strcmp (pszSection, "Options")) {
			return new CRunToolOptions (poDefaults, m_pargc, m_argv);
		}
		return poDefaults;
	}
};

static CJavaVM *_InitialiseJVM (int *pargc, char **argv) {
	CRunToolConfigSource oConfig (pargc, argv);
	if (!CJavaRT::s_oConfig.Read (&oConfig)) {
		ReportErrorReference (ERROR_REF_MAIN);
		return NULL;
	}
	CJavaRT *poJavaRT = CJavaRT::Init ();
	if (!poJavaRT) {
		ReportErrorReference (ERROR_REF_MAIN);
		return NULL;
	}
	CJavaVM *poJavaVM = poJavaRT->CreateVM ();
	if (!poJavaVM) {
		ReportErrorReference (ERROR_REF_MAIN);
		return NULL;
	}
	return poJavaVM;
}

static void _tr (char *psz) {
	while (*psz) {
		if (*psz == '.') {
			*psz = '/';
		}
		psz++;
	}
}

int main (int argc, char **argv) {
	if (!_ConnectToConsole (&argc, argv)) {
		return ERROR_INTERNAL_ERROR;
	}
	if (_ElevateSelf (&argc, argv)) {
		return _RunElevated (argc, argv);
	}
	CJavaVM *poJava = _InitialiseJVM (&argc, argv);
	if (!poJava) {
		return ERROR_INTERNAL_ERROR;
	}
	if (argc < 2) {
		ReportErrorReference (ERROR_REF_MAIN);
		fprintf (stderr, "Usage is %0 <classname> [<arg> ...]\n", argv[0]);
		return ERROR_INTERNAL_ERROR;
	}
	_tr (argv[1]);
	CConfigString oClass ("class", argv[1]);
	CConfigString oMethod ("method", "main");
	CConfigMultiString oArgs ("argc", "arg%d");
	CRunToolArgs oArgStrings (argc - 2, argv + 2);
	oArgs.Read (&oArgStrings);
	DWORD dwError = poJava->Invoke (&oClass, &oMethod, &oArgs);
	if (dwError) {
		ReportErrorReference (dwError);
		return ERROR_INTERNAL_ERROR;
	}
	return 0;
}
