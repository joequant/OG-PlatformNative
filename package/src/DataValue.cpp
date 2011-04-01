/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "DataValue.h"
#include "Errors.h"
#include "FudgeMsg.h"
#include "RCallback.h"
#include "ErrorValue.h"
#include Client(DataUtil.h)

LOGGING (com.opengamma.pirate.package.DataValue);

static com_opengamma_language_Value *_FudgeMsgValue (FudgeMsg msg) {
	com_opengamma_language_Value *pValue = new com_opengamma_language_Value;
	if (pValue) {
		memset (pValue, 0, sizeof (com_opengamma_language_Value));
		FudgeMsg_retain (msg);
		pValue->_messageValue = msg;
	} else {
		LOGFATAL (ERR_MEMORY);
	}
	return pValue;
}

com_opengamma_language_Value *CValue::FromSEXP (SEXP value, int index) {
	if (TYPEOF (value) == VECSXP) {
		return FromSEXP (VECTOR_ELT (value, index));
	}
	com_opengamma_language_Value *pValue = new com_opengamma_language_Value;
	if (pValue) {
		memset (pValue, 0, sizeof (com_opengamma_language_Value));
		switch (TYPEOF (value)) {
			case INTSXP :
				pValue->_intValue = (fudge_i32*)malloc (sizeof (fudge_i32));
				if (pValue->_intValue) *pValue->_intValue = INTEGER (value)[index];
				break;
			case LGLSXP :
				pValue->_boolValue = (fudge_bool*)malloc (sizeof (fudge_bool));
				if (pValue->_boolValue) *pValue->_boolValue = INTEGER (value)[index] ? FUDGE_TRUE : FUDGE_FALSE;
				break;
			case NILSXP :
				LOGDEBUG (TEXT ("NULL value"));
				break;
			case REALSXP :
				pValue->_doubleValue = (fudge_f64*)malloc (sizeof (fudge_f64));
				if (pValue->_doubleValue) *pValue->_doubleValue = REAL (value)[index];
				break;
			case STRSXP :
				pValue->_stringValue = Ascii_tcsDup (CHAR (STRING_ELT (value, index)));
				break;
			default :
				LOGERROR (TEXT ("Invalid value, type ") << TYPEOF (value));
				break;
		}
	} else {
		LOGFATAL (ERR_MEMORY);
	}
	return pValue;
}

void CValue::ToSEXP (int type, SEXP vector, int index, const com_opengamma_language_Value *pValue) {
	switch (type) {
	case DATATYPE_BOOLEAN :
		INTEGER(vector)[index] = *pValue->_boolValue;
		break;
	case DATATYPE_INTEGER :
		INTEGER(vector)[index] = *pValue->_intValue;
		break;
	case DATATYPE_DOUBLE :
		REAL(vector)[index] = *pValue->_doubleValue;
		break;
	case DATATYPE_STRING :
		SET_STRING_ELT (vector, index, mkChar (pValue->_stringValue));
		break;
	case DATATYPE_MESSAGE :
		SET_VECTOR_ELT (vector, index, RFudgeMsg::FromFudgeMsg (pValue->_messageValue));
		break;
	case DATATYPE_ERROR :
		TODO (TEXT ("Store ERROR type in vector"));
		break;
	default :
		LOGWARN (TEXT ("Invalid type, ") << type);
		break;
	}
}

SEXP CValue::ToSEXP (const com_opengamma_language_Value *pValue) {
	SEXP result = R_NilValue;
	if (pValue->_errorValue) {
		LOGWARN (TEXT ("Error ") << *pValue->_errorValue << TEXT (" in response"));
		result = RErrorValue::FromValue (pValue);
	} else if (pValue->_boolValue) {
		LOGDEBUG (TEXT ("BOOL value"));
		result = allocVector (LGLSXP, 1);
		ToSEXP (DATATYPE_BOOLEAN, result, 0, pValue);
	} else if (pValue->_doubleValue) {
		LOGDEBUG (TEXT ("DOUBLE value"));
		result = allocVector (REALSXP, 1);
		ToSEXP (DATATYPE_DOUBLE, result, 0, pValue);
	} else if (pValue->_intValue) {
		LOGDEBUG (TEXT ("INTEGER value"));
		result = allocVector (INTSXP, 1);
		ToSEXP (DATATYPE_INTEGER, result, 0, pValue);
	} else if (pValue->_messageValue) {
		LOGDEBUG (TEXT ("MESSAGE value"));
		result = RFudgeMsg::FromFudgeMsg (pValue->_messageValue);
	} else if (pValue->_stringValue) {
		LOGDEBUG (TEXT ("STRING value"));
		result = mkString (pValue->_stringValue);
	} else {
		LOGWARN (TEXT ("Invalid value in response"));
	}
	return result;
}

