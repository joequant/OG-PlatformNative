/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the system information querying methods

#include <connector/cpp/SystemInfo.h>
#include <version.h>

LOGGING (com.opengamma.language.connector.SystemInfoTest);

#define TEST_LANGUAGE		TEXT ("test")
#define TIMEOUT_STARTUP		30000

static CConnector *g_poConnector;

static void StartConnector () {
	g_poConnector = CConnector::Start (TEST_LANGUAGE);
	ASSERT (g_poConnector);
	ASSERT (g_poConnector->WaitForStartup (TIMEOUT_STARTUP));
}

static void StopConnector () {
	ASSERT (g_poConnector->Stop ());
	CConnector::Release (g_poConnector);
	g_poConnector = NULL;
}

static void QueryLSID () {
	CSystemInfoQuery query (g_poConnector);
	query.SetQueryField (com_opengamma_language_config_SystemInfo_lsid_Ordinal);
	ASSERT (query.Send ());
	com_opengamma_language_config_SystemInfo *pInfo = query.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pInfo);
	ASSERT (pInfo->_lsid);
	LOGINFO (TEXT ("Got server identifier ") << pInfo->_lsid);
}

static void QueryConfigurationURL () {
	CSystemInfoQuery query (g_poConnector);
	query.SetQueryField (com_opengamma_language_config_SystemInfo_configurationURL_Ordinal);
	ASSERT (query.Send ());
	com_opengamma_language_config_SystemInfo *pInfo = query.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pInfo);
	ASSERT (pInfo->_configurationURL);
	LOGINFO (TEXT ("Got URL ") << pInfo->_configurationURL);
}

static void QueryServerDescription () {
	CSystemInfoQuery query (g_poConnector);
	query.SetQueryField (com_opengamma_language_config_SystemInfo_serverDescription_Ordinal);
	ASSERT (query.Send ());
	com_opengamma_language_config_SystemInfo *pInfo = query.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pInfo);
	ASSERT (pInfo->_serverDescription);
	LOGINFO (TEXT ("Got server description ") << pInfo->_serverDescription);
}

static void QueryClientVersion () {
	CSystemInfoQuery query (g_poConnector);
	query.SetQueryField (com_opengamma_language_config_SystemInfo_ogLanguageVersion_Ordinal);
	ASSERT (query.Send ());
	com_opengamma_language_config_SystemInfo *pInfo = query.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pInfo);
	ASSERT (pInfo->_ogLanguageVersion);
	LOGINFO (TEXT ("Got OG-Language version ") << pInfo->_ogLanguageVersion);
	TCHAR sz[32];
	StringCbPrintf (sz, sizeof (sz), TEXT ("%d.%d.%d.%d%s"), VERSION_MAJOR, VERSION_MINOR, REVISION, BUILD_NUMBER, TEXT(VERSION_SUFFIX));
	ASSERT (!_tcscmp (pInfo->_ogLanguageVersion, sz));
}

static void QueryServerVersion () {
	CSystemInfoQuery query (g_poConnector);
	query.SetQueryField (com_opengamma_language_config_SystemInfo_ogPlatformVersion_Ordinal);
	ASSERT (query.Send ());
	com_opengamma_language_config_SystemInfo *pInfo = query.Recv (CRequestBuilder::GetDefaultTimeout ());
	ASSERT (pInfo);
	ASSERT (pInfo->_ogPlatformVersion);
	LOGINFO (TEXT ("Got OG-Platform version ") << pInfo->_ogPlatformVersion);
}

BEGIN_TESTS(SystemInfoTest)
	INTEGRATION_TEST (QueryLSID)
	INTEGRATION_TEST (QueryConfigurationURL)
	INTEGRATION_TEST (QueryServerDescription)
	INTEGRATION_TEST (QueryClientVersion)
	INTEGRATION_TEST (QueryServerVersion)
	BEFORE_TEST (StartConnector)
	AFTER_TEST (StopConnector)
END_TESTS
