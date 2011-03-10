/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_settings_h
#define __inc_og_pirate_client_settings_h

#define SETTINGS_LOG_CONFIGURATION	TEXT ("clientLogConfiguration")

class CSettings : public CAbstractSettings {
private:
	const TCHAR *GetLogConfiguration (const TCHAR *pszDefault) { return Get (SETTINGS_LOG_CONFIGURATION, pszDefault); }
public:
	CSettings ();
	~CSettings ();
	const TCHAR *GetLogConfiguration ();
};

#endif /* ifndef __inc_og_pirate_client_settings_h */
