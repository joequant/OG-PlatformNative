/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include Client(DataUtil.h)

LOGGING (com.opengamma.rstats.client.DataUtil);

int CDataUtil::TypeOf (const com_opengamma_language_Value *pValue) {
	if (!pValue) {
		LOGWARN (TEXT ("Null pointer"));
		return 0;
	}
	if (pValue->_errorValue) {
		return DATATYPE_ERROR;
	} else if (pValue->_boolValue) {
		return DATATYPE_BOOLEAN;
	} else if (pValue->_intValue) {
		return DATATYPE_INTEGER;
	} else if (pValue->_doubleValue) {
		return DATATYPE_DOUBLE;
	} else if (pValue->_stringValue) {
		return DATATYPE_STRING;
	} else if (pValue->_messageValue) {
		return DATATYPE_MESSAGE;
	} else {
		LOGDEBUG (TEXT ("Empty data value"));
		return 0;
	}
}

int CDataUtil::TypeOf (const com_opengamma_language_Value * const *ppValue, int *pnValue) {
	int type = 0;
	int nCount = 0;
	while (*ppValue) {
		type |= TypeOf (*ppValue);
		ppValue++;
		nCount++;
	}
	if (pnValue) *pnValue = nCount;
	return type;
}

int CDataUtil::TypeOf (const com_opengamma_language_Value * const * const *pppValue, int *pnFirst, int *pnSecond) {
	int type = 0;
	int nFirst = 0, nSecond = 0;
	while (*pppValue) {
		int nCount;
		type |= TypeOf (*pppValue, &nCount);
		pppValue++;
		if (nCount > nSecond) nSecond = nCount;
		nFirst++;
	}
	if (pnFirst) *pnFirst = nFirst;
	if (pnSecond) *pnSecond = nSecond;
	return type;
}

/**
 * Reduces a composite type (e.g. from a linear) to the preferred single type. This should be used
 * in conjunction with CanCoerce to see if the types in use can be coerced to this single type.
 */
int CDataUtil::SingleType (int type) {
	if (type & DATATYPE_ERROR) {
		return DATATYPE_ERROR;
	} else if (type & DATATYPE_STRING) {
		return DATATYPE_STRING;
	} else if (type & DATATYPE_DOUBLE) {
		return DATATYPE_DOUBLE;
	} else if (type & DATATYPE_INTEGER) {
		return DATATYPE_INTEGER;
	} else if (type & DATATYPE_BOOLEAN) {
		return DATATYPE_BOOLEAN;
	} else if (type & DATATYPE_MESSAGE) {
		return DATATYPE_MESSAGE;
	} else {
		return 0;
	}
}

bool CDataUtil::CanCoerce (int typeFrom, int typeTo) {
	if (typeFrom & DATATYPE_ERROR) {
		if (!(typeTo & DATATYPE_ERROR)) {
			return false;
		}
	}
	if (typeFrom & DATATYPE_BOOLEAN) {
		if (!(typeTo & (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_STRING))) {
			return false;
		}
	}
	if (typeFrom & DATATYPE_INTEGER) {
		if (!(typeTo & (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING))) {
			return false;
		}
	}
	if (typeFrom & DATATYPE_DOUBLE) {
		if (!(typeTo & (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING))) {
			return false;
		}
	}
	if (typeFrom & DATATYPE_STRING) {
		if (!(typeTo & (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING))) {
			return false;
		}
	}
	if (typeFrom & DATATYPE_MESSAGE) {
		if (!(typeTo & DATATYPE_MESSAGE)) {
			return false;
		}
	}
	return true;
}

