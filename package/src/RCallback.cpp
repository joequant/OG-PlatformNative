/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "RCallback.h"

LOGGING (com.opengamma.rstats.package.RCallback);

SEXP CRCallback::InvokeMethod (const char *pszMethod, SEXP value) const {
	LOGINFO ("Invoke " << pszMethod << " on value");
	SEXP evalExpr;
	evalExpr = allocList (2);
	PROTECT (evalExpr);
	SET_TYPEOF (evalExpr, LANGSXP);
	SETCAR (evalExpr, install (pszMethod));
	SETCAR (CDR (evalExpr), value);
	value = eval (evalExpr, m_envir);
	UNPROTECT (1);
	return value;
}

SEXP CRCallback::InteropConvert (SEXP value, const TCHAR *pszClass) const {
#ifdef _UNICODE
	const char *pszClassAscii = WideToAsciiDup (pszClass);
	if (!pszClassAscii) {
		LOGFATAL (TEXT ("Out of memory"));
		return R_NilValue;
	}
#else /* ifdef _UNICODE */
#define pszClassAscii pszClass
#endif /* ifdef _UNICODE */
	char szFunction[256];
	StringCbPrintfA (szFunction, sizeof (szFunction), "interop.%s", pszClassAscii);
	SEXP result = InvokeMethod (szFunction, value);
#ifdef _UNICODE
	free (pszClassAscii);
#endif /* ifdef _UNICODE */
	return result;
}
