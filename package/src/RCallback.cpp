/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "RCallback.h"

LOGGING (com.opengamma.rstats.package.RCallback);

SEXP CRCallback::InvokeGeneric (SEXP object, const char *pszGeneric) {
	TODO ("Invoke " << pszGeneric << " on object");
	// TODO: Note that this should never fail, but return a R_NilValue if there is a problem
	return R_NilValue;
}
