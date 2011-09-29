/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "RCallback.h"
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.RCallback);

#define R_FROM_FUDGEMSG_GENERIC		"fromFudgeMsg"
#define R_EXISTS_FUNCTION			"existsFunction"
#define R_INTEROP_GENERIC			"Interop"

#ifdef _WIN32

/// Key value in the string map. Strings are held in their raw UTF-8 byte encoding rather than
/// unpacked into a platform string.
struct _key {

	/// Bytes of the key
	const void *pKey;

	/// Number of bytes in the key
	size_t cbKey;

};

/// Hashes a key value
class key_hasher : public stdext::hash_compare<const struct _key> {
public:
	
	/// Hashes the key
	///
	/// @param[in] key key to hash
	/// @retturn the hashed value
	size_t operator () (const struct _key &key) const {
		size_t v = 1;
		size_t i, j = key.cbKey / sizeof (size_t);
		const size_t *pKey = (const size_t*)key.pKey;
		for (i = 0; i < j; i++) {
			v += (v << 4) + *(pKey++);
		}
		return v;
	}

	/// Compares two keys
	///
	/// @param[in] a first key
	/// @param[in] b second key
	/// @return true if the first key is strictly less than the second
	bool operator () (const struct _key &a, const struct _key &b) const {
		if (a.cbKey < b.cbKey) {
			return true;
		} else if (a.cbKey == b.cbKey) {
			return memcmp (a.pKey, b.pKey, a.cbKey) < 0;
		} else {
			return false;
		}
	}

};

/// Map of strings to the fromFudgeMsg function names (or the empty string if there is no function
/// defined).
typedef stdext::hash_map<struct _key, char *, key_hasher> TStringMap;

#endif /* ifdef _WIN32 */

/// Cached mapping of UTF-8 class name strings to the fromFudgeMsg functions.
class CFromFudgeMsgCache {
private:

#ifdef _WIN32

	/// Underlying hash map
	TStringMap m_map;

	/// Copies a block of memory
	///
	/// @param[in] pData memory to copy
	/// @param[in] cbData bytes to copy
	/// @return the new allocation
	static void *Copy (const void *pData, size_t cbData) {
		void *p = new BYTE[cbData];
		if (!p) {
			LOGFATAL (TEXT ("Out of memory"));
			return NULL;
		} else {
			memcpy (p, pData, cbData);
			return p;
		}
	}

#else /* ifdef _WIN32 */

	/// Allocation pool for the hash map
	apr_pool_t *m_pPool;

	/// Hash map
	apr_hash_t *m_pHash;

#endif /* ifdef _WIN32 */
	
	/// Mutex to guard concurrent access to the map
	CMutex m_oMutex;
public:

	/// Creates a new cache instance.
	CFromFudgeMsgCache () {
#ifndef _WIN32
		m_pPool = NULL;
		apr_pool_create_core (&m_pPool);
		m_pHash = apr_hash_make (m_pPool);
#endif /* ifndef _WIN32 */
	}

	/// Destroys the cache instance.
	~CFromFudgeMsgCache () {
#ifndef _WIN32
		apr_pool_destroy (m_pPool);
#endif /* ifndef _WIN32 */
	}