bool CDataUtil::Coerce (int type, com_opengamma_language_Value *pValue) {
	if (type & DATATYPE_ERROR) {
		if (pValue->_errorValue) return true;
	}
	if (type & DATATYPE_STRING) {
		if (pValue->_stringValue) return true;
		do {
			// TODO: what is the largest we'll need to represent a f64 or i32?
			TCHAR szBuffer[32];
			if (pValue->_boolValue) {
				LOGDEBUG (TEXT ("Coerce boolean ") << *pValue->_boolValue << TEXT (" to string"));
				StringCbPrintf (szBuffer, sizeof (szBuffer), TEXT ("%s"), *pValue->_boolValue ? "true" : "");
				free (pValue->_boolValue);
				pValue->_boolValue = NULL;
			} else if (pValue->_intValue) {
				LOGDEBUG (TEXT ("Coerce integer ") << *pValue->_intValue << TEXT (" to string"));
				StringCbPrintf (szBuffer, sizeof (szBuffer), TEXT ("%d"), *pValue->_intValue);
				free (pValue->_intValue);
				pValue->_intValue = NULL;
			} else if (pValue->_doubleValue) {
				LOGDEBUG (TEXT ("Coerce double ") << *pValue->_doubleValue << TEXT (" to string"));
				StringCbPrintf (szBuffer, sizeof (szBuffer), TEXT ("%f"), *pValue->_doubleValue);
				free (pValue->_doubleValue);
				pValue->_doubleValue = NULL;
			} else {
				break;
			}
			pValue->_stringValue = _tcsdup (szBuffer);
			if (!pValue->_stringValue) {
				LOGFATAL (TEXT ("Out of memory"));
			}
			return true;
		} while (false);
	}
	if (type & DATATYPE_DOUBLE) {
		if (pValue->_doubleValue) return true;
		do {
			fudge_f64 doubleValue;
			if (pValue->_boolValue) {
				LOGDEBUG (TEXT ("Coerce boolean ") << *pValue->_boolValue << TEXT (" to double"));
				doubleValue = *pValue->_boolValue ? 1.0 : 0.0;
				free (pValue->_boolValue);
				pValue->_boolValue = NULL;
			} else if (pValue->_intValue) {
				LOGDEBUG (TEXT ("Coerce integer ") << *pValue->_intValue << TEXT (" to double"));
				doubleValue = (fudge_f64)*pValue->_intValue;
				free (pValue->_intValue);
				pValue->_intValue = NULL;
			} else if (pValue->_stringValue) {
				LOGDEBUG (TEXT ("Coerce string ") << *pValue->_stringValue << TEXT (" to double"));
				doubleValue = _tstof (pValue->_stringValue);
				free ((char*)pValue->_stringValue);
				pValue->_stringValue = NULL;
			} else {
				break;
			}
			pValue->_doubleValue = (fudge_f64*)malloc (sizeof (fudge_f64));
			if (pValue->_doubleValue) {
				*pValue->_doubleValue = doubleValue;
			} else {
				LOGFATAL (TEXT ("Out of memory"));
			}
			return true;
		} while (false);
	}
	if (type & DATATYPE_INTEGER) {
		if (pValue->_intValue) return true;
		do {
			fudge_i32 intValue;
			if (pValue->_boolValue) {
				LOGDEBUG (TEXT ("Coerce boolean ") << *pValue->_boolValue << TEXT (" to integer"));
				intValue = *pValue->_boolValue ? -1 : 0;
				free (pValue->_boolValue);
				pValue->_boolValue = NULL;
			} else if (pValue->_doubleValue) {
				LOGDEBUG (TEXT ("Coerce double ") << *pValue->_doubleValue << TEXT (" to integer"));
				intValue = (fudge_i32)*pValue->_doubleValue;
				free (pValue->_doubleValue);
				pValue->_doubleValue = NULL;
			} else if (pValue->_stringValue) {
				LOGDEBUG (TEXT ("Coerce string ") << *pValue->_stringValue << TEXT (" to integer"));
				intValue = _tstoi (pValue->_stringValue);
				free ((char*)pValue->_stringValue);
				pValue->_stringValue = NULL;
			} else {
				break;
			}
			pValue->_intValue = (fudge_i32*)malloc (sizeof (fudge_i32));
			if (pValue->_intValue) {
				*pValue->_intValue = intValue;
			} else {
				LOGFATAL (TEXT ("Out of memory"));
			}
			return true;
		} while (false);
	}
	if (type & DATATYPE_BOOLEAN) {
		if (pValue->_boolValue) return true;
		do {
			fudge_bool boolValue;
			if (pValue->_intValue) {
				LOGDEBUG (TEXT ("Coerce integer ") << *pValue->_intValue << TEXT (" to boolean"));
				boolValue = *pValue->_intValue ? FUDGE_TRUE : FUDGE_FALSE;
				free (pValue->_intValue);
				pValue->_intValue = NULL;
			} else if (pValue->_doubleValue) {
				LOGDEBUG (TEXT ("Coerce double ") << *pValue->_doubleValue << TEXT (" to boolean"));
				boolValue = *pValue->_doubleValue ? FUDGE_TRUE : FUDGE_FALSE;
				free (pValue->_doubleValue);
				pValue->_doubleValue = NULL;
			} else if (pValue->_stringValue) {
				LOGDEBUG (TEXT ("Coerce string ") << *pValue->_stringValue << TEXT (" to boolean"));
				boolValue = *pValue->_stringValue ? FUDGE_TRUE : FUDGE_FALSE;
				free ((char*)pValue->_stringValue);
				pValue->_stringValue = NULL;
			} else {
				break;
			}
			pValue->_boolValue = (fudge_bool*)malloc (sizeof (fudge_bool));
			if (pValue->_boolValue) {
				*pValue->_boolValue = boolValue;
			} else {
				LOGFATAL (TEXT ("Out of memory"));
			}
			return true;
		} while (false);
	}
	if (type & DATATYPE_MESSAGE) {
		if (pValue->_messageValue) return true;
	}
	return false;
}

bool CDataUtil::Coerce (int type, com_opengamma_language_Value * const *ppValue) {
	bool result = true;
	while (*ppValue) {
		result &= Coerce (type, *(ppValue++));
	}
	return result;
}

bool CDataUtil::Coerce (int type, com_opengamma_language_Value * const * const *pppValue) {
	bool result = true;
	while (*pppValue) {
		result &= Coerce (type, *(pppValue++));
	}
	return result;
}
