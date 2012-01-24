/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(Parameter.h)

LOGGING (com.opengamma.rstats.client.Parameter);

CParameter::CParameter (const com_opengamma_language_definition_Parameter *pDefinition) {
	m_pszName = _tcsAsciiDup (pDefinition->_name);
	m_pszDescription = pDefinition->_description ? _tcsAsciiDup (pDefinition->_description) : NULL;
	m_nFlags = 0;
	if (!pDefinition->_required) {
		m_nFlags |= PARAMETER_FLAG_OPTIONAL;
	}
}

CParameter::~CParameter () {
	delete m_pszName;
	delete m_pszDescription;
}

