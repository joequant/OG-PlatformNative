/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_settings_h
#define __inc_og_pirate_client_settings_h

#define SETTINGS_STARTUP_TIMEOUT	TEXT ("startupTimeout")

#include <Connector/Settings.h>
#undef CSettings
#define CSettings CClientSettings

class CSettings : public CAbstractSettings {
private:
	CConnectorSettings m_oConnectorSettings;
	long GetStartupTimeout (long lDefault) const { return Get (SETTINGS_STARTUP_TIMEOUT, lDefault); }
public:
	CSettings ();
	~CSettings ();
	const TCHAR *GetLogConfiguration () const { return m_oConnectorSettings.GetLogConfiguration (); }
	long GetStartupTimeout () const;
};

#endif /* ifndef __inc_og_pirate_client_settings_h */
