/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#ifndef _WIN32
#include Client(DllVersionInfo.h)
#endif /* ifndef _WIN32 */
#include Client(Settings.h)
#include <Util/DllVersion.h>

LOGGING (com.opengamma.rstats.client.Initialise);

static CConnector *g_poConnector = NULL;

static bool InitialiseDllVersion () {
#ifndef _WIN32
	LOGDEBUG (TEXT ("Initialising DLL version info"));
	CDllVersion::Initialise ();
#endif /* ifndef _WIN32 */
	return true;
}

static bool InitialiseLogging () {
	LOGDEBUG (TEXT ("Initialising logging"));
	CSettings oSettings;
	LoggingInit (&oSettings);
	return true;
}

static bool InitialiseConnector () {
	LOGDEBUG (TEXT ("Initialising connector"));
	g_poConnector = CConnector::Start (TEXT ("R"));
	return g_poConnector != NULL;
}

static bool WaitForConnectorInitialisation () {
	LOGDEBUG (TEXT ("Waiting for connector startup"));
	CSettings oSettings;
	return g_poConnector->WaitForStartup (oSettings.GetStartupTimeout ());
}

bool Initialise () {
	if (g_poConnector) {
		LOGWARN (TEXT ("Library already initialised"));
		SetLastError (EALREADY);
		return false;
	}
	if (!InitialiseDllVersion ()) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't initialise DLL version info, error ") << ec);
		SetLastError (ec);
		return false;
	}
	if (!InitialiseLogging ()) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't initialise logging, error ") << ec);
		SetLastError (ec);
		return false;
	}
	LOGINFO (TEXT ("Initialising library"));
	if (!InitialiseConnector ()) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't initialise connector, error ") << ec);
		SetLastError (ec);
		return false;
	}
	// TODO: any other startup routines here while the connection is being established
	if (!WaitForConnectorInitialisation ()) {
		int ec = GetLastError ();
		LOGWARN (TEXT ("Couldn't initialise connector, error ") << ec);
		CConnector::Release (g_poConnector);
		g_poConnector = NULL;
		SetLastError (ec);
		return false;
	}
	// TODO: any other initialisation that requires the connection
	LOGINFO (TEXT ("Library initialised"));
	return true;
}

bool Shutdown () {
	LOGINFO (TEXT ("Shutting down library"));
	if (!g_poConnector) {
		LOGWARN (TEXT ("Library not initialised"));
		SetLastError (EALREADY);
		return false;
	}
	LOGDEBUG (TEXT ("Stopping connector"));
	if (!g_poConnector->Stop ()) {
		LOGWARN (TEXT ("Couldn't stop connector, error ") << GetLastError ());
	}
	LOGDEBUG (TEXT ("Connector stopped, releasing pointer"));
	CConnector::Release (g_poConnector);
	g_poConnector = NULL;
	LOGDEBUG (TEXT ("Pointers released"));
	return true;
}

CConnector *ConnectorInstance () {
	if (!g_poConnector) {
		LOGFATAL (TEXT ("ConnectorInstance called on uninitialised library"));
		assert (0);
		return NULL;
	}
	g_poConnector->Retain ();
	return g_poConnector;
}
