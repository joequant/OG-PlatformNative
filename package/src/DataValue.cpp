/*
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

LOGGING (com.opengamma.rstats.package.DataValue);

/// Creates a Value instance describing the FudgeMsg.
///
/// @param[in] msg Fudge message to describe
/// @return the Value instance
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

/// Creates a Value instance descriping the object (either its toFudgeMsg or toString).
///
/// @param[in] poR callback object representing the original caller's environment
/// @param[in] obj object to convert
/// @return the Value instance
static com_opengamma_language_Value *_ObjectValue (const CRCallback *poR, SEXP obj) {
	com_opengamma_language_Value *pValue;
	FudgeMsg msg = RFudgeMsg::ToFudgeMsg (poR, obj);
	if (msg) {
		pValue = _FudgeMsgValue (msg);
		FudgeMsg_release (msg);
	} else {
		LOGDEBUG (TEXT ("Using toString on unknown object type"));
		SEXP str = poR->ToString (obj);
		if (isObject (str)) {
			// Avoid the infinite recursion case should toString give us an object
			LOGERROR (ERR_PARAMETER_TYPE);
			return NULL;
		}
		PROTECT (str);
		pValue = CValue::FromSEXP (poR, str);
		UNPROTECT (1);
	}
	return pValue;
}

/// Creates a Value instance representing the SEXP type. Only basic vectors can be represented as Values;
/// any lists or multiple element vectors will need to be converted to Data instance.
///
/// @param[in] poR callback object representing the original caller's environment
/// @param[in] value value to convert
/// @param[in] index element index to convert from a vector value
/// @return the Value instance or NULL if there was a problem (e.g. invalid type)
com_opengamma_language_Value *CValue::FromSEXP (const CRCallback *poR, SEXP value, int index) {
#define ALLOC_PVALUE \
    pValue = new com_opengamma_language_Value; \
    if (!pValue) { \
        LOGFATAL (ERR_MEMORY); \
        return NULL; \
    } \
    memset (pValue, 0, sizeof (com_opengamma_language_Value));
	com_opengamma_language_Value *pValue;
	switch (TYPEOF (value)) {
		case INTSXP :
			ALLOC_PVALUE
			pValue->_intValue = (fudge_i32*)malloc (sizeof (fudge_i32));
			if (pValue->_intValue) *pValue->_intValue = INTEGER (value)[index];
			break;
		case LGLSXP :
			ALLOC_PVALUE
			pValue->_boolValue = (fudge_bool*)malloc (sizeof (fudge_bool));
			if (pValue->_boolValue) *pValue->_boolValue = INTEGER (value)[index] ? FUDGE_TRUE : FUDGE_FALSE;
			break;
		case NILSXP :
			LOGDEBUG (TEXT ("NULL value"));
			pValue = NULL;
			break;
		case REALSXP :
			ALLOC_PVALUE
			pValue->_doubleValue = (fudge_f64*)malloc (sizeof (fudge_f64));
			if (pValue->_doubleValue) *pValue->_doubleValue = REAL (value)[index];
			break;
		case S4SXP :
			return _ObjectValue (poR, value);
		case STRSXP :
			ALLOC_PVALUE
			pValue->_stringValue = Ascii_tcsDup (CHAR (STRING_ELT (value, index)));
			break;
		case VECSXP :
			return FromSEXP (poR, VECTOR_ELT (value, index));
		default :
			LOGERROR (ERR_PARAMETER_TYPE);
			pValue = NULL;
			break;
	}
	return pValue;
#undef ALLOC_PVALUE
}

/// Converts a Value instance into a SEXP vector element.
///
/// @param[in] type type of the value element
/// @param[in] vector vector to populate
/// @param[in] index index of the vector element to update
/// @param[in] pValue value instance to convert
void CValue::ToSEXP (int type, SEXP vector, int index, const com_opengamma_language_Value *pValue) {
	switch (type) {
	case DATATYPE_BOOLEAN :
		if (pValue->_boolValue) {
			LOGDEBUG (TEXT ("BOOL value ") << *pValue->_boolValue);
			INTEGER(vector)[index] = *pValue->_boolValue;
		} else {
			LOGDEBUG (TEXT ("BOOL value N/A"));
			INTEGER(vector)[index] = NA_LOGICAL;
		}
		break;
	case DATATYPE_INTEGER :
		if (pValue->_intValue) {
			LOGDEBUG (TEXT ("INTEGER value ") << *pValue->_intValue);
			INTEGER(vector)[index] = *pValue->_intValue;
		} else {
			LOGDEBUG (TEXT ("INTEGER value N/A"));
			INTEGER(vector)[index] = NA_INTEGER;
		}
		break;
	case DATATYPE_DOUBLE :
		if (pValue->_doubleValue) {
			LOGDEBUG (TEXT ("DOUBLE value ") << *pValue->_doubleValue);
			REAL(vector)[index] = *pValue->_doubleValue;
		} else {
			LOGDEBUG (TEXT ("DOUBLE value N/A"));
			REAL(vector)[index] = NA_REAL;
		}
		break;
	case DATATYPE_STRING : {
		if (pValue->_stringValue) {
			LOGDEBUG (TEXT ("STRING value ") << pValue->_stringValue);
#ifdef _UNICODE
			char *pszStringValue = WideToAsciiDup (pValue->_stringValue);
			SEXP value;
			if (pszStringValue) {
				value = mkChar (pszStringValue);
				free (pszStringValue);
			} else {
				LOGFATAL (ERR_MEMORY);
				value = R_NilValue;
			}
#else /* ifdef _UNICODE */
			SEXP value = mkChar (pValue->_stringValue);
