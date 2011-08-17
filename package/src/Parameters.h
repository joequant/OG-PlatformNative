/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_parameters_h
#define __inc_og_rstats_package_parameters_h

#include "DataValue.h"

class CParameters {
private:
	com_opengamma_language_Data **m_ppArg;
	int m_nArg;
	CParameters (com_opengamma_language_Data **ppArg, int nArg);
public:
	~CParameters ();
	static CParameters *Decode (SEXP args);
	int Count () { return m_nArg; }
	com_opengamma_language_Data **GetData () { return m_ppArg; }
};

#endif /* ifndef __inc_og_rstats_package_parameters_h */
