/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include <service/cpp/Service.h>

/// Suppress the warnings that come from the LOGGING macros.
static CSuppressLoggingWarning g_oSuppressLoggingWarning;

#ifndef __cplusplus_cli
int main (int argc, TCHAR **argv) {
#ifndef _WIN32
	if ((argc == 3) && !_tcscmp (argv[1], TEXT ("jvm"))) {
		return ServiceTestJVM (argv[2]) ? 0 : 1;
	}
#endif /* ifndef _WIN32 */
	CAbstractTest::Main (argc, argv);
	return 0;
}
#endif /* ifndef __cplusplus_cli */