#endif /* ifdef _UNICODE */
			PROTECT (value);
			SET_STRING_ELT (vector, index, value);
			UNPROTECT (1);
		} else {
			LOGDEBUG (TEXT ("STRING value N/A"));
			SET_STRING_ELT (vector, index, NA_STRING);
		}
		break;
						   }
	case DATATYPE_MESSAGE : {
		if (pValue->_messageValue) {
			LOGDEBUG (TEXT ("MESSAGE value"));
			SEXP value = RFudgeMsg::FromFudgeMsg (pValue->_messageValue);
			PROTECT (value);
			SET_VECTOR_ELT (vector, index, value);
			UNPROTECT (1);
		} else {
			LOGDEBUG (TEXT ("MESSAGE value N/A"));
			// Omit a value, or is there a NA_ equivalent?
		}
		break;
							}
	case DATATYPE_ERROR :
		LOGDEBUG (TEXT ("ERROR value"));
		LOGWARN (TEXT ("TODO: store ERROR type in vector"));
		LOGFATAL (ERR_INTERNAL);
		break;
	default :
		LOGDEBUG (TEXT ("Unknown type ") << type);
		LOGERROR (ERR_RESULT_TYPE);
		break;
	}
}

/// Converts a value instance to an R SEXP representation.
/// 
/// @param[in] pValue value to convert
/// @return the SEXP encoding, or R_NilValue if there is a problem
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
#ifdef _UNICODE
		char *pszStringValue = WideToAsciiDup (pValue->_stringValue);
		if (pszStringValue) {
			result = mkString (pszStringValue);
			free (pszStringValue);
		} else {
			LOGFATAL (ERR_MEMORY);
		}
#else
		result = mkString (pValue->_stringValue);
#endif /* ifdef _UNICODE */
	} else {
		LOGDEBUG (TEXT ("NULL value in response"));
	}
	return result;
}

/// Converts an R SEXP representation to a Data instance. Native R objects that cannot be represented
/// as Value instances are converted via their toFudgeMsg or toString generic methods if possible.
///
/// @param[in] poR callback object representing the original caller's environment
/// @param[in] data data to convert
/// @return the data instance, or NULL if there is a problem
com_opengamma_language_Data *CData::FromSEXP (const CRCallback *poR, SEXP data) {
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
					pData->_matrix[i][j] = CValue::FromSEXP (poR, data, (j * rows) + i);
				}
				pData->_matrix[i][cols] = NULL;
			}
			pData->_matrix[rows] = NULL;
		} else if (isTs (data)) {
			LOGDEBUG (TEXT ("TimeSeries with ") << length (data) << TEXT (" samples"));
			pData->_linear = new com_opengamma_language_Value*[length (data) + 2];
			if (pData->_linear) {
				int n;
				pData->_linear[n] = CValue::FromSEXP (poR, poR->InteropConvert (data, "TimeSeriesStart"));
				for (n = 0; n < length (data); n++) {
					pData->_linear[n + 1] = CValue::FromSEXP (poR, data, n);
				}
				pData->_linear[n + 1] = NULL;
			} else {
				LOGFATAL (ERR_MEMORY);
			}
		} else if (isVector (data) || isList (data)) {
			if (length (data) > 1) {
				LOGDEBUG (TEXT ("Vector with ") << length (data) << TEXT (" elements"));
				pData->_linear = new com_opengamma_language_Value*[length (data) + 1];
				if (pData->_linear) {
					int n;
					for (n = 0; n < length (data); n++) {
						pData->_linear[n] = CValue::FromSEXP (poR, data, n);
					}
					pData->_linear[n] = NULL;
				} else {
					LOGFATAL (ERR_MEMORY);
				}
			} else if (length (data) == 1) {
				LOGDEBUG (TEXT ("Primitive"));
				pData->_single = CValue::FromSEXP (poR, data);
			} else {
				LOGDEBUG (TEXT ("Empty vector"));
			}
		} else if (isNull (data)) {
			LOGDEBUG (TEXT ("NULL"));
		} else if (isObject (data)) {
			pData->_single = _ObjectValue (poR, data);
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
		}
	} else {
		LOGFATAL (ERR_MEMORY);
	}
	return pData;
}

