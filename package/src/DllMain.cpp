/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Initialise.h)
#include <R.h>
#include <Rinternals.h>
#include <R_ext/Rdynload.h>

LOGGING (com.opengamma.pirate.package.DllMain);

extern "C" {

void LibExport R_init_OpenGamma (DllInfo *pInfo) {
	LOGDEBUG (TEXT ("Initialising Dll"));
	if (!Initialise ()) {
		LOGERROR (TEXT ("Couldn't initialise DLL, error ") << GetLastError ());
	}
}

void LibExport R_unload_OpenGamma (DllInfo *pInfo) {
	LOGINFO (TEXT ("Unloading DLL"));
	if (!Shutdown ()) {
		LOGERROR (TEXT ("Couldn't shutdown DLL, error ") << GetLastError ());
	}
}

}
