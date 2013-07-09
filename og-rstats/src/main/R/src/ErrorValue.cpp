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

#define R_ERRORVALUE_CLASS		"ErrorValue"
#define R_ERRORVALUE_ERROR		"code"
#define R_ERRORVALUE_INT		"index"
#define R_ERRORVALUE_STRING		"message"
#define R_ERRORVALUE_TOSTRING	"toString"

SEXP RErrorValue::FromValue (const com_opengamma_language_Value *pValue) {
	if (!pValue) {
		LOGWARN (TEXT ("NULL pointer"));
		return R_NilValue;
	}
	if (!pValue->_errorValue) {
		LOGWARN (TEXT ("NULL error value"));
		return R_NilValue;
	}
	LOGDEBUG (TEXT ("Error ") << *pValue->_errorValue);
	SEXP cls = R_getClassDef (R_ERRORVALUE_CLASS);
	if (cls == R_NilValue) {
		LOGFATAL (ERR_R_FUNCTION);
		return R_NilValue;
	}
	PROTECT (cls);
	SEXP obj = R_do_new_object (cls);
	if (obj == R_NilValue) {
		LOGFATAL (ERR_R_FUNCTION);
		UNPROTECT (1);
		return R_NilValue;
	}
	PROTECT (obj);
	SEXP v = allocVector (INTSXP, 1);
	if (v == R_NilValue) {
		LOGFATAL (ERR_R_FUNCTION)
		UNPROTECT (2);
		return R_NilValue;
	}
	PROTECT (v);
	*INTEGER(v) = *pValue->_errorValue;
	SEXP f = mkString (R_ERRORVALUE_ERROR);
	if (f == R_NilValue) {
		LOGFATAL (ERR_R_FUNCTION);
		UNPROTECT (3);
		return R_NilValue;
	}
	PROTECT (f);
	R_do_slot_assign (obj, f, v);
	if (pValue->_intValue) {
		LOGDEBUG (TEXT ("Int ") << *pValue->_intValue);
		v = allocVector (INTSXP, 1);
		if (v != R_NilValue) {
			PROTECT (v);
			*INTEGER(v) = *pValue->_intValue;
			f = mkString (R_ERRORVALUE_INT);
			if (f && (f != R_NilValue)) {
				PROTECT (f);
				R_do_slot_assign (obj, f, v);
				UNPROTECT (2);
			} else {
				LOGERROR (ERR_R_FUNCTION);
				UNPROTECT (1);
			}
		} else {
			LOGERROR (ERR_R_FUNCTION);
		}
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
			return R_NilValue;
		}
#else /* ifdef _UNICODE */
		v = mkString (pValue->_stringValue);
#endif /* ifdef _UNICODE */
		if (v != R_NilValue) {
			PROTECT (v);
			f = mkString (R_ERRORVALUE_STRING);
			if (f != R_NilValue) {
				PROTECT (f);
				R_do_slot_assign (obj, f, v);
				UNPROTECT (2);
			} else {
				LOGERROR (ERR_R_FUNCTION);
			}
		} else {
			LOGERROR (ERR_R_FUNCTION);
		}
	}
	TCHAR *psz = CError::ToString (pValue);
	if (!psz) {
		LOGFATAL (ERR_MEMORY);
		UNPROTECT (4);
		return R_NilValue;
	}
	LOGDEBUG (TEXT ("toString ") << psz);
#ifdef _UNICODE
	char *pszAscii = WideToAsciiDup (psz);
	if (pszAscii) {
		v = mkString (pszAscii);
		free (pszAscii);
	} else {
		LOGFATAL (ERR_MEMORY);
		UNPROTECT (4);
		return R_NilValue;
	}
#else /* ifdef _UNICODE */
	v = mkString (psz);
#endif /* ifdef _UNICODE */
	delete psz;
	if (v != R_NilValue) {
		PROTECT (v);
		f = mkString (R_ERRORVALUE_TOSTRING);
		if (f != R_NilValue) {
			PROTECT (f);
			R_do_slot_assign (obj, f, v);
			UNPROTECT (6);
		} else {
			LOGERROR (ERR_R_FUNCTION);
			UNPROTECT (5);
		}
	} else {
		LOGERROR (ERR_R_FUNCTION);
		UNPROTECT (4);
	}
	return obj;
}