/// Converts a linear Data object to an R list.
///
/// @param[in] ppValue list of values to convert
/// @param[in] nCount number of values in the list
/// @return the R list
static SEXP _LinearToList (const com_opengamma_language_Value * const *ppValue, int nCount) {
	int n;
	LOGDEBUG (TEXT ("Converting ") << nCount << TEXT (" element result list"));
	SEXP result = allocVector (VECSXP, nCount);
	PROTECT (result);
	for (n = 0; n < nCount; n++) {
		SEXP value = CValue::ToSEXP (ppValue[n]);
		PROTECT (value);
		SET_VECTOR_ELT (result, n, value);
	}
	UNPROTECT (1 + nCount);
	return result;
}

/// Converts a Value type token to the equivalent R type token.
///
/// @param[in] type Value type token
/// @return R type token
static SEXPTYPE _DataTypeToVectorType (int type) {
	switch (type) {
	case DATATYPE_BOOLEAN :
		return LGLSXP;
	case DATATYPE_INTEGER :
		return INTSXP;
	case DATATYPE_DOUBLE :
		return REALSXP;
	case DATATYPE_STRING :
		return STRSXP;
	case DATATYPE_MESSAGE :
		// Force degeneration to list; can't have a vector of lists
		return 0;
	case DATATYPE_ERROR :
		LOGWARN (TEXT ("TODO: convert ERROR to SEXP"));
		LOGFATAL (ERR_INTERNAL);
		// NA?
		return 0;
	default :
		LOGERROR (ERR_RESULT_TYPE);
		return 0;
	}
}

/// Converts a linear Data object to an R vector.
///
/// @param[in] type Value type token of elements
/// @param[in] ppValue values to convert
/// @param[in] nCount number of values
/// @return the R vector
static SEXP _LinearToVector (int type, const com_opengamma_language_Value * const *ppValue, int nCount) {
	int n;
	LOGDEBUG (TEXT ("Converting ") << nCount << TEXT (" element result vector"));
	SEXPTYPE sexptype = _DataTypeToVectorType (type);
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

/// Converts a linear Data object to an R SEXP value; either a vector or a list depending on the underlying
/// data.
///
/// @param[in] ppValue values to convert
/// @return the SEXP - either a vector or list
static SEXP _LinearToSEXP (com_opengamma_language_Value * const *ppValue) {
	int nCount;
	int type = CDataUtil::TypeOf (ppValue, &nCount);
	if (nCount == 0) {
		return allocVector (INTSXP, 0);
	}
	int stype = CDataUtil::SingleType (type);
	if ((stype == type) || (CDataUtil::CanCoerce (type, stype) && CDataUtil::Coerce (stype, ppValue))) {
		// Have a pure (or coerced) typed linear result; convert to a typed-vector
		return _LinearToVector (stype, ppValue, nCount);
	} else {
		// Have a mixed linear result; return as a list
		return _LinearToList (ppValue, nCount);
	}
}

/// Converts a matrix Data object to an R list of lists or list of vectors.
///
/// @param[in] pppValue matrix data
/// @param[in] nRows number of rows in the matrix
/// @return the R list of lists or list of vectors
static SEXP _MatrixToLists (com_opengamma_language_Value * const * const *pppValue, int nRows) {
	int n;
	LOGDEBUG (TEXT ("Converting ") << nRows << TEXT (" element result list"));
	SEXP result = allocVector (VECSXP, nRows);
	PROTECT (result);
	for (n = 0; n < nRows; n++) {
		SEXP value = _LinearToSEXP (pppValue[n]);
		PROTECT (value);
		SET_VECTOR_ELT (result, n, value);
	}
	UNPROTECT (1 + nRows);
	return result;
}

/// Converts a matrix Data object to an R matrix.
///
/// @param[in] type Value type token of elements
/// @param[in] pppValue matrix elements
/// @param[in] nRows number of rows in the matrix
/// @param[in] nCols number of columns in the matrix
/// @return the R matrix
static SEXP _MatrixToMatrix (int type, com_opengamma_language_Value * const * const *pppValue, int nRows, int nCols) {
	int i, j;
	LOGDEBUG (TEXT ("Converting ") << nRows << TEXT ("x") << nCols << TEXT (" element result matrix"));
	SEXPTYPE sexptype = _DataTypeToVectorType (type);
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

/// Converts a matrix Data object to an R SEXP value. This may be a matrix, a list of lists, or a list of
/// vectors depending on the underlying type.
///
/// @param[in] pppValue matrix values
/// @return the R SEXP value
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

/// Converts a Data object to an R representation.
///
/// @param[in] pData data value to convert
/// @return the R representation
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
