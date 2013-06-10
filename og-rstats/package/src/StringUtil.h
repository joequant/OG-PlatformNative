/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_stringutil_h
#define __inc_og_rstats_package_stringutil_h

/// R method helper for strings.
class RString {
private:
	RString () { }
	~RString () { }
public:
	static SEXP Escape (SEXP string, SEXP escapeChars);
};

#ifdef GLOBALS
extern "C" {

	SEXP RPROC String_escape2 (SEXP string, SEXP escapeChars) {
		return RString::Escape (string, escapeChars);
	}

}
#endif /* ifdef GLOBALS */

#endif /* ifndef __inc_og_rstats_package_stringutil_h */
