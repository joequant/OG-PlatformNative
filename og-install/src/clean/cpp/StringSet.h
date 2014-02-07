/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_stringset_h
#define __inc_clean_stringset_h

class CStringSet {
private:
	PTSTR *m_apsz;
	int m_nSize;
	int m_nCount;
public:
	CStringSet ();
	~CStringSet ();
	void Add (PCTSTR psz);
	BOOL Contains (PCTSTR psz);
	int Size ();
	PCTSTR Get (int nIndex);
};

#endif /* ifndef __inc_clean_stringset_h */
