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
	void *m_pData;

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

	/// Returns the binary encoding of the message.
	///
	/// @return the encoding
	const void *GetData () const { return m_pData; }

	/// Returns the length of the binary encoding of the message in bytes.
	///
	/// @return the length in bytes
	size_t GetLength () const { return m_cbData; }

	void Retain ();
	static void Release (CFudgeMsgInfo *poMessage);
	static CFudgeMsgInfo *GetMessage (FudgeMsg msg);
	static CFudgeMsgInfo *GetMessage (const void *pData, size_t cbData);
};

#endif /* ifndef __inc_og_rstats_client_fudgemsgmap_h */