com_opengamma_language_Data *CData::FromSEXP (SEXP data) {
	com_opengamma_language_Data *pData = new com_opengamma_language_Data;
	if (pData) {
		memset (pData, 0, sizeof (com_opengamma_language_Data));
		if (isMatrix (data)) {
			int rows = nrows (data), cols = ncols (data);
			LOGDEBUG (TEXT ("Matrix with ") << rows << TEXT ("x") << cols);
			pData->_matrix = new com_opengamma_language_Value**[rows + 1];
			int i, j;
			for (i = 0; i < rows; i++) {
				pData->_matrix[i] = new com_opengamma_language_Value*[cols + 1];
				for (j = 0; j < cols; j++) {
					pData->_matrix[i][j] = CValue::FromSEXP (data, (j * rows) + i);
				}
				pData->_matrix[i][cols] = NULL;
			}
			pData->_matrix[rows] = NULL;
		} else if (isVector (data) || isList (data)) {
			if (length (data) > 1) {
				LOGDEBUG (TEXT ("Vector with ") << length (data) << TEXT (" elements"));
				pData->_linear = new com_opengamma_language_Value*[length (data) + 1];
				if (pData->_linear) {
					int n;
					for (n = 0; n < length (data); n++) {
						pData->_linear[n] = CValue::FromSEXP (data, n);
					}
					pData->_linear[length (data)] = NULL;
				} else {
					LOGFATAL (ERR_MEMORY);
				}
			} else if (length (data) == 1) {
				LOGDEBUG (TEXT ("Primitive"));
				pData->_single = CValue::FromSEXP (data);
			} else {
				LOGDEBUG (TEXT ("Empty vector"));
			}
		} else if (isNull (data)) {
			LOGDEBUG (TEXT ("NULL"));
		} else if (isObject (data)) {
			FudgeMsg msg = RFudgeMsg::ToFudgeMsg (data);
			if (msg) {
				LOGDEBUG (TEXT ("Single FudgeMsg"));
				pData->_single = _FudgeMsgValue (msg);
				FudgeMsg_release (msg);
			} else {
				LOGDEBUG (TEXT ("Using toString on unknown object type"));
				pData->_single = CValue::FromSEXP (CRCallback::ToString (data));
			}
		} else {
			LOGWARN (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGFATAL (ERR_MEMORY);
	}
	return pData;
}

static SEXP _LinearToList (const com_opengamma_language_Value * const *ppValue, int nCount) {
	int n;
	LOGDEBUG (TEXT ("Converting ") << nCount << TEXT (" element result list"));
	SEXP result = allocVector (VECSXP, nCount);
	PROTECT (result);
	for (n = 0; n < nCount; n++) {
		SEXP value = CValue::ToSEXP (ppValue[n]);
		SET_VECTOR_ELT (result, n, value);
	}
	UNPROTECT (1);
	return result;
}

static SEXPTYPE _DataTypeToSEXPTYPE (int type) {
	switch (type) {
	case DATATYPE_BOOLEAN :
		return LGLSXP;
	case DATATYPE_INTEGER :
		return INTSXP;
	case DATATYPE_DOUBLE :
		return REALSXP;
	case DATATYPE_MESSAGE :
		// Force degeneration to lists
		return 0;
	case DATATYPE_ERROR :
		TODO (TEXT ("DATATYPE_ERROR"));
		return 0;
	default :
		LOGERROR (TEXT ("Unknown type, ") << type);
		return 0;
	}
}

static SEXP _LinearToVector (int type, const com_opengamma_language_Value * const *ppValue, int nCount) {
	int n;
	LOGDEBUG (TEXT ("Converting ") << nCount << TEXT (" element result vector"));
	SEXPTYPE sexptype = _DataTypeToSEXPTYPE (type);
	if (!sexptype) {
		return _LinearToList (ppValue, nCount);
	}
	SEXP result = allocVector (sexptype, nCount);
	PROTECT (result);
	for (n = 0; n < nCount; n++) {
		CValue::ToSEXP (type, result, n, ppValue[n]);
	}
	UNPROTECT (1);
	return result;
}

static SEXP _LinearToSEXP (com_opengamma_language_Value * const *ppValue) {
	int nCount;
	int type = CDataUtil::TypeOf (ppValue, &nCount);
	int stype = CDataUtil::SingleType (type);
	if ((stype == type) || (CDataUtil::CanCoerce (type, stype) && CDataUtil::Coerce (stype, ppValue))) {
		// Have a pure (or coerced) typed linear result; convert to a typed-vector
		return _LinearToVector (stype, ppValue, nCount);
	} else {
		// Have a mixed linear result; return as a list
		return _LinearToList (ppValue, nCount);
	}
}

static SEXP _MatrixToLists (com_opengamma_language_Value * const * const *pppValue, int nRows) {
	int n;
	LOGDEBUG (TEXT ("Converting ") << nRows << TEXT (" element result list"));
	SEXP result = allocVector (VECSXP, nRows);
	PROTECT (result);
	for (n = 0; n < nRows; n++) {
		SET_VECTOR_ELT (result, n, _LinearToSEXP (pppValue[n]));
	}
	UNPROTECT (1);
	return result;
}

static SEXP _MatrixToMatrix (int type, com_opengamma_language_Value * const * const *pppValue, int nRows, int nCols) {
	int i, j;
	LOGDEBUG (TEXT ("Converting ") << nRows << TEXT ("x") << nCols << TEXT (" element result matrix"));
	SEXPTYPE sexptype = _DataTypeToSEXPTYPE (type);
	if (!sexptype) {
		return _MatrixToLists (pppValue, nRows);
	}
	SEXP result = allocMatrix (sexptype, nRows, nCols);
	PROTECT (result);
	for (i = 0; (i < nRows) && pppValue[i]; i++) {
		for (j = 0; (j < nCols) && pppValue[i][j]; j++) {
			CValue::ToSEXP (type, result, (j * nRows) + i, pppValue[i][j]);
		}
	}
	UNPROTECT (1);
	return result;
}

static SEXP _MatrixToSEXP (com_opengamma_language_Value * const * const *pppValue) {
	int nRows, nCols;
	int type = CDataUtil::TypeOf (pppValue, &nRows, &nCols);
	int stype = CDataUtil::SingleType (type);
	if ((stype == type) || (CDataUtil::CanCoerce (type, stype) && CDataUtil::Coerce (stype, pppValue))) {
		// Have a pure (or coerced) type matrix result; convert to a typed-matrix
		return _MatrixToMatrix (stype, pppValue, nRows, nCols);
	} else {
		// Have a mixed matrix result; return as a list of linear best guesses
		return _MatrixToLists (pppValue, nRows);
	}
}

SEXP CData::ToSEXP (const com_opengamma_language_Data *pData) {
	SEXP result = R_NilValue;;
	if (pData->_single) {
		LOGDEBUG (TEXT ("Converting SINGLE value result"));
		result = CValue::ToSEXP (pData->_single);
	} else if (pData->_linear) {
		LOGDEBUG (TEXT ("Converting LINEAR result"));
		result = _LinearToSEXP (pData->_linear);
	} else if (pData->_matrix) {
		LOGDEBUG (TEXT ("Converting MATRIX result"));
		result = _MatrixToSEXP (pData->_matrix);
	} else {
		LOGDEBUG (TEXT ("Empty DATA structure"));
	}
	return result;
}
