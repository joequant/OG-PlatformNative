/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_customactions_msisupport_h
#define __inc_customactions_msisupport_h

#include <Windows.h>
#include <MsiQuery.h>

class CCustomActionData {
private:
	PTSTR m_pszData;
	DWORD m_dwError;
public:
	CCustomActionData (MSIHANDLE hInstall);
	~CCustomActionData ();
	static PTSTR Get (MSIHANDLE hInstall);
	PTSTR Get ();
};

#endif /* ifndef __inc_customactions_msisupport_h */