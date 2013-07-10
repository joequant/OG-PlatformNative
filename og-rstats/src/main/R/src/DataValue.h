/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_datavalue_h
#define __inc_og_rstats_package_datavalue_h

#include <connector/cpp/com_opengamma_language_Data.h>
#include "RCallback.h"

/// Helper functions for converting between the OG-Language Value type and R's SEXP type
class CValue {
private:

	/// Prevents instantiation
	CValue () { }

	/// Prevents instantiation
	~CValue () { }

public:

	/// Releases the memory associated with a Value instance
	static void Release (com_opengamma_language_Value *pValue) { com_opengamma_language_Value_free (pValue); }

	static com_opengamma_language_Value *FromSEXP (const CRCallback *poR, SEXP value, int index = 0);
	static SEXP ToSEXP (const com_opengamma_language_Value *pValue);
	static void ToSEXP (int type, SEXP vector, int index, const com_opengamma_language_Value *pValue);
};

/// Helper functions for converting between the OG-Language Data type and R's SEXP type
class CData {
private:

	/// Prevents instantiation
	CData () { }

	/// Prevents instantiation
	~CData () { }

public:

	/// Releases the memory associated with a Data instance
	static void Release (com_opengamma_language_Data *pData) { com_opengamma_language_Data_free (pData); }

	static com_opengamma_language_Data *FromSEXP (const CRCallback *poR, SEXP data);
	static SEXP ToSEXP (const com_opengamma_language_Data *pData);
};

#endif /* ifndef __inc_og_rstats_package_datavalue_h */
