/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the objects and functions in Client/FudgeMsgMap.cpp

#ifdef _WIN32
#ifdef _UNICODE
#define GetMessage	GetMessageW
#else /* ifdef _UNICODE */
#define GetMessage	GetMessageA
#endif /* ifdef _UNICODE */
#endif /* ifdef _WIN32 */
#include "FudgeMsgMap.h"

LOGGING (com.opengamma.rstats.client.FudgeMsgMapTest);

static CFudgeInitialiser g_oFudgeInitialiser;

static FudgeMsg CreateMessage (fudge_i32 n) {
	FudgeMsg msg;
	ASSERT (FudgeMsg_create (&msg) == FUDGE_OK);
	FudgeMsg_addFieldI32 (msg, NULL, NULL, n);
	return msg;
}

static void GetAndRelease () {
	FudgeMsg a1 = CreateMessage (1);
	FudgeMsg a2 = CreateMessage (1);
	FudgeMsg b = CreateMessage (2);
	CFudgeMsgInfo *x = CFudgeMsgInfo::GetMessage (a1);
	ASSERT (x);
	ASSERT (x->GetMessage () == a1);
	x = CFudgeMsgInfo::GetMessage (a2);
	ASSERT (x);
	ASSERT (x->GetMessage () == a1);
	CFudgeMsgInfo::Release (x);
	x = CFudgeMsgInfo::GetMessage (a2);
	ASSERT (x);
	ASSERT (x->GetMessage () == a1);
	CFudgeMsgInfo::Release (x);
	CFudgeMsgInfo::Release (x);
	x = CFudgeMsgInfo::GetMessage (a2);
	ASSERT (x);
	ASSERT (x->GetMessage () == a2);
	x = CFudgeMsgInfo::GetMessage (b);
	ASSERT (x);
	ASSERT (x->GetMessage () == b);
}

BEGIN_TESTS (FudgeMsgMapTest)
	UNIT_TEST (GetAndRelease)
END_TESTS
