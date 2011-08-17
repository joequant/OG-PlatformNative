/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Parameters.h"
#include "Errors.h"

LOGGING (com.opengamma.rstats.package.Parameters);

CParameters::CParameters (com_opengamma_language_Data **ppArg, int nArg) {
	m_ppArg = ppArg;
	m_nArg = nArg;
}

CParameters::~CParameters () {
	int n;
	for (n = 0; n < m_nArg; n++) {
		CData::Release (m_ppArg[n]);
	}
	delete m_ppArg;
}

CParameters *CParameters::Decode (SEXP args) {
	CParameters *poResult = NULL;
	if (isVector (args)) {
		int nArgs = length (args);
		LOGDEBUG (TEXT ("Converting ") << nArgs << TEXT (" argument vector to CParameters"));
		com_opengamma_language_Data **ppArg = new com_opengamma_language_Data*[nArgs];
		if (ppArg) {
			int n;
			for (n = 0; n < nArgs; n++) {
				ppArg[n] = CData::FromSEXP (VECTOR_ELT (args, n));
			}
			poResult = new CParameters (ppArg, nArgs);
		} else {
			LOGFATAL (ERR_MEMORY);
		}
	} else {
		LOGWARN (ERR_PARAMETER_TYPE);
	}
	return poResult;
}
