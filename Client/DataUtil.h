/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_client_datautil_h
#define __inc_og_rstats_client_datautil_h

#include <Connector/com_opengamma_language_Data.h>

#define DATATYPE_BOOLEAN		0x0001
#define DATATYPE_INTEGER		0x0002
#define DATATYPE_DOUBLE			0x0004
#define DATATYPE_STRING			0x0008
#define DATATYPE_MESSAGE		0x0010
#define DATATYPE_ERROR			0x0020

class CDataUtil {
private:
	CDataUtil () { }
	~CDataUtil () { }
public:
	static int TypeOf (const com_opengamma_language_Value *pValue);
	static int TypeOf (const com_opengamma_language_Value * const *ppValue, int *pnValue = NULL);
	static int TypeOf (const com_opengamma_language_Value * const * const *pppValue, int *pnFirst = NULL, int *pnSecond = NULL);
	static int SingleType (int type);
	static bool CanCoerce (int typeFrom, int typeTo);
	static bool Coerce (int type, com_opengamma_language_Value *pValue);
	static bool Coerce (int type, com_opengamma_language_Value * const *ppValue);
	static bool Coerce (int type, com_opengamma_language_Value * const * const *ppValue);
};

#endif /* ifndef __inc_og_rstats_client_datautil_h */