	/// Fetches a function name from the cache, creating a function name the first time a
	/// new class name is encountered and the function is defined. Empty strings are
	/// put into the map for functions that are not defined to speed future lookups.
	///
	/// @param[in] className class name to search for
	/// @return the function name, or NULL if there is none
	const char *Get (FudgeString className, const CRCallback *poR) {
#ifdef _WIN32
		struct _key key;
		key.pKey = FudgeString_getData (className);
		key.cbKey = FudgeString_getSize (className);
#else /* ifdef _WIN32 */
		const void *pKey = FudgeString_getData (className);
		size_t cbKey = FudgeString_getSize (className);
#endif /* ifdef _WIN32 */
		m_oMutex.Enter ();
#ifdef _WIN32
		TStringMap::iterator itr = m_map.find (key);
		const char *pszValue;
		if (itr == m_map.end ()) {
#else /* ifdef _WIN32 */
		const char *pszValue = (const char*)apr_hash_get (m_pHash, pKey, cbKey);
		if (!pszValue) {
#endif /* ifdef _WIN32 */
			char *pszClassName;
			if (FudgeString_convertToASCIIZ (&pszClassName, className) == FUDGE_OK) {
				LOGDEBUG ("Creating fromFudgeMsg function for " << pszClassName);
				char *pszSimpleName = strrchr (pszClassName, '.');
				if (!pszSimpleName) {
					pszSimpleName = pszClassName;
				} else {
					pszSimpleName++;
				}
				char *pchSimpleName = pszSimpleName;
				size_t cchMethod = 14;
				while (*pchSimpleName) {
					if (*pchSimpleName == '$') {
						*pchSimpleName = '_';
					}
					pchSimpleName++;
					cchMethod++;
				}
				char *pszMethod = new char[cchMethod];
				if (pszMethod) {
					StringCbPrintfA (pszMethod, cchMethod * sizeof (char), R_FROM_FUDGEMSG_GENERIC ".%s", pszSimpleName);
					SEXP method = mkString (pszMethod);
					PROTECT (method);
					SEXP exists = poR->InvokeMethod (R_EXISTS_FUNCTION, method);
					UNPROTECT (1);
#ifdef _WIN32
					key.pKey = Copy (key.pKey, key.cbKey);
#endif /* ifdef _WIN32 */
					if ((TYPEOF (exists) == LGLSXP) && *INTEGER (exists)) {
						LOGINFO ("Found function " << pszMethod);
#ifdef _WIN32
						m_map.insert (TStringMap::value_type (key, pszMethod));
#else /* ifdef _WIN32 */
						apr_hash_set (m_pHash, pKey, cbKey, pszMethod);
#endif /* ifdef _WIN32 */
						pszValue = pszMethod;
					} else {
						LOGINFO ("No function " << pszMethod);
#ifdef _WIN32
						m_map.insert (TStringMap::value_type (key, (char*)""));
						pszValue = NULL;
#else /* ifdef _WIN32 */
						apr_hash_set (m_pHash, pKey, cbKey, "");
#endif /* ifdef _WIN32 */
						free (pszMethod);
					}
				} else {
					LOGFATAL (ERR_MEMORY);
#ifdef _WIN32
					pszValue = NULL;
#endif /* ifdef _WIN32 */
				}
				free (pszClassName);
			} else {
				LOGERROR (ERR_INTERNAL);
#ifdef _WIN32
				pszValue = NULL;
#endif /* ifdef _WIN32 */
			}
		}
#ifdef _WIN32
		else pszValue = itr->second;
#endif /* ifdef _WIN32 */
		m_oMutex.Leave ();
		if (pszValue) {
			if (*pszValue) {
				return pszValue;
			} else {
				LOGDEBUG (TEXT ("Returning cached method failure"));
				return NULL;
			}
		} else {
			// Already logged the error
			return NULL;
		}
	}

};

/// Global cache of conversion function names.
static CFromFudgeMsgCache g_oFromFudgeMsgCache;

/// Invokes a function that takes a single parameter and returns a result.
///
/// @param[in] pszMethod name of the function
/// @param[in] value parameter value
/// @return result of the function, or R_NilValue if there was a problem
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

/// Invokes the interop.<Class> method for the class. The class should be in R namespace
/// form (e.g. ViewClient) and not the fully qualified Java one.
///
/// @param[in] value value to convert
/// @param[in] pszClass name of the class
/// @return the converted value
SEXP CRCallback::InteropConvert (SEXP value, const TCHAR *pszClass) const {
#ifdef _UNICODE
	const char *pszClassAscii = WideToAsciiDup (pszClass);
	if (!pszClassAscii) {
		LOGFATAL (ERR_MEMORY);
		return R_NilValue;
	}
#else /* ifdef _UNICODE */
#define pszClassAscii pszClass
#endif /* ifdef _UNICODE */
	char szFunction[256];
	StringCbPrintfA (szFunction, sizeof (szFunction), R_INTEROP_GENERIC ".%s", pszClassAscii);
	SEXP result = InvokeMethod (szFunction, value);
#ifdef _UNICODE
	free (pszClassAscii);
#endif /* ifdef _UNICODE */
	return result;
}

/// Invokes the fromFudgeMsg.<Class> method to unpack a Fudge encoded object into a rich
/// R object. If no function is defined, the original message value is returned.
///
/// @param[in] className name of the class to convert to (qualified Java name)
/// @param[in] message message value to convert
/// @return the converted value or the original message if there is no conversion
SEXP CRCallback::FromFudgeMsg (FudgeString className, SEXP message) const {
	const char *pszMethod = g_oFromFudgeMsgCache.Get (className, this);
	if (pszMethod) {
		SEXP result = InvokeMethod (pszMethod, message);
		if (result != R_NilValue) {
			return result;
		} else {
			return message;
		}
	} else {
		return message;
	}
}
