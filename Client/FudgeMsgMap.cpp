/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(FudgeMsgMap.h)

LOGGING (com.opengamma.rstats.client.FudgeMsgMap);

#ifdef _WIN32

/// Hashes a FudgeMsg
class FudgeMsg_hasher : public stdext::hash_compare<FudgeMsg> {
private:

	/// Hashes an arbitrary block of memory
	///
	/// @param[in] pData data to hash
	/// @param[in] cbData length of data to hash in bytes
	/// @return the hashed value
	static size_t hash (const void *pData, size_t cbData) {
		size_t hc = 1;
		const char *ptr = (const char*)pData;
		while (cbData-- > 0) {
			hc += (hc << 4) + *(ptr++);
		}
		return hc;
	}

	/// Hashes a Fudge string
	///
	/// @param[in] string string to hash
	/// @return the hashed value
	static size_t hash (FudgeString string) {
		return hash (FudgeString_getData (string), FudgeString_getSize (string));
	}

	/// Hashes the message
	///
	/// @param[in] msg the message to hash
	/// @return the hashed value
	static size_t hash (const FudgeMsg msg) {
		size_t hc = 1;
		int n = 0;
		FudgeField field;
		while (FudgeMsg_getFieldAtIndex (&field, msg, n++) == FUDGE_OK) {
			hc += (hc << 4) + field.ordinal;
			if (field.name) {
				hc += hash (field.name);
			}
			switch (field.type) {
				case FUDGE_TYPE_INDICATOR :
					hc++;
					break;
				case FUDGE_TYPE_BOOLEAN :
					hc += field.data.boolean ? 1 : 0;
					break;
				case FUDGE_TYPE_BYTE :
					hc += field.data.byte;
					break;
				case FUDGE_TYPE_SHORT :
					hc += field.data.i16;
					break;
				case FUDGE_TYPE_INT :
					hc += field.data.i32;
					break;
				case FUDGE_TYPE_LONG :
					hc += (size_t)field.data.i64;
					break;
				case FUDGE_TYPE_FLOAT :
					hc += (size_t)field.data.f32;
					break;
				case FUDGE_TYPE_DOUBLE :
					hc += (size_t)field.data.f64;
					break;
				case FUDGE_TYPE_DATE :
					hc += hash (&field.data.datetime.date, sizeof (FudgeDate));
					break;
				case FUDGE_TYPE_TIME :
					hc += hash (&field.data.datetime.time, sizeof (FudgeTime));
					break;
				case FUDGE_TYPE_DATETIME :
					hc += hash (&field.data.datetime, sizeof (FudgeDateTime));
					break;
				case FUDGE_TYPE_STRING :
					hc += hash (field.data.string);
					break;
				case FUDGE_TYPE_FUDGE_MSG :
					hc += hash (field.data.message);
					break;
				default :
					hc += hash (field.data.bytes, field.numbytes);
					break;
			}
		}
		return hc;
	}

	/// Compares two Fudge message
	///
	/// @param[in] a first message
	/// @param[in] b second message
	/// @return negative if the first message is less, positive if greater, 0 if equal
	static int compare (const FudgeMsg a, const FudgeMsg b) {
		int n = 0, c;
		FudgeField fA, fB;
		c = FudgeMsg_numFields (a) - FudgeMsg_numFields (b);
		if (c) return c;
		while ((FudgeMsg_getFieldAtIndex (&fA, a, n) == FUDGE_OK)
			&& (FudgeMsg_getFieldAtIndex (&fB, b, n) == FUDGE_OK)) {
			if (fA.type < fB.type) return -1;
			if (fA.type > fB.type) return 1;
			if (fA.ordinal < fB.ordinal) return -1;
			if (fA.ordinal > fB.ordinal) return 1;
			if (fA.name) {
				if (fB.name) {
					c = FudgeString_compare (fA.name, fB.name);
					if (c) return c;
				} else {
					return -1;
				}
			} else {
				if (fB.name) return 1;
			}
			switch (fA.type) {
				case FUDGE_TYPE_INDICATOR :
					c = 0;
					break;
				case FUDGE_TYPE_BOOLEAN :
					c = fA.data.boolean - fB.data.boolean;
					break;
				case FUDGE_TYPE_BYTE :
					c = fA.data.byte - fB.data.byte;
					break;
				case FUDGE_TYPE_SHORT :
					c = fA.data.i16 - fB.data.i16;
					break;
				case FUDGE_TYPE_INT :
					c = fA.data.i32 - fB.data.i32;
					break;
				case FUDGE_TYPE_LONG :
					if (fA.data.i64 < fB.data.i64) {
						c = -1;
					} else if (fA.data.i64 > fB.data.i64) {
						c = 1;
					} else {
						c = 0;
					}
					break;
				case FUDGE_TYPE_FLOAT :
					if (fA.data.f32 < fB.data.f32) {
						c = -1;
					} else if (fA.data.f32 > fB.data.f32) {
						c = 1;
					} else {
						c = 0;
					}
					break;
				case FUDGE_TYPE_DOUBLE :
					if (fA.data.f64 < fB.data.f64) {
						c = -1;
					} else if (fA.data.f64 > fB.data.f64) {
						c = 1;
					} else {
						c = 0;
					}
					break;
				case FUDGE_TYPE_DATE :
					c = memcmp (&fA.data.datetime.date, &fB.data.datetime.date, sizeof (FudgeDate));
					break;
				case FUDGE_TYPE_TIME :
					c = memcmp (&fA.data.datetime.time, &fB.data.datetime.time, sizeof (FudgeTime));
					break;
				case FUDGE_TYPE_DATETIME :
					c = memcmp (&fA.data.datetime, &fB.data.datetime, sizeof (FudgeDateTime));
					break;
				case FUDGE_TYPE_STRING :
					c = FudgeString_compare (fA.data.string, fB.data.string);
					break;
				case FUDGE_TYPE_FUDGE_MSG :
					c = compare (fA.data.message, fB.data.message);
					break;
				default :
					c = fA.numbytes - fB.numbytes;
					if (!c) {
						c = memcmp (fA.data.bytes, fB.data.bytes, fA.numbytes);
					}
					break;
			}
			if (c) return c;
			n++;
		}
		return 0;
	}

public:

	/// Hashes the message
	///
	/// @param[in] msg the message to hash
	/// @return the hashed value
	size_t operator () (const FudgeMsg &msg) const {
		return hash (msg);
	}

	/// Compares two Fudge messages
	///
	/// @param[in] a first message
	/// @param[in] b second message
	/// @return true if the first message is strictly less than the second
	bool operator () (const FudgeMsg &a, const FudgeMsg &b) const {
		return compare (a, b) < 0;
	}

};

typedef stdext::hash_map<FudgeMsg, CFudgeMsgInfo *, FudgeMsg_hasher> TFudgeMsgMap;

#endif /* ifdef _WIN32 */

/// The underlying map implementation.
class CMap {
private:
	
	/// Mutex to protect the map and objects within it.
	CMutex m_oMutex;

#ifdef _WIN32

	/// Underlying hash map.
	TFudgeMsgMap m_oMap;

#else /* ifdef _WIN32 */

	/// Allocation pool for the hash map
	apr_pool_t *m_pPool;

	/// Underlying hash map
	apr_hash_t *m_pHash;

#endif /* ifdef _WIN32 */

public:
	
	/// Creates and initialises the map structures.
	CMap () {
#ifndef _WIN32
		m_pPool = NULL;
		apr_pool_create_core (&m_pPool);
		m_pHash = apr_hash_make (m_pPool);
#endif /* ifndef _WIN32 */
	}

	/// Destroys the map structures.
	~CMap () {
		// TODO: should really destroy the objects in the map
#ifndef _WIN32
		apr_pool_destroy (m_pPool);
#endif /* ifndef _WIN32 */
	}

	/// Enters the mutex.
	void EnterMutex () {
		m_oMutex.Enter ();
	}

	/// Leaves the mutex.
	void LeaveMutex () {
		m_oMutex.Leave ();
	}

#ifdef _WIN32

