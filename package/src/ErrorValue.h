/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_package_errorvalue_h
#define __inc_og_pirate_package_errorvalue_h

#include <Connector/com_opengamma_language_Value.h>

class RErrorValue {
private:
	RErrorValue () { }
	~RErrorValue () { }
public:
	static SEXP FromValue (const com_opengamma_language_Value *pValue);
};

#endif /* ifndef __inc_og_pirate_package_errorvalue_h */
