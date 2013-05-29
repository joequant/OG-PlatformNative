/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_fudgemsgmap_h
#define __inc_og_rstats_client_fudgemsgmap_h

/// A message entry from the map. The entry provides the serialized form of the message
/// and a reference count used to determine when the message is no longer reachable
/// from R.
class CFudgeMsgInfo {
private:

	/// The message.
	FudgeMsg m_msg;

	/// The binary encoding of the message.
	void * volatile m_pData;

	/// The length of the binary encoding in bytes.
	size_t m_cbData;

	/// The number of active R references.
	int m_nRefCount;

	CFudgeMsgInfo (FudgeMsg msg, void *pData, size_t cbData);
	~CFudgeMsgInfo ();
public:

	/// Returns the normalized message encoding.
	///
	/// @return the message
	FudgeMsg GetMessage () const { FudgeMsg_retain (m_msg); return m_msg; }

	const void *GetData ();
	size_t GetLength ();
	void Retain ();
	static void Release (CFudgeMsgInfo *poMessage);
	static CFudgeMsgInfo *GetMessage (FudgeMsg msg);
	static CFudgeMsgInfo *GetMessage (const void *pData, size_t cbData);
	static size_t GetBufferSize ();
	static size_t GetBufferCount ();
};

#endif /* ifndef __inc_og_rstats_client_fudgemsgmap_h */
