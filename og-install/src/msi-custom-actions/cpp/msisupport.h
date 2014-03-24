/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_customactions_msisupport_h
#define __inc_customactions_msisupport_h

#include <Windows.h>
#include <MsiQuery.h>

class CMsiPropertyValue {
private:
	PTSTR m_pszData;
	DWORD m_dwError;
public:
	CMsiPropertyValue (MSIHANDLE hInstall, PCTSTR pszProperty);
	virtual ~CMsiPropertyValue ();
	static PTSTR Get (MSIHANDLE hInstall, PCTSTR pszProperty);
	PTSTR Get ();
	BOOL Equals (PCTSTR pszText);
};

class CCustomActionData : public CMsiPropertyValue {
public:
	CCustomActionData (MSIHANDLE hInstall) : CMsiPropertyValue (hInstall, TEXT ("CustomActionData")) { }
};

class CRemove : public CMsiPropertyValue {
public:
	CRemove (MSIHANDLE hInstall) : CMsiPropertyValue (hInstall, TEXT ("REMOVE")) { }
	BOOL IsRemoveAll () { return Equals (TEXT ("ALL")); }
};

#endif /* ifndef __inc_customactions_msisupport_h */