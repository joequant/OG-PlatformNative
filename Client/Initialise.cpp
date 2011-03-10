/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Settings.h"
#ifndef _WIN32
#include "DllVersionInfo.h"
#endif /* ifndef _WIN32 */
#include <Util/DllVersion.h>

LOGGING (com.opengamma.pirate.client.Initialise);

static bool InitialiseDllVersion () {
#ifndef _WIN32
	LOGDEBUG (TEXT ("Initialising DLL version info"));
	CDllVersion::Initialise ();
#endif /* ifndef _WIN32 */
	return true;
}

static bool InitialiseLogging () {
	CSettings oSettings;
	LoggingInit (&oSettings);
	return true;
}

bool Initialise () {
	LOGINFO (TEXT ("Initialising library"));
	return InitialiseDllVersion () && InitialiseLogging ();
}
