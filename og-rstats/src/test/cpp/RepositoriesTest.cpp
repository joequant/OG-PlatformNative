/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the objects and functions in Client/Repositories.cpp

#include "Connector.h"
#include "Repositories.h"
#include "Initialise.h"

LOGGING (com.opengamma.rstats.client.RepositoriesTest);

static void Construct () {
	const CConnector *poConnector = ConnectorInstance ();
	ASSERT (poConnector);
	CRepositories repositories (poConnector);
	const CFunctions *poFunctions = repositories.GetFunctions ();
	ASSERT (poFunctions);
	CFunctions::Release (poFunctions);
	const CLiveData *poLiveData = repositories.GetLiveData ();
	ASSERT (poLiveData);
	CLiveData::Release (poLiveData);
	const CProcedures *poProcedures = repositories.GetProcedures ();
	ASSERT (poProcedures);
	CProcedures::Release (poProcedures);
	CConnector::Release (poConnector);
}

BEGIN_TESTS (RepositoriesTest)
	INTEGRATION_TEST (Construct)
	BEFORE_TEST (Initialise)
	AFTER_TEST (Shutdown)
END_TESTS
