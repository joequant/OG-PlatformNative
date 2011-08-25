/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_parameter_h
#define __inc_og_rstats_client_parameter_h

#include <Connector/com_opengamma_language_definition_Parameter.h>

#define PARAMETER_FLAG_OPTIONAL		1

class CParameter {
private:
	char *m_pszName;
	int m_nFlags;
public:
	CParameter (const com_opengamma_language_definition_Parameter *pDefinition);
	~CParameter ();
	const char *GetName () const { return m_pszName; }
	int GetFlags () const { return m_nFlags; }
};

#endif /* ifndef __inc_og_rstats_client_parameter_h */
