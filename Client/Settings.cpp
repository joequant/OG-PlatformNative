/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Settings.h"

LOGGING (com.opengamma.pirate.client.Settings);

#define DEFAULT_LOG_CONFIGURATION	NULL

CSettings::CSettings () : CAbstractSettings () {
}

CSettings::~CSettings () {
}

const TCHAR *CSettings::GetLogConfiguration () {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}
