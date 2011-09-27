/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_errors_h
#define __inc_og_rstats_package_errors_h

#define ERR_INITIALISATION		"Not initialised"
#define ERR_INVOCATION			"Invocation error"
#define ERR_PARAMETER_TYPE		"Bad parameter type"
#define ERR_PARAMETER_VALUE		"Bad parameter value"
#define ERR_RESULT_TYPE			"Bad result type"
#define ERR_MEMORY				"Out of memory"
#define ERR_INTERNAL			"Internal error"

#undef LOGERROR
#define LOGERROR(e) { \
	LOG4CXX_ERROR (_logger, e); \
	warning ("%s at %s:%d", e, __FILE__, __LINE__); \
}

#undef LOGFATAL
#define LOGFATAL(e) { \
	LOG4CXX_FATAL (_logger, e); \
	error ("%s at %s:%d", e, __FILE__, __LINE__); \
}

#endif /* ifndef __inc_og_rstats_package_errors_h */
