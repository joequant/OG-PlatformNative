/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(StringUtil.h)

LOGGING (com.opengamma.rstats.client.StringUtil);

/// Escapes the string by inserting a special character before the reserved characters.
/// A new string is allocated to contain the escaped version. The caller must release this
/// memory when done. If the string does not contain any of the reserved characters, no
/// allocation is performed and the function returns NULL.
///
/// @param[in] pszString the string to escape, never NULL
/// @param[in] pszChars the reserved characters, never NULL
/// @param[in] cEscape the escape character to use
/// @return the escaped string or NULL if no escaping was necessary
char *StringEscapeA (const char *pszString, const char *pszChars, char cEscape) {
	LOGDEBUG ("Escaping " << pszString << " with chars " << pszChars << " using " << cEscape);
	const char *pszStringItr = pszString;
	int nEscape = 0, nChars = 0;
	while (*pszStringItr) {
		if (strchr (pszChars, *(pszStringItr++)) != NULL) {
			nEscape++;
		}
		nChars++;
	}
	if (!nEscape) {
		LOGDEBUG (TEXT ("No escape needed"));
		return NULL;
	}
	char *pszResult = (char*)malloc (nChars + nEscape + 1);
	if (!pszResult) {
		LOGFATAL (TEXT ("Out of memory"));
		return NULL;
	}
	char *pszResultItr = pszResult;
	pszStringItr = pszString;
	while (*pszStringItr) {
		char c = *(pszStringItr++);
		if (strchr (pszChars, c) != NULL) {
			*(pszResultItr++) = cEscape;
		}
		*(pszResultItr++) = c;
	}
	*pszResultItr = 0;
	LOGDEBUG ("Escaped string " << pszResult);
	return pszResult;
}
