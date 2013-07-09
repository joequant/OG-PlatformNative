/*
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the objects and functions in Client/StringUtil.cpp

#include "StringUtil.h"

LOGGING (com.opengamma.rstats.client.StringUtil);

static void UnescapedA () {
	ASSERT (!StringEscapeA ("No escaped characters", "", 0));
	ASSERT (!StringEscapeA ("No escaped characters", "\\\"", '\\'));
	ASSERT (!StringEscapeA ("", "\\\"", '\\'));
}

static void EscapedA () {
	char *psz;
	psz = StringEscapeA ("\\Te\"s\\t\\\"", "\\\"", '\\');
	ASSERT (psz);
	ASSERT (!strcmp (psz, "\\\\Te\\\"s\\\\t\\\\\\\""));
	free (psz);
}

BEGIN_TESTS (StringUtilTest)
	UNIT_TEST (UnescapedA)
	UNIT_TEST (EscapedA)
END_TESTS
