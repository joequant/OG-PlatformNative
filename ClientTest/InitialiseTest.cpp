/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the functions in Client/Initialise.cpp

#include "Client/Initialise.h"

LOGGING (com.opengamma.pirate.client.InitialiseTest);

static void CallInitialise () {
	ASSERT (Initialise ());
}

BEGIN_TESTS (InitialiseTest)
	TEST (CallInitialise)
END_TESTS
