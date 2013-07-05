/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions and objects in Connector/Pipes.cpp

#include <connector/cpp/Pipes.h>

LOGGING (com.opengamma.language.connector.PipesTest);

static void Create () {
	CClientPipes *poPipes = CClientPipes::Create ();
	ASSERT (poPipes);
	delete poPipes;
}

static bool _CreateInput (int nCount) {
	CNamedPipe *poPipe = CClientPipes::CreateInput (CNamedPipe::GetTestPipePrefix (), 2, 1);
	bool bResult = poPipe && ((nCount == 1) || _CreateInput (nCount - 1));
	if (poPipe) delete poPipe;
	return bResult;
}

static bool _CreateOutput (int nCount) {
	CNamedPipe *poPipe = CClientPipes::CreateOutput (CNamedPipe::GetTestPipePrefix (), 2, 1);
	bool bResult = poPipe && ((nCount == 1) || _CreateInput (nCount - 1));
	if (poPipe) delete poPipe;
	return bResult;
}

static void CreateInput1 () {
	ASSERT (_CreateInput (1));
}

static void CreateInput2 () {
	ASSERT (_CreateInput (2));
}

static void CreateInputFail () {
	ASSERT (!_CreateInput (3));
}

static void CreateOutput1 () {
	ASSERT (_CreateOutput (1));
}

static void CreateOutput2 () {
	ASSERT (_CreateOutput (2));
}

static void CreateOutputFail () {
	ASSERT (!_CreateOutput (3));
}

BEGIN_TESTS (PipesTest)
	UNIT_TEST (Create)
	UNIT_TEST (CreateInput1)
	UNIT_TEST (CreateInput2)
	UNIT_TEST (CreateInputFail)
	UNIT_TEST (CreateOutput1)
	UNIT_TEST (CreateOutput2)
	UNIT_TEST (CreateOutputFail)
END_TESTS
