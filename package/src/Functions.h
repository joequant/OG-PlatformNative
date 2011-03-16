/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_package_functions_h
#define __inc_og_pirate_package_functions_h

extern "C" {

	SEXP RPROC Functions_count0 ();
	SEXP RPROC Functions_getName1 (SEXP index);
	SEXP RPROC Functions_getParameterFlags1 (SEXP index);
	SEXP RPROC Functions_getParameterNames1 (SEXP index);
	SEXP RPROC Functions_invoke2 (SEXP index, SEXP args);

}

#endif /* ifndef __inc_og_pirate_package_functions_h */
