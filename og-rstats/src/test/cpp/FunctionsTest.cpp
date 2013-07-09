/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the objects and functions in Client/Functions.cpp

#include "Connector.h"
#include "Functions.h"
#include "Initialise.h"

LOGGING (com.opengamma.rstats.client.FunctionsTest);

static void QueryAvailable () {
	const CConnector *poConnector = ConnectorInstance ();
	ASSERT (poConnector);
	CFunctionQueryAvailable query (poConnector);
	ASSERT (query.Send ());
	const CFunctions *poFunctions = CFunctions::GetAvailable (&query);
	ASSERT (poFunctions);
	LOGINFO (TEXT ("Found ") << poFunctions->Size () << TEXT (" functions"));
	int i, j;
	for (i = 0; i < poFunctions->Size (); i++) {
		const CFunctionEntry *poFunction = poFunctions->Get (i);
		ASSERT (poFunction);
		LOGDEBUG ("Function " << i << " = " << poFunction->GetName ());
		for (j = 0; j < poFunction->GetParameterCount (); j++) {
			const CParameter *poParameter = poFunction->GetParameter (j);
			ASSERT (poParameter);
			LOGDEBUG ("Parameter " << j << " = " << poParameter->GetName ());
		}
	}
	CFunctions::Release (poFunctions);
	CConnector::Release (poConnector);
}

BEGIN_TESTS (FunctionsTest)
	INTEGRATION_TEST (QueryAvailable)
	BEFORE_TEST (Initialise)
	AFTER_TEST (Shutdown)
END_TESTS
