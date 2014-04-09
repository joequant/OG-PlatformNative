/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_common_config_h
#define __inc_common_config_h

class CConfigSourceSection {
public:
	virtual ~CConfigSourceSection () { }
	virtual int ReadInteger (PCSTR pszName, int nDefault) = 0;
	virtual size_t ReadString (PCSTR pszName, PSTR pszBuffer, size_t cbBuffer, PCSTR pszDefault) = 0;
};

class CConfigSource {
public:
	virtual CConfigSourceSection *OpenSection (PCSTR pszSection) = 0;
};

class CFileConfigSourceSection : public CConfigSourceSection {
private:
	PCSTR m_pszFilename;
	PCSTR m_pszSection;
public:
	CFileConfigSourceSection (PCSTR pszFilename, PCSTR pszSection) {
		m_pszFilename = pszFilename;
		m_pszSection = pszSection;
	}
	int ReadInteger (PCSTR pszName, int nDefault) {
		return GetPrivateProfileInt (m_pszSection, pszName, nDefault, m_pszFilename);
	}
	size_t ReadString (PCSTR pszName, PSTR pszBuffer, size_t cbBuffer, PCSTR pszDefault) {
		return GetPrivateProfileString (m_pszSection, pszName, pszDefault, pszBuffer, (DWORD)cbBuffer, m_pszFilename);
	}
};

class CFileConfigSource : public CConfigSource {
private:
	PCSTR m_pszFilename;
public:
	CFileConfigSource (PCSTR pszFilename) {
		m_pszFilename = pszFilename;
	}
	CFileConfigSourceSection *OpenSection (PCSTR pszSection) {
		return new CFileConfigSourceSection (m_pszFilename, pszSection);
	}
};

class CConfigEntry {
public:
	CConfigEntry () { }
	virtual ~CConfigEntry () { }
	virtual BOOL Read (CConfigSourceSection *poConfig) = 0;
};

class CConfigString : public CConfigEntry {
private:
	PCSTR m_pszParameter;
	PCSTR m_pszDefault;
	PSTR m_pszValue;
public:
	CConfigString (PCSTR pszParameter, PCSTR pszDefault);
	~CConfigString ();
	PCSTR GetValue () const { return m_pszValue ? m_pszValue : m_pszDefault; }
	BOOL Read (CConfigSourceSection *poConfig);
};

class CConfigMultiString : public CConfigEntry {
private:
	PCSTR m_pszCount;
	PCSTR m_pszParameter;
	UINT m_nValues;
	PSTR *m_ppszValues;
protected:
	virtual PSTR Malloc (PCSTR pszValue);
public:
	CConfigMultiString (PCSTR pszCount, PCSTR pszParameter);
	~CConfigMultiString ();
	UINT GetValueCount () const { return m_nValues; }
	PCSTR GetValue (UINT nIndex) const { return (nIndex < m_nValues) ? m_ppszValues[nIndex] : NULL; }
	BOOL Read (CConfigSourceSection *poConfig);
};

class CConfigSection {
private:
	PCSTR m_pszSection;
	UINT m_nEntries;
	CConfigEntry **m_ppEntries;
public:
	CConfigSection (PCSTR pszSection, UINT nEntries, CConfigEntry **ppEntries);
	BOOL Read (CConfigSource *poConfig);
};

class CConfig {
private:
	UINT m_nSections;
	CConfigSection **m_ppSections;
public:
	CConfig (UINT nSections, CConfigSection **ppSections);
	BOOL Read (CConfigSource *poConfig);
	BOOL Read (PCSTR pszFilename);
};

#endif /* ifndef __inc_common_config_h */