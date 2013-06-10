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
public:

	/// Hashes the message
	///
	/// @param[in] msg the message to hash
	/// @return the hashed value
	size_t operator () (const FudgeMsg &msg) const {
		return FudgeMsg_hash (msg);
	}

	/// Compares two Fudge messages
	///
	/// @param[in] a first message
	/// @param[in] b second message
	/// @return true if the first message is strictly less than the second
	bool operator () (const FudgeMsg &a, const FudgeMsg &b) const {
		return FudgeMsg_compare (a, b) < 0;
	}

};

typedef stdext::hash_map<FudgeMsg, CFudgeMsgInfo *, FudgeMsg_hasher> TFudgeMsgMap;

#else /* ifdef _WIN32 */

/// Hashes a FudgeMsg
typedef struct {
	long operator () (const FudgeMsg &msg) const {
		return FudgeMsg_hash (msg);
	}
} FudgeMsg_hasher;

/// Compares two Fudge messages for equality
typedef struct {
	bool operator () (const FudgeMsg &a, const FudgeMsg &b) const {
		return FudgeMsg_compare (a, b) == 0;
	}
} FudgeMsg_comparator;

typedef std::tr1::unordered_map<FudgeMsg, CFudgeMsgInfo *, FudgeMsg_hasher, FudgeMsg_comparator> TFudgeMsgMap;

#endif /* ifdef _WIN32 */

/// Mutex to protect the map and objects within it.
static CMutex g_oMutex;

/// Underlying hash map containing the already seen/referenced Fudge messages.
static TFudgeMsgMap g_oMap;

/// Count of the total number of bytes allocated and used in the map. This is based on the length of the
/// Fudge messages plus 32 bytes of overhead for each.
static volatile size_t g_cbData;

#define MESSAGE_INFO_OVERHEAD	(16 * sizeof (void *))

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
	if (m_pData) {
		free (m_pData);
		g_cbData -= m_cbData;
	}
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

/// Returns the binary encoding of the message.
///
/// @return the encoding
const void *CFudgeMsgInfo::GetData () {
	if (!m_pData) {
		g_oMutex.Enter ();
		if (!m_pData) {
			m_pData = _EncodeFudgeMsg (m_msg, &m_cbData);
		}
		g_oMutex.Leave ();
	}
	return m_pData;
}

/// Returns the length of the binary encoding of the message in bytes.
///
/// @return the length if bytes
size_t CFudgeMsgInfo::GetLength () {
	if (!m_pData) {
		g_oMutex.Enter ();
		if (!m_pData) {
			m_pData = _EncodeFudgeMsg (m_msg, &m_cbData);
		}
		g_oMutex.Leave ();
	}
	return m_cbData;
}

/// Increments the R reference count.
void CFudgeMsgInfo::Retain () {
	g_oMutex.Enter ();
	m_nRefCount++;
	g_oMutex.Leave ();
}

/// Decrements the R reference count, destroying the object when the count reaches zero.
void CFudgeMsgInfo::Release (CFudgeMsgInfo *poMessage) {
	g_oMutex.Enter ();
	LOGDEBUG (TEXT ("Releasing CFudgeMsgInfo, rc=") << poMessage->m_nRefCount);
	if (--poMessage->m_nRefCount == 0) {
		TFudgeMsgMap::iterator itr = g_oMap.find (poMessage->m_msg);
		if (itr != g_oMap.end ()) {
			LOGDEBUG (TEXT ("Removing message from map (size = ") << (g_oMap.size () - 1) << TEXT (")"));
			g_oMap.erase (itr);
			g_cbData -= MESSAGE_INFO_OVERHEAD;
		}
		delete poMessage;
	}
	g_oMutex.Leave ();
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
	CFudgeMsgInfo *poMessage;
	g_oMutex.Enter ();
	TFudgeMsgMap::const_iterator itr = g_oMap.find (msg);
	if (itr != g_oMap.end ()) {
		poMessage = itr->second;
		poMessage->m_nRefCount++;
		g_oMutex.Leave ();
		return poMessage;
	}
	poMessage = new CFudgeMsgInfo (msg, NULL, 0);
	if (poMessage) {
		LOGDEBUG (TEXT ("Adding message to map (size = ") << (g_oMap.size () + 1) << TEXT (")"));
		g_oMap.insert (TFudgeMsgMap::value_type (msg, poMessage));
		g_cbData += MESSAGE_INFO_OVERHEAD;
	} else {
		LOGFATAL (TEXT ("Out of memory"));
	}
	g_oMutex.Leave ();
	return poMessage;
}

/// Finds an existing message entry in the map, or creates a new entry if none is found.
/// The message is returned with an incremented reference count - the caller should call
/// Release when finished with it (unless the reference is offloaded to R).
///
/// @param[in] pData the binary encoding of the message to lock up
/// @param[in] cbData the length of the binary encoding in bytes
CFudgeMsgInfo *CFudgeMsgInfo::GetMessage (const void *pData, size_t cbData) {
	g_oMutex.Enter ();
	FudgeMsg msg = NULL;
	CFudgeMsgInfo *poMessage = NULL;
	void *pDataCopy = NULL;
	do {
		msg = _DecodeFudgeMsg (pData, cbData);
		if (!msg) {
			break;
		}
		TFudgeMsgMap::const_iterator itr = g_oMap.find (msg);
		if (itr != g_oMap.end ()) {
			poMessage = itr->second;
			poMessage->m_nRefCount++;
			break;
		}
		pDataCopy = malloc (cbData);
		if (!pDataCopy) {
			LOGFATAL (TEXT ("Out of memory"));
			break;
		}
		memcpy (pDataCopy, pData, cbData);
		poMessage = new CFudgeMsgInfo (msg, pDataCopy, cbData);
		if (!poMessage) {
			LOGFATAL (TEXT ("Out of memory"));
			break;
		}
		LOGDEBUG (TEXT ("Adding message to map (size = ") << (g_oMap.size () + 1) << TEXT (")"));
		g_oMap.insert (TFudgeMsgMap::value_type (msg, poMessage));
		g_cbData += cbData + MESSAGE_INFO_OVERHEAD;
		pDataCopy = NULL;
	} while (false);
	g_oMutex.Leave ();
	if (msg) FudgeMsg_release (msg);
	if (pDataCopy) free (pDataCopy);
	return poMessage;
}

/// Returns an approximation of the amount of memory currently used by the Fudge message map.
///
/// This doesn't include the memory actually required for the Fudge message representations. For
/// example if workspace serialisation is enabled then there are byte vectors allocated for each
/// message and held in this map. If workspace serialisation is disabled then the map only
/// contains the Fudge message pointer, so the size returned here is based just on the number of
/// messages. The ACTUAL memory footprint of the Fudge messages in memory will therefore be at
/// least twice this value if serialisation is enabled, and significantly bigger if serialisation
/// is disabled (as this value will be far too low).
///
/// @return an approximate number of bytes
size_t CFudgeMsgInfo::GetBufferSize () {
	return g_cbData;
}

/// Returns the number of messages currently in the buffer.
///
/// @return the number of messages
size_t CFudgeMsgInfo::GetBufferCount () {
	g_oMutex.Enter ();
	size_t count = g_oMap.size ();
	g_oMutex.Leave ();
	return count;
}
