/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_services_h
#define __inc_clean_services_h

#include "StringSet.h"

class CServices {
private:
	CStringSet m_oWatch;
	SC_HANDLE m_hSCM;
	SC_HANDLE GetServiceManager ();
	void Stop (SC_HANDLE hService, PCTSTR pszShortName);
	void Kill (SC_HANDLE hService);
	void DeleteNicely (SC_HANDLE hService, PCTSTR pszShortName);
	void DeleteForcefully (PCTSTR pszShortName);
	BOOL StopKillAndDelete (PCTSTR pszShortName);
	void DetailedReport (LPENUM_SERVICE_STATUS_PROCESS lpService);
	void Check (PCTSTR pszShortName);
public:
	CServices ();
	~CServices ();
	void Watch (PCTSTR pszShortName) { m_oWatch.Add (pszShortName); }
	void StopKillAndDelete ();
	void Report ();
	void Check ();
};

#endif /* ifndef __inc_clean_services_h */
