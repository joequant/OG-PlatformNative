/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the objects and functions in Client/Repositories.cpp

#include "Client/Connector.h"
#include "Client/Repositories.h"
#include "Client/Initialise.h"

LOGGING (com.opengamma.rstats.client.FunctionsTest);

static void Construct () {
	CConnector *poConnector = ConnectorInstance ();
	ASSERT (poConnector);
	CRepositories repositories (poConnector);
	CFunctions *poFunctions = repositories.GetFunctions ();
	ASSERT (poFunctions);
	CFunctions::Release (poFunctions);
	CLiveData *poLiveData = repositories.GetLiveData ();
	ASSERT (poLiveData);
	CLiveData::Release (poLiveData);
	CProcedures *poProcedures = repositories.GetProcedures ();
	ASSERT (poProcedures);
	CProcedures::Release (poProcedures);
	CConnector::Release (poConnector);
}

BEGIN_TESTS (RepositoriesTest)
	TEST (Construct)
	BEFORE_TEST (Initialise)
	AFTER_TEST (Shutdown)
END_TESTS
