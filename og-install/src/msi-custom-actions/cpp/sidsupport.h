/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_customactions_sidsupport_h
#define __inc_customactions_sidsupport_h

#include <Windows.h>

class CSecurityAccount {
private:
	PTSTR m_pszName;
	BOOL m_bFreeName;
	PSID m_psid;
	BOOL m_bFreeSID;
	DWORD m_dwError;
public:
	CSecurityAccount (PCTSTR pszName);
	CSecurityAccount (PSID psid);
	~CSecurityAccount ();
	PCTSTR GetName ();
	PSID GetSID ();
	static PTSTR GetName (PSID psid);
	static PSID GetSID (PCTSTR pszName);
};

#endif /* ifndef __inc_customactions_sidsupport_h */