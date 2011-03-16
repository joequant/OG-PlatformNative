/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_package_datavalue_h
#define __inc_og_pirate_package_datavalue_h

#include <Connector/com_opengamma_language_Data.h>

class CValue {
private:
	CValue () { }
	~CValue () { }
public:
	static com_opengamma_language_Value *FromSEXP (SEXP value, int index = 0);
	static SEXP ToSEXP (const com_opengamma_language_Value *pValue);
	static void ToSEXP (int type, SEXP vector, int index, const com_opengamma_language_Value *pValue);
	static void Release (com_opengamma_language_Value *pValue) { com_opengamma_language_Value_free (pValue); }
};

class CData {
private:
	CData () { }
	~CData () { }
public:
	static com_opengamma_language_Data *FromSEXP (SEXP data);
	static SEXP ToSEXP (const com_opengamma_language_Data *pData);
	static void Release (com_opengamma_language_Data *pData) { com_opengamma_language_Data_free (pData); }
};

#endif /* ifndef __inc_og_pirate_package_datavalue_h */
