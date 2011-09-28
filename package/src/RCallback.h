/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_rcallback_h
#define __inc_og_rstats_package_rcallback_h

#define R_TO_STRING_GENERIC		"toString"
#define R_TO_FUDGEMSG_GENERIC	"toFudgeMsg"

class CRCallback {
private:
	SEXP m_envir;
public:
	CRCallback (SEXP envir) { m_envir = envir; }
	~CRCallback () { }
	SEXP ToString (SEXP object) const { return InvokeMethod (R_TO_STRING_GENERIC, object); }
	SEXP ToFudgeMsg (SEXP object) const { return InvokeMethod (R_TO_FUDGEMSG_GENERIC, object); }
	SEXP InteropConvert (SEXP value, const TCHAR *pszClass) const;
	SEXP InvokeMethod (const char *pszMethod, SEXP param) const;
	SEXP FromFudgeMsg (FudgeString className, SEXP message) const;
};

#endif /* ifndef __inc_og_rstats_package_rcallback_h */
