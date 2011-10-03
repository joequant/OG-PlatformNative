/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "ErrorValue.h"
#include <Connector/Errors.h>
#include "RCallback.h"
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.ErrorValue);

#define R_ERRORVALUE_CLASS		"OpenGammaErrorValue"
#define R_ERRORVALUE_ERROR		"code"
#define R_ERRORVALUE_INT		"index"
#define R_ERRORVALUE_STRING		"message"
#define R_ERRORVALUE_TOSTRING	"toString"

SEXP RErrorValue::FromValue (const com_opengamma_language_Value *pValue) {
	if (!pValue) {
		LOGWARN (TEXT ("NULL pointer"));
		return NULL;
	}
	if (!pValue->_errorValue) {
		LOGWARN (TEXT ("NULL error value"));
		return NULL;
	}
	LOGDEBUG (TEXT ("Error ") << *pValue->_errorValue);
	SEXP cls = R_getClassDef (R_ERRORVALUE_CLASS);
	PROTECT (cls);
	SEXP obj = R_do_new_object (cls);
	PROTECT (obj);
	SEXP v = allocVector (INTSXP, 1);
	PROTECT (v);
	*INTEGER(v) = *pValue->_errorValue;
	SEXP f = mkString (R_ERRORVALUE_ERROR);
	PROTECT (f);
	R_do_slot_assign (obj, f, v);
	if (pValue->_intValue) {
		LOGDEBUG (TEXT ("Int ") << *pValue->_intValue);
		v = allocVector (INTSXP, 1);
		PROTECT (v);
		*INTEGER(v) = *pValue->_intValue;
		f = mkString (R_ERRORVALUE_INT);
		PROTECT (f);
		R_do_slot_assign (obj, f, v);
		UNPROTECT (2);
	}
	if (pValue->_stringValue) {
		LOGDEBUG (TEXT ("String ") << pValue->_stringValue);
#ifdef _UNICODE
		char *pszStringValue = WideToAsciiDup (pValue->_stringValue);
		if (pszStringValue) {
			v = mkString (pszStringValue);
			free (pszStringValue);
		} else {
			LOGFATAL (ERR_MEMORY);
		}
#else /* ifdef _UNICODE */
		v = mkString (pValue->_stringValue);
#endif /* ifdef _UNICODE */
		PROTECT (v);
		f = mkString (R_ERRORVALUE_STRING);
		PROTECT (f);
		R_do_slot_assign (obj, f, v);
		UNPROTECT (2);
	}
	TCHAR *psz = CError::ToString (pValue);
	LOGDEBUG (TEXT ("toString ") << psz);
#ifdef _UNICODE
	char *pszAscii = WideToAsciiDup (psz);
	if (pszAscii) {
		v = mkString (pszAscii);
		free (pszAscii);
	} else {
		LOGFATAL (ERR_MEMORY);
	}
#else /* ifdef _UNICODE */
	v = mkString (psz);
#endif /* ifdef _UNICODE */
	PROTECT (v);
	delete psz;
	f = mkString (R_ERRORVALUE_TOSTRING);
	PROTECT (f);
	R_do_slot_assign (obj, f, v);
	UNPROTECT (6);
	return obj;
}
