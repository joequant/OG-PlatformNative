/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_registry_h
#define __inc_clean_registry_h

#include "StringSet.h"

class CRegistry {
private:
	CStringSet m_oWatch;
	int Report (PCTSTR pszPath);
public:
	static int Delete (PCTSTR pszPath);
	void Watch (PCTSTR pszPath) { m_oWatch.Add (pszPath); }
	void Delete ();
	void Report ();
};

#endif /* ifndef __inc_clean_registry_h */
