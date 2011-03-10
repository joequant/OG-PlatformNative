/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Settings.h"

LOGGING (com.opengamma.pirate.client.Settings);

#define DEFAULT_LOG_CONFIGURATION	NULL
#define DEFAULT_STARTUP_TIMEOUT		30000 /* 30s */

CSettings::CSettings () : CAbstractSettings () {
}

CSettings::~CSettings () {
}

const TCHAR *CSettings::GetLogConfiguration () {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

long CSettings::GetStartupTimeout () {
	return GetStartupTimeout (DEFAULT_STARTUP_TIMEOUT);
}