	/// Fetch a message from the map. The caller must hold the mutex.
	///
	/// @param[in] msg the message to search for
	/// @return the message structure, or NULL if none is found
	CFudgeMsgInfo *Get (FudgeMsg msg) const {
		TFudgeMsgMap::const_iterator itr = m_oMap.find (msg);
		if (itr == m_oMap.end ()) {
			return NULL;
		} else {
			return itr->second;
		}
	}

#else /* ifdef _WIN32 */

	/// Fetch a message from the map. The caller must hold the mutex.
	///
	/// @param[in] pData the binary encoding of the message
	/// @param[in] cbData the length of the binary encoding
	/// @return the message structure, or NULL if none is found
	CFudgeMsgInfo *Get (const void *pData, size_t cbData) const {
		return (CFudgeMsgInfo*)apr_hash_get (m_pHash, pData, cbData);
	}

#endif /* ifdef _WIN32 */

	/// Stores a message in the map. The caller must hold the mutex.
	void Put (CFudgeMsgInfo *poMessage) {
#ifdef _WIN32
		m_oMap.insert (TFudgeMsgMap::value_type (poMessage->GetMessage (), poMessage));
#else /* ifdef _WIN32 */
		apr_hash_set (m_pHash, poMessage->GetData (), poMessage->GetLength (), poMessage);
#endif /* ifdef _WIN32 */
	}

	/// Removes a message from the map. The caller must hold the mutex.
	void Remove (CFudgeMsgInfo *poMessage) {
#ifdef _WIN32
		TFudgeMsgMap::iterator itr = m_oMap.find (poMessage->GetMessage ());
		if (itr != m_oMap.end ()) {
			m_oMap.erase (itr);
		}
#else /* ifdef _WIN32 */
		apr_hash_set (m_pHash, poMessage->GetData (), poMessage->GetLength (), NULL);
#endif /* ifdef _WIN32 */
	}

};

/// The map implementation instance.
static CMap g_oMap;

/// Creates a new message entry. The initial reference count is 1.
///
/// @param[in] msg the message represented
/// @param[in] pData the binary encoding of the message - the pointer will be retained by the new object
/// @param[in] cbData the length of the binary encoding
CFudgeMsgInfo::CFudgeMsgInfo (FudgeMsg msg, void *pData, size_t cbData) {
	m_nRefCount = 1;
	m_msg = msg;
	FudgeMsg_retain (msg);
	m_pData = pData;
	m_cbData = cbData;
}

/// Destroys the message entry.
CFudgeMsgInfo::~CFudgeMsgInfo () {
	assert (m_nRefCount == 0);
	FudgeMsg_release (m_msg);
	free (m_pData);
}

/// Increments the R reference count.
void CFudgeMsgInfo::Retain () {
	g_oMap.EnterMutex ();
	m_nRefCount++;
	g_oMap.LeaveMutex ();
}

/// Decrements the R reference count, destroying the object when the count reaches zero.
void CFudgeMsgInfo::Release (CFudgeMsgInfo *poMessage) {
	g_oMap.EnterMutex ();
	if (--poMessage->m_nRefCount == 0) {
		g_oMap.Remove (poMessage);
		delete poMessage;
	}
	g_oMap.LeaveMutex ();
}

/// Creates a binary encoding of a Fudge message.
///
/// @param[in] msg the message to encode
/// @param[out] pcbData receives the length of the allocated encoding
/// @return the encoded form
static void *_EncodeFudgeMsg (FudgeMsg msg, size_t *pcbData) {
	FudgeStatus status;
	FudgeMsgEnvelope env;
	void *pData;
	if ((status = FudgeMsgEnvelope_create (&env, 0, 0, 0, msg)) != FUDGE_OK) {
		LOGWARN ("Couldn't create message envelope, error " << FudgeStatus_strerror (status));
		return NULL;
	}
	fudge_i32 cbData;
	status = FudgeCodec_encodeMsg (env, (fudge_byte**)&pData, &cbData);
	FudgeMsgEnvelope_release (env);
	if (status != FUDGE_OK) {
		LOGWARN ("Couldn't encode Fudge message, error " << FudgeStatus_strerror (status));
		return NULL;
	}
	*pcbData = cbData;
	return pData;
}

