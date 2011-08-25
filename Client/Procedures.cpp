/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Procedures.h)
#include Client(Connector.h)

LOGGING (com.opengamma.rstats.client.Procedures);

CProcedureEntry::CProcedureEntry (int nInvocationId, const com_opengamma_language_procedure_Definition *pDefinition)
: CEntityEntry (nInvocationId, &pDefinition->fudgeParent) {
}

CProcedureEntry::~CProcedureEntry () {
}

com_opengamma_language_Data *CProcedureEntry::Invoke (const CConnector *poConnector, const com_opengamma_language_Data * const *ppArg) const {
	LOGDEBUG ("Invoking " << GetName ());
	TODO (TEXT ("Invoke ") << GetInvocationId ());
	return NULL;
}

CProcedures::CProcedures (const CConnector *poConnector, const com_opengamma_language_procedure_Available *pAvailable)
: CEntities (poConnector, pAvailable->fudgeCountProcedure) {
	LOGINFO (TEXT ("Creating procedure repository"));
	int n, count = pAvailable->fudgeCountProcedure;
	for (n = 0; n < count; n++) {
		SetImpl (n, new CProcedureEntry (pAvailable->_procedure[n]->_identifier, pAvailable->_procedure[n]->_definition));
	}
}

CProcedures::~CProcedures () {
	LOGINFO (TEXT ("Destroying procedure repository"));
}

const CProcedures *CProcedures::GetAvailable (CProcedureQueryAvailable *poQuery) {
	LOGDEBUG (TEXT ("Waiting for procedure definitions"));
	const com_opengamma_language_procedure_Available *pAvailable = poQuery->Recv (CRequestBuilder::GetDefaultTimeout ());
	if (!pAvailable) {
		LOGWARN (TEXT ("Did not get available procedures response"));
		return NULL;
	}
	if (pAvailable->fudgeCountProcedure > 0) {
		const CConnector *poConnector = poQuery->GetConnector ();
		CProcedures *poProcedures = new CProcedures (poConnector, pAvailable);
		CConnector::Release (poConnector);
		return poProcedures;
	} else {
		LOGWARN (TEXT ("No procedures available"));
		return NULL;
	}
}
