/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_discovery_h
#define __inc_clean_discovery_h

#include "StringSet.h"

class CDiscovery {
private:
	CStringSet m_oServices;
	CStringSet m_oRegistry;
	CStringSet m_oFolders;
	void ReportProcesses ();
	void ReportServices ();
	void ReportRegistry ();
	void ReportFolders ();
public:
	void ReportService (PCTSTR pszService) { m_oServices.Add (pszService); }
	void ReportRegistry (PCTSTR pszPath) { m_oRegistry.Add (pszPath); }
	void ReportFolders (PCTSTR pszPath) { m_oFolders.Add (pszPath); }
	void WriteConfiguration ();
};

#endif /* ifndef __inc_clean_discovery_h */
