/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_folders_h
#define __inc_clean_folders_h

#include "StringSet.h"

class CFolders {
private:
	CStringSet m_oWatch;
	int DeleteContents (PCTSTR pszPath);
	int Delete (PCTSTR pszPath);
	int Report (PCTSTR pszPath, DWORD dwIndent = 0);
public:
	void Watch (PCTSTR pszPath);
	void Delete ();
	void Report ();
};

#endif /* ifndef __inc_clean_folders_h */
