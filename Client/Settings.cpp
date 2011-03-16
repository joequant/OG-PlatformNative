/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Settings.h)

LOGGING (com.opengamma.rstats.client.Settings);

#define DEFAULT_STARTUP_TIMEOUT		30000 /* 30s */

CSettings::CSettings () : CAbstractSettings () {
}

CSettings::~CSettings () {
}

long CSettings::GetStartupTimeout () {
	return GetStartupTimeout (DEFAULT_STARTUP_TIMEOUT);
}
