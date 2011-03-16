/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_client_parameter_h
#define __inc_og_pirate_client_parameter_h

#include <Connector/com_opengamma_language_definition_Parameter.h>

#define PARAMETER_FLAG_OPTIONAL		1

class CParameter {
private:
	char *m_pszName;
	int m_nFlags;
public:
	CParameter (com_opengamma_language_definition_Parameter *pDefinition);
	~CParameter ();
	const char *GetName () { return m_pszName; }
	int GetFlags () { return m_nFlags; }
};

#endif /* ifndef __inc_og_pirate_client_parameter_h */
