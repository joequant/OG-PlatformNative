/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Connector.h)
#include Client(Initialise.h)
#include Client(Repositories.h)
#define GLOBALS
#include "FudgeMsg.h"
#include "globals.h"
#include "Functions.h"
#include "ExternalRef.h"
#include "LiveData.h"
#include "Procedures.h"
#include "Errors.h"
#include "StringUtil.h"

/// Try and suppress the error message written to stderr to avoid R CMD check from failing.
static CSuppressLoggingWarning g_oSetQuietMode;

LOGGING (com.opengamma.rstats.package.DllMain);

/// Pre-load tracker to monitor the stub OG package and force a reload if necessary.
class CPreload {
private:

	/// TRUE if the OG stub is loaded, FALSE if it is not
	bool m_bOGLoaded;

public:

	/// Creates a new, reset, instance
	CPreload () {
		Clear ();
	}

	/// Resets the instance, with OG initially unloaded.
	void Clear () {
		m_bOGLoaded = false;
	}

	/// Marks OG as loaded before the OpenGamma package.
	void SetOGLoaded () {
		m_bOGLoaded = true;
	}

	/// Tests if OG was loaded before the OpenGamma package and clears
	/// the flag if it is.
	///
	/// @return TRUE if the OG stub was loaded before OpenGamma, FALSE otherwise
	bool IsOGLoaded () {
		bool bResult = m_bOGLoaded;
		m_bOGLoaded = false;
		return bResult;
	}

};

/// The pre-load tracking instance
static CPreload g_oPreload;

extern "C" {

	void LibExport R_init_OpenGamma (DllInfo *pInfo);
	void LibExport R_unload_OpenGamma (DllInfo *pInfo);

	/// Tests if the library is "happy" or not. I.e. did the call to R_init_OpenGamma do
	/// as it should have done.
	SEXP RPROC DllMain_check0 () {
		SEXP result = allocVector (LGLSXP, 1);
		if (result != R_NilValue) {
			if (g_poFunctions && g_poLiveData && g_poProcedures) {
				LOGINFO (TEXT ("Library initialised ok"));
				INTEGER (result)[0] = 1;
			} else {
				LOGWARN (TEXT ("Library failed to initialise"));
				INTEGER (result)[0] = 0;
			}
		} else {
			LOGFATAL (ERR_R_FUNCTION);
		}
		return result;
	}

	/// Tests if OG was loaded before OpenGamma, return TRUE if it was and FALSE otherwise.
	SEXP RPROC DllMain_isPreload0 () {
		SEXP result = allocVector (INTSXP, 1);
		if (result != R_NilValue) {
			*INTEGER (result) = g_oPreload.IsOGLoaded ();
		}
		return result;
	}

	/// Sets the OG preload flag if the namespace is available before the OpenGamma namespace
	/// is loaded.
	SEXP RPROC DllMain_setPreload0 () {
		g_oPreload.SetOGLoaded ();
		return R_NilValue;
	}
	
}

#define F(name, args) { #name, (DL_FUNC)&name##args, args }

/// Methods exported to R. The methods are constructed from a macro to append the number of parameters
/// and force a small amount of validation rather than stack explosions if the number of arguments
/// changes in the future.
static R_CallMethodDef g_aMethods[] = {
	F (DllMain_check, 0),
	F (DllMain_isPreload, 0),
	F (DllMain_setPreload, 0),
	F (ExternalRef_create, 2),
	F (ExternalRef_fetch, 1),
	F (FudgeMsg_getAllFields, 1),
	F (FudgeMsg_setSerialiseMode, 1),
	F (Functions_count, 0),
	F (Functions_getCategory, 1),
	F (Functions_getDescription, 1),
	F (Functions_getName, 1),
	F (Functions_getParameterFlags, 1),
	F (Functions_getParameterNames, 1),
	F (Functions_getParameterDescriptions, 1),
	F (Functions_invoke, 3),
	F (LiveData_count, 0),
	F (LiveData_getCategory, 1),
	F (LiveData_getDescription, 1),
	F (LiveData_getName, 1),
	F (LiveData_getParameterFlags, 1),
	F (LiveData_getParameterNames, 1),
	F (LiveData_getParameterDescriptions, 1),
	F (LiveData_invoke, 3),
	F (Procedures_count, 0),
	F (Procedures_getCategory, 1),
	F (Procedures_getDescription, 1),
	F (Procedures_getName, 1),
	F (Procedures_getParameterFlags, 1),
	F (Procedures_getParameterNames, 1),
	F (Procedures_getParameterDescriptions, 1),
	F (Procedures_invoke, 3),
	F (String_escape, 2),
	{ NULL, NULL, 0 }
};

/// Shutdown hook (if it has not already been called) for process termination or shared library unload.
static void _shutdown () {
	LOGDEBUG (TEXT ("Shutdown hook"));
	if (Shutdown ()) {
		LOGINFO (TEXT ("Connector shutdown from termination hook"));
	} else {
		int ec = GetLastError ();
		if (ec != EALREADY) {
			LOGWARN (TEXT ("Couldn't shut down library, error ") << ec);
		}
	}
}

/// Initialise the OpenGamma package. A connection is established to the Java stack and the repositories
/// of functions, procedures and livedata sources creates for use by R.
///
/// @param[in] pInfo see R documentation
void LibExport R_init_OpenGamma (DllInfo *pInfo) {
	LOGDEBUG (TEXT ("Initialising Dll"));
	g_oPreload.Clear ();
	g_poFunctions = NULL;
	g_poLiveData = NULL;
	g_poProcedures = NULL;
	if (Initialise ()) {
		const CConnector *poConnector = ConnectorInstance ();
		if (poConnector) {
			CRepositories repositories (poConnector);
			g_poFunctions = repositories.GetFunctions ();
			g_poLiveData = repositories.GetLiveData ();
			g_poProcedures = repositories.GetProcedures ();
			CConnector::Release (poConnector);
		} else {
			LOGERROR (ERR_INITIALISATION);
		}
	} else {
		LOGWARN (TEXT ("Couldn't initialise DLL, error ") << GetLastError ());
		LOGERROR (ERR_INITIALISATION);
	}
	R_registerRoutines (pInfo, NULL, g_aMethods, NULL, NULL);
	atexit (_shutdown);
}

/// Shuts down the OpenGamma package. The connection to the Java stack is released and any allocated
/// memory freed.
///
/// @param[in] pInfo see R documentation
void LibExport R_unload_OpenGamma (DllInfo *pInfo) {
	LOGINFO (TEXT ("Unloading DLL"));
	g_oPreload.Clear ();
	if (g_poFunctions) {
		CFunctions::Release (g_poFunctions);
		g_poFunctions = NULL;
	}
	if (g_poLiveData) {
		CLiveData::Release (g_poLiveData);
		g_poLiveData = NULL;
	}
	if (g_poProcedures) {
		CProcedures::Release (g_poProcedures);
		g_poProcedures = NULL;
	}
	if (!Shutdown ()) {
		LOGWARN (TEXT ("Couldn't shutdown DLL, error ") << GetLastError ());
		LOGERROR (ERR_INITIALISATION);
		return;
	}
}