/// Creates a Fudge message from a binary encoding.
///
/// @param[in] pData the encoded message data
/// @param[in] cbData the length of the encoded data in bytes
/// @return the message, or NULL if there is a problem
static FudgeMsg _DecodeFudgeMsg (const void *pData, size_t cbData) {
	FudgeMsgEnvelope env;
	FudgeMsg msg = NULL;
	if (FudgeCodec_decodeMsg (&env, (fudge_byte*)pData, cbData) == FUDGE_OK) {
		msg = FudgeMsgEnvelope_getMessage (env);
		FudgeMsg_retain (msg);
		FudgeMsgEnvelope_release (env);
	} else {
		LOGWARN (TEXT ("Couldn't decode Fudge message"));
	}
	return msg;
}

/// Finds an existing message entry in the map, or creates a new entry if none is found.
/// The message is returned with an incremented reference count - the caller should call
/// Release when finished with it (unless the reference is offloaded to R).
///
/// @param[in] the message to look up
/// @return the message entry
CFudgeMsgInfo *CFudgeMsgInfo::GetMessage (FudgeMsg msg) {
	g_oMap.EnterMutex ();
#ifdef _WIN32
	CFudgeMsgInfo *poMessage = g_oMap.Get (msg);
#else /* ifdef _WIN32 */
	size_t cbData;
	void *pData = _EncodeFudgeMsg (msg, &cbData);
	if (!pData) {
		g_oMap.LeaveMutex ();
		return NULL;
	}
	CFudgeMsgInfo *poMessage = g_oMap.Get (pData, cbData);
#endif /* ifdef _WIN32 */
	if (poMessage) {
		poMessage->m_nRefCount++;
		g_oMap.LeaveMutex ();
#ifndef _WIN32
		free (pData);
#endif /* ifndef _WIN32 */
		return poMessage;
	}
#ifdef _WIN32
	size_t cbData;
	void *pData = _EncodeFudgeMsg (msg, &cbData);
	if (!pData) {
		g_oMap.LeaveMutex ();
		return NULL;
	}
#endif /* ifdef _WIN32 */
	poMessage = new CFudgeMsgInfo (msg, pData, cbData);
	if (poMessage) {
		g_oMap.Put (poMessage);
	} else {
		LOGFATAL (TEXT ("Out of memory"));
		free (pData);
	}
	g_oMap.LeaveMutex ();
	return poMessage;
}

/// Finds an existing message entry in the map, or creates a new entry if none is found.
/// The message is returned with an incremented reference count - the caller should call
/// Release when finished with it (unless the reference is offloaded to R).
///
/// @param[in] pData the binary encoding of the message to lock up
/// @param[in] cbData the length of the binary encoding in bytes
CFudgeMsgInfo *CFudgeMsgInfo::GetMessage (const void *pData, size_t cbData) {
	g_oMap.EnterMutex ();
	FudgeMsg msg = NULL;
	CFudgeMsgInfo *poMessage = NULL;
	void *pDataCopy = NULL;
	do {
#ifdef _WIN32
		msg = _DecodeFudgeMsg (pData, cbData);
		if (!msg) {
			break;
		}
		poMessage = g_oMap.Get (msg);
#else /* ifdef _WIN32 */
		poMessage = g_oMap.Get (pData, cbData);
#endif /* ifdef _WIN32 */
		if (poMessage) {
			poMessage->m_nRefCount++;
			break;
		}
		pDataCopy = malloc (cbData);
		if (!pDataCopy) {
			LOGFATAL (TEXT ("Out of memory"));
			break;
		}
		memcpy (pDataCopy, pData, cbData);
#ifndef _WIN32
		msg = _DecodeFudgeMsg (pDataCopy, cbData);
		if (!msg) {
			break;
		}
#endif /* ifndef _WIN32 */
		poMessage = new CFudgeMsgInfo (msg, pDataCopy, cbData);
		if (!poMessage) {
			LOGFATAL (TEXT ("Out of memory"));
		}
		pDataCopy = NULL;
	} while (false);
	g_oMap.LeaveMutex ();
	if (msg) FudgeMsg_release (msg);
	if (pDataCopy) free (pDataCopy);
	return poMessage;
}
