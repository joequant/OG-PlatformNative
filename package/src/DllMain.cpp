/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Connector.h)
#include Client(Initialise.h)
#include Client(Repositories.h)
#define GLOBALS
#include "Functions.h"
#include "LiveData.h"
#include "Procedures.h"
#include "globals.h"

LOGGING (com.opengamma.pirate.package.DllMain);

extern "C" {
	void LibExport R_init_OpenGamma (DllInfo *pInfo);
	void LibExport R_unload_OpenGamma (DllInfo *pInfo);
}

#define F(name, args) { #name, (DL_FUNC)&name##args, args }

static R_CallMethodDef g_aMethods[] = {
	F (Functions_count, 0),
	F (Functions_getName, 1),
	F (Functions_getParameterFlags, 1),
	F (Functions_getParameterNames, 1),
	F (Functions_invoke, 2),
	F (LiveData_count, 0),
	F (LiveData_getName, 1),
	F (Procedures_count, 0),
	F (Procedures_getName, 1),
	{ NULL, NULL, 0 }
};

void LibExport R_init_OpenGamma (DllInfo *pInfo) {
	LOGDEBUG (TEXT ("Initialising Dll"));
	g_poFunctions = NULL;
	g_poLiveData = NULL;
	g_poProcedures = NULL;
	if (!Initialise ()) {
		LOGERROR (TEXT ("Couldn't initialise DLL, error ") << GetLastError ());
		return;
	}
	CConnector *poConnector = ConnectorInstance ();
	if (!poConnector) {
		LOGERROR (TEXT ("No connector"));
		return;
	}
	CRepositories repositories (poConnector);
	g_poFunctions = repositories.GetFunctions ();
	g_poLiveData = repositories.GetLiveData ();
	g_poProcedures = repositories.GetProcedures ();
	CConnector::Release (poConnector);
	R_registerRoutines (pInfo, NULL, g_aMethods, NULL, NULL);
}

void LibExport R_unload_OpenGamma (DllInfo *pInfo) {
	LOGINFO (TEXT ("Unloading DLL"));
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
		LOGERROR (TEXT ("Couldn't shutdown DLL, error ") << GetLastError ());
		return;
	}
}
