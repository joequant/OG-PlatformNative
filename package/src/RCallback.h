/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_rcallback_h
#define __inc_og_rstats_package_rcallback_h

class CRCallback {
private:
	CRCallback () { }
	~CRCallback () { }
	static SEXP InvokeGeneric (SEXP object, const char *pszGeneric);
public:
	static SEXP ToString (SEXP object) { return InvokeGeneric (object, "toString"); }
	static SEXP ToFudgeMsg (SEXP object) { return InvokeGeneric (object, "toFudgeMsg"); }
};

#endif /* ifndef __inc_og_rstats_package_rcallback_h */
