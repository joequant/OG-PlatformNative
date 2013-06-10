/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "StringUtil.h"
#include Client(StringUtil.h)
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.String);

/// Escape a string that contains reserved characters using the first of the reserved characters.
/// This was written as a gsub based operation was very slow when run on R2.14.1 x86_64-redhat-linux-gnu
///
/// @param[in] string the string to escape
/// @param[in] escapeChars the characters to escape, the first is the escape character to use
/// @return the escaped string, possibly the original
SEXP RString::Escape (SEXP string, SEXP escapeChars) {
	SEXP result = R_NilValue;
	if (TYPEOF (string) == STRSXP) {
		if (TYPEOF (escapeChars) == STRSXP) {
			if (length (string) == 1) {
				const char *pszString = CHAR (STRING_ELT (string, 0));
				if (length (escapeChars) == 1) {
					const char *pszEscapeChars = CHAR (STRING_ELT (escapeChars, 0));
					char *pszEscaped = StringEscapeA (pszString, pszEscapeChars, *pszEscapeChars);
					if (pszEscaped) {
						result = mkString (pszEscaped);
						free (pszEscaped);
					} else {
						result = string;
					}
				} else if (length (escapeChars) > 1) {
					result = allocVector (STRSXP, length (escapeChars));
					PROTECT (result);
					int i;
					for (i = 0; i < length (escapeChars); i++) {
						const char *pszEscapeChars = CHAR (STRING_ELT (escapeChars, i));
						char *pszEscaped = StringEscapeA (pszString, pszEscapeChars, *pszEscapeChars);
						if (pszEscaped) {
							SEXP escaped = mkChar (pszEscaped);
							PROTECT (escaped);
							SET_STRING_ELT (result, i, escaped);
							UNPROTECT (1);
						} else {
							SET_STRING_ELT (result, i,STRING_ELT (string, 0));
						}
					}
					UNPROTECT (1);
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
				}
			} else if (length (string) > 1) {
				if (length (escapeChars) == 1) {
					const char *pszEscapeChars = CHAR (STRING_ELT (escapeChars, 0));
					result = allocVector (STRSXP, length (string));
					PROTECT (result);
					int i;
					for (i = 0; i < length (string); i++) {
						const char *pszString = CHAR (STRING_ELT (string, i));
						char *pszEscaped = StringEscapeA (pszString, pszEscapeChars, *pszEscapeChars);
						if (pszEscaped) {
							SEXP escaped = mkChar (pszEscaped);
							PROTECT (escaped);
							SET_STRING_ELT (result, i, escaped);
							UNPROTECT (1);
						} else {
							SET_STRING_ELT (result, i, STRING_ELT (string, i));
						}
					}
					UNPROTECT (1);
				} else if (length (escapeChars) > 1) {
					result = allocVector (STRSXP, length (string) * length (escapeChars));
					PROTECT (result);
					int i, j;
					for (i = 0; i < length (string); i++) {
						const char *pszString = CHAR (STRING_ELT (string, i));
						for (j = 0; j < length (escapeChars); j++) {
							const char *pszEscapeChars = CHAR (STRING_ELT (escapeChars, j));
							char *pszEscaped = StringEscapeA (pszString, pszEscapeChars, *pszEscapeChars);
							if (pszEscaped) {
								SEXP escaped = mkChar (pszEscaped);
								PROTECT (escaped);
								SET_STRING_ELT (result, i * length (escapeChars) + j, escaped);
								UNPROTECT (1);
							} else {
								SET_STRING_ELT (result, i * length (escapeChars) + j, STRING_ELT (string, i));
							}
						}
					}
					UNPROTECT (1);
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
				}
			} else {
				LOGERROR (ERR_PARAMETER_VALUE);
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGERROR (ERR_PARAMETER_TYPE);
	}
	return result;
}
