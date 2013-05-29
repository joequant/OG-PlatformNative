/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "FudgeMsg.h"
#include "Errors.h"
#include "RCallback.h"
#include Client(FudgeMsgMap.h)

LOGGING (com.opengamma.rstats.package.FudgeMsg);

#define R_CLASS					"class"
#define R_FUDGEMSG_CLASS		"FudgeMsg"
#define R_FUDGEMSG_POINTER		"message"
#define R_FUDGEFIELD_VALUE		"Value"
#define R_FUDGEFIELD_ORDINAL	"Ordinal"
#define R_FUDGEFIELD_NAME		"Name"
#define R_FUDGEINDICATOR		"indicator"

/// Flag to indicate whether the Fudge message objects sent to R should support
/// serialisation (i.e. allow a workspace to be persisted). This is the default
/// but is slower. If saving a workspace isn't needed, you can run faster with
/// serialisation mode off.
static bool g_bSerialise = true;

static SEXP _CreateByteArray (const fudge_byte *bytes, int elements) {
	SEXP vector = allocVector (INTSXP, elements);
	int n;
	for (n = 0; n < elements; n++) {
		INTEGER (vector)[n] = (unsigned char)bytes[n];
	}
	return vector;
}

static SEXP _CreateShortArray (const fudge_byte *bytes, int elements) {
	const fudge_i16 *pValues = (const fudge_i16*)bytes;
	SEXP vector = allocVector (INTSXP, elements);
	int n;
	for (n = 0; n < elements; n++) {
		INTEGER (vector)[n] = pValues[n];
	}
	return vector;
}

static SEXP _CreateIntArray (const fudge_byte *bytes, int elements) {
	const fudge_i32 *pValues = (const fudge_i32*)bytes;
	SEXP vector = allocVector (INTSXP, elements);
	int n;
	for (n = 0; n < elements; n++) {
		INTEGER (vector)[n] = pValues[n];
	}
	return vector;
}

static SEXP _CreateLongArray (const fudge_byte *bytes, int elements) {
	const fudge_i64 *pValues = (const fudge_i64*)bytes;
	SEXP vector = allocVector (REALSXP, elements);
	int n;
	for (n = 0; n < elements; n++) {
		REAL (vector)[n] = pValues[n];
	}
	return vector;
}

static SEXP _CreateFloatArray (const fudge_byte *bytes, int elements) {
	const fudge_f32 *pValues = (const fudge_f32*)bytes;
	SEXP vector = allocVector (REALSXP, elements);
	int n;
	for (n = 0; n < elements; n++) {
		REAL (vector)[n] = pValues[n];
	}
	return vector;
}

static SEXP _CreateDoubleArray (const fudge_byte *bytes, int elements) {
	const fudge_f64 *pValues = (const fudge_f64*)bytes;
	SEXP vector = allocVector (REALSXP, elements);
	int n;
	for (n = 0; n < elements; n++) {
		REAL (vector)[n] = pValues[n];
	}
	return vector;
}

static void RPROC FudgeMsg_finalizer (SEXP msgptr) {
	FudgeMsg msg = (FudgeMsg)R_ExternalPtrAddr (msgptr);
	if (msg) {
		CFudgeMsgInfo *poMsg = CFudgeMsgInfo::GetMessage (msg);
		if (poMsg) {
			// Release twice; once for the return here, and once deferred from when we set the pointer
			CFudgeMsgInfo::Release (poMsg);
			CFudgeMsgInfo::Release (poMsg);
		} else {
			LOGWARN (ERR_INTERNAL);
		}
		FudgeMsg_release (msg);
		R_ClearExternalPtr (msgptr);
	} else {
		LOGDEBUG (TEXT ("No message pointer attached"));
	}
}

SEXP RFudgeMsg::FromFudgeMsg (FudgeMsg msg) {
	FudgeMsg_retain (msg);
	SEXP msgtag = R_NilValue;
	int prot = 0;
	CFudgeMsgInfo *poMsg = CFudgeMsgInfo::GetMessage (msg);
	if (poMsg) {
		if (g_bSerialise) {
			msgtag = allocVector (RAWSXP, poMsg->GetLength ());
			if (msgtag != R_NilValue) {
				PROTECT (msgtag);
				prot++;
				memcpy (RAW (msgtag), poMsg->GetData (), poMsg->GetLength ());
			} else {
				LOGERROR (ERR_R_FUNCTION);
			}
		}
		FudgeMsg_release (msg);
		msg = poMsg->GetMessage ();
		// Don't release the pointer; R will need the reference counted
	} else {
		LOGERROR (ERR_INTERNAL);
	}
	SEXP msgptr = R_MakeExternalPtr (msg, R_NilValue, msgtag);
	PROTECT (msgptr);
	prot++;
	SEXP cls = R_getClassDef (R_FUDGEMSG_CLASS);
	PROTECT (cls);
	prot++;
	SEXP obj = R_do_new_object (cls);
	PROTECT (obj);
	prot++;
	SEXP field = mkString (R_FUDGEMSG_POINTER);
	PROTECT (field);
	prot++;
	R_do_slot_assign (obj, field, msgptr);
	R_RegisterCFinalizerEx (msgptr, FudgeMsg_finalizer, FALSE);
	UNPROTECT (prot);
	return obj;
}

static FudgeMsg _GetFudgeMsgFromPointer (SEXP msgPointer) {
	FudgeMsg msg = (FudgeMsg)R_ExternalPtrAddr (msgPointer);
	if (!msg) {
		SEXP msgEncoded = R_ExternalPtrProtected (msgPointer);
		if (msgEncoded != R_NilValue) {
			PROTECT (msgEncoded);
			CFudgeMsgInfo *poMsg = CFudgeMsgInfo::GetMessage (RAW (msgEncoded), LENGTH (msgEncoded));
			if (poMsg) {
				msg = poMsg->GetMessage ();
				R_SetExternalPtrAddr (msgPointer, msg);
				// Don't release the pointer; R will need the reference counted
			}
			UNPROTECT (1);
		}
	}
	// Safe to pass NULL to FudgeMsg_retain
	FudgeMsg_retain (msg);
	return msg;
}

static FudgeMsg _GetFudgeMsgFromObject (SEXP msgValue) {
	FudgeMsg msg = NULL;
	SEXP slot = mkString (R_FUDGEMSG_POINTER);
	PROTECT (slot);
	if (R_has_slot (msgValue, slot)) {
		SEXP msgPointer = R_do_slot (msgValue, slot);
		msg = _GetFudgeMsgFromPointer (msgPointer);
	}
	UNPROTECT (1);
	return msg;
}

FudgeMsg RFudgeMsg::ToFudgeMsg (const CRCallback *poR, SEXP value) {
	if (isObject (value)) {
		SEXP cls = getAttrib (value, install (R_CLASS));
		if (isString (cls)) {
			if (!strcmp (CHAR (STRING_ELT (cls, 0)), R_FUDGEMSG_CLASS)) {
				FudgeMsg msg = _GetFudgeMsgFromObject (value);
				if (msg) {
					return msg;
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
				}
			} else {
				LOGDEBUG ("Class " << CHAR (STRING_ELT (cls, 0)) << " not a FudgeMsg");
				value = poR->ToFudgeMsg (value);
				if (value != R_NilValue) {
					FudgeMsg msg = _GetFudgeMsgFromObject (value);
					if (msg) {
						return msg;
					} else {
						LOGERROR (ERR_INTERNAL);
					}
				}
			}
		} else {
			LOGERROR (ERR_PARAMETER_VALUE);
		}
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return NULL;
}

static SEXP _FieldValue (FudgeField *pField) {
	SEXP elem = R_NilValue;
	switch (pField->type) {
	case FUDGE_TYPE_INDICATOR :
		elem = mkString (R_FUDGEINDICATOR);
		break;
	case FUDGE_TYPE_BOOLEAN :
		elem = ScalarLogical (pField->data.boolean);
		break;
	case FUDGE_TYPE_BYTE :
		elem = ScalarInteger (pField->data.byte);
		break;
	case FUDGE_TYPE_SHORT :
		elem = ScalarInteger (pField->data.i16);
		break;
	case FUDGE_TYPE_INT :
		elem = ScalarInteger (pField->data.i32);
		break;
	case FUDGE_TYPE_LONG :
		elem = ScalarReal (pField->data.i64);
		break;
	case FUDGE_TYPE_FLOAT :
		elem = ScalarReal (pField->data.f32);
		break;
	case FUDGE_TYPE_DOUBLE :
		elem = ScalarReal (pField->data.f64);
		break;
	case FUDGE_TYPE_BYTE_ARRAY :
		elem = _CreateByteArray (pField->data.bytes, pField->numbytes / sizeof (fudge_byte));
		break;
	case FUDGE_TYPE_SHORT_ARRAY :
		elem = _CreateShortArray (pField->data.bytes, pField->numbytes / sizeof (fudge_i16));
		break;
	case FUDGE_TYPE_INT_ARRAY :
		elem = _CreateIntArray (pField->data.bytes, pField->numbytes / sizeof (fudge_i32));
		break;
	case FUDGE_TYPE_LONG_ARRAY :
		elem = _CreateLongArray (pField->data.bytes, pField->numbytes / sizeof (fudge_i64));
		break;
	case FUDGE_TYPE_FLOAT_ARRAY :
		elem = _CreateFloatArray (pField->data.bytes, pField->numbytes / sizeof (fudge_f32));
		break;
	case FUDGE_TYPE_DOUBLE_ARRAY :
		elem = _CreateDoubleArray (pField->data.bytes, pField->numbytes / sizeof (fudge_f64));
		break;
	case FUDGE_TYPE_STRING : {
		char *psz;
		if (FudgeString_convertToASCIIZ (&psz,pField->data.string) == FUDGE_OK) {
			elem = mkString (psz);
			free (psz);
		}
		break;
							 }
	case FUDGE_TYPE_FUDGE_MSG :
		elem = RFudgeMsg::FromFudgeMsg (pField->data.message);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_4 :
		elem = _CreateByteArray (pField->data.bytes, 4);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_8 :
		elem = _CreateByteArray (pField->data.bytes, 8);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_16 :
		elem = _CreateByteArray (pField->data.bytes, 16);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_20 :
		elem = _CreateByteArray (pField->data.bytes, 20);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_32 :
		elem = _CreateByteArray (pField->data.bytes, 32);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_64 :
		elem = _CreateByteArray (pField->data.bytes, 64);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_128 :
		elem = _CreateByteArray (pField->data.bytes, 128);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_256 :
		elem = _CreateByteArray (pField->data.bytes, 256);
		break;
	case FUDGE_TYPE_BYTE_ARRAY_512 :
		elem = _CreateByteArray (pField->data.bytes, 512);
		break;
	case FUDGE_TYPE_DATE : {
		char sz[32];
		StringCbPrintfA (sz, sizeof (sz), "%04d-%02d-%02d", pField->data.datetime.date.year, pField->data.datetime.date.month, pField->data.datetime.date.day);
		elem = mkString (sz);
		// TODO: return a proper R date object
		break;
						   }
	case FUDGE_TYPE_TIME : {
		char sz[32];
		StringCbPrintfA (sz, sizeof (sz), "%d:%02d:%02d.%09d", pField->data.datetime.time.seconds / 3600, (pField->data.datetime.time.seconds / 60) % 60, pField->data.datetime.time.seconds % 60, pField->data.datetime.time.nanoseconds);
		elem = mkString (sz);
		// TODO: return a proper R time object
		break;
						   }
	case FUDGE_TYPE_DATETIME : {
		char sz[32];
		StringCbPrintfA (sz, sizeof (sz), "%04d-%02d-%02d %d:%02d:%02d.%09d", pField->data.datetime.date.year, pField->data.datetime.date.month, pField->data.datetime.date.day, pField->data.datetime.time.seconds / 3600, (pField->data.datetime.time.seconds / 60) % 60, pField->data.datetime.time.seconds % 60, pField->data.datetime.time.nanoseconds);
		elem = mkString (sz);
		// TODO: return a proper R datetime object
		break;
							   }
	default : {
		char sz[32];
		StringCbPrintfA (sz, sizeof (sz), "Unknown Fudge type %d", pField->type);
		elem = mkString (sz);
		break;
			  }
	}
	return elem;
}

static void _GetField (SEXP strName, SEXP strOrdinal, SEXP strValue, SEXP result, int nResult, FudgeField *pField) {
	int nListSize = 1;
	if (pField->flags & FUDGE_FIELD_HAS_NAME) nListSize++;
	if (pField->flags & FUDGE_FIELD_HAS_ORDINAL) nListSize++;
	SEXP field = allocList (nListSize);
	SET_VECTOR_ELT (result, nResult, field);
	if (pField->flags & FUDGE_FIELD_HAS_NAME) {
		char *psz;
		if (FudgeString_convertToASCIIZ (&psz, pField->name) == FUDGE_OK) {
			SEXP name = mkString (psz);
			free (psz);
			SETCAR (field, name);
			SET_TAG (field, strName);
			field = CDR (field);
		}
	}
	if (pField->flags & FUDGE_FIELD_HAS_ORDINAL) {
		SETCAR (field, ScalarInteger (pField->ordinal));
		SET_TAG (field, strOrdinal);
		field = CDR (field);
	}
	SEXP elem = _FieldValue (pField);
	SETCAR (field, elem);
	SET_TAG (field, strValue);
}

static void _GetValue (SEXP result, int nResult, FudgeField *pField) {
	SEXP elem = _FieldValue (pField);
	SET_VECTOR_ELT (result, nResult, elem);
}

SEXP RFudgeMsg::GetAllFields (SEXP message) {
	SEXP result = R_NilValue;
	FudgeMsg msg = _GetFudgeMsgFromPointer (message);
	if (msg) {
		int nFields = FudgeMsg_numFields (msg);
		FudgeField *aFields = new FudgeField[nFields];
		if (aFields) {
			if (FudgeMsg_getFields (aFields, nFields, msg) == nFields) {
				result = allocVector (VECSXP, nFields);
				PROTECT (result);
				SEXP strValue = install (R_FUDGEFIELD_VALUE);
				PROTECT (strValue);
				SEXP strName = install (R_FUDGEFIELD_NAME);
				PROTECT (strName);
				SEXP strOrdinal = install (R_FUDGEFIELD_ORDINAL);
				PROTECT (strOrdinal);
				int n;
				for (n = 0; n < nFields; n++) {
				    _GetField (strName, strOrdinal, strValue, result, n, aFields + n);
				}
				UNPROTECT (4);
			} else {
				LOGFATAL (ERR_INTERNAL);
			}
			delete aFields;
		} else {
			LOGFATAL (ERR_MEMORY);
		}
		FudgeMsg_release (msg);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

SEXP RFudgeMsg::GetAllValues (SEXP message) {
	SEXP result = R_NilValue;
	FudgeMsg msg = _GetFudgeMsgFromPointer (message);
	if (msg) {
		int nFields = FudgeMsg_numFields (msg);
		FudgeField *aFields = new FudgeField[nFields];
		if (aFields) {
			if (FudgeMsg_getFields (aFields, nFields, msg) == nFields) {
				result = allocVector (VECSXP, nFields);
				PROTECT (result);
				int n;
				for (n = 0; n < nFields; n++) {
					_GetValue (result, n, aFields + n);
				}
				UNPROTECT (1);
			} else {
				LOGFATAL (ERR_INTERNAL);
			}
			delete aFields;
		} else {
			LOGFATAL (ERR_MEMORY);
		}
		FudgeMsg_release (msg);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

static FudgeString _GetFudgeString (SEXP value) {
	FudgeString result = NULL;
	if (TYPEOF (value) == STRSXP) {
		if (length (value) == 1) {
			const char *pszString = CHAR (STRING_ELT (value, 0));
			FudgeString_createFromASCIIZ (&result, pszString);
		}
	}
	return result;
}

SEXP RFudgeMsg::GetFieldsByName (SEXP message, SEXP name) {
	SEXP result = R_NilValue;
	FudgeMsg msg = _GetFudgeMsgFromPointer (message);
	if (msg) {
		FudgeString fieldName = _GetFudgeString (name);
		if (fieldName) {
			int nFields = FudgeMsg_numFields (msg);
			FudgeField *aFields = new FudgeField[nFields];
			if (aFields) {
				if (FudgeMsg_getFields (aFields, nFields, msg) == nFields) {
					int n, nMatches = 0;
					for (n = 0; n < nFields; n++) {
						if ((aFields[n].flags & FUDGE_FIELD_HAS_NAME) && !FudgeString_compare (aFields[n].name, fieldName)) {
							nMatches++;
						}
					}
					result = allocVector (VECSXP, nMatches);
					if (result != R_NilValue) {
						PROTECT (result);
						SEXP strValue = install (R_FUDGEFIELD_VALUE);
						PROTECT (strValue);
						SEXP strName = install (R_FUDGEFIELD_NAME);
						PROTECT (strName);
						SEXP strOrdinal = install (R_FUDGEFIELD_ORDINAL);
						PROTECT (strOrdinal);
						nMatches = 0;
						for (n = 0; n < nFields; n++) {
							if ((aFields[n].flags & FUDGE_FIELD_HAS_NAME) && !FudgeString_compare (aFields[n].name, fieldName)) {
								_GetField (strName, strOrdinal, strValue, result, nMatches++, aFields + n);
							}
						}
						UNPROTECT (4);
					} else {
						LOGERROR (ERR_R_FUNCTION);
					}
				} else {
					LOGERROR (ERR_INTERNAL);
				}
				delete aFields;
			} else {
				LOGFATAL (ERR_MEMORY);
			}
			FudgeString_release (fieldName);
		} else {
			LOGERROR (ERR_PARAMETER_VALUE);
		}
		FudgeMsg_release (msg);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

SEXP RFudgeMsg::GetValuesByName (SEXP message, SEXP name) {
	SEXP result = R_NilValue;
	FudgeMsg msg = _GetFudgeMsgFromPointer (message);
	if (msg) {
		FudgeString fieldName = _GetFudgeString (name);
		if (fieldName) {
			int nFields = FudgeMsg_numFields (msg);
			FudgeField *aFields = new FudgeField[nFields];
			if (aFields) {
				if (FudgeMsg_getFields (aFields, nFields, msg) == nFields) {
					int n, nMatches = 0;
					for (n = 0; n < nFields; n++) {
						if ((aFields[n].flags & FUDGE_FIELD_HAS_NAME) && !FudgeString_compare (aFields[n].name, fieldName)) {
							nMatches++;
						}
					}
					result = allocVector (VECSXP, nMatches);
					if (result != R_NilValue) {
						PROTECT (result);
						nMatches = 0;
						for (n = 0; n < nFields; n++) {
							if ((aFields[n].flags & FUDGE_FIELD_HAS_NAME) && !FudgeString_compare (aFields[n].name, fieldName)) {
								_GetValue (result, nMatches++, aFields + n);
							}
						}
						UNPROTECT (1);
					} else {
						LOGERROR (ERR_R_FUNCTION);
					}
				} else {
					LOGERROR (ERR_INTERNAL);
				}
				delete aFields;
			} else {
				LOGFATAL (ERR_MEMORY);
			}
			FudgeString_release (fieldName);
		} else {
			LOGERROR (ERR_PARAMETER_VALUE);
		}
		FudgeMsg_release (msg);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

SEXP RFudgeMsg::GetFieldsByOrdinal (SEXP message, SEXP ordinal) {
	SEXP result = R_NilValue;
	FudgeMsg msg = _GetFudgeMsgFromPointer (message);
	if (msg) {
		int nOrdinal = *INTEGER (ordinal);
		int nFields = FudgeMsg_numFields (msg);
		FudgeField *aFields = new FudgeField[nFields];
		if (aFields) {
			if (FudgeMsg_getFields (aFields, nFields, msg) == nFields) {
				int n, nMatches = 0;
				for (n = 0; n < nFields; n++) {
					if ((aFields[n].flags & FUDGE_FIELD_HAS_ORDINAL) && (aFields[n].ordinal == nOrdinal)) {
						nMatches++;
					}
				}
				result = allocVector (VECSXP, nMatches);
				if (result != R_NilValue) {
					PROTECT (result);
					SEXP strValue = install (R_FUDGEFIELD_VALUE);
					PROTECT (strValue);
					SEXP strName = install (R_FUDGEFIELD_NAME);
					PROTECT (strName);
					SEXP strOrdinal = install (R_FUDGEFIELD_ORDINAL);
					PROTECT (strOrdinal);
					nMatches = 0;
					for (n = 0; n < nFields; n++) {
						if ((aFields[n].flags & FUDGE_FIELD_HAS_ORDINAL) && (aFields[n].ordinal == nOrdinal)) {
							_GetField (strName, strOrdinal, strValue, result, nMatches++, aFields + n);
						}
					}
					UNPROTECT (4);
				} else {
					LOGERROR (ERR_INTERNAL);
				}
			} else {
				LOGERROR (ERR_INTERNAL);
			}
			delete aFields;
		} else {
			LOGFATAL (ERR_MEMORY);
		}
		FudgeMsg_release (msg);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

SEXP RFudgeMsg::GetValuesByOrdinal (SEXP message, SEXP ordinal) {
	SEXP result = R_NilValue;
	FudgeMsg msg = _GetFudgeMsgFromPointer (message);
	if (msg) {
		int nOrdinal = *INTEGER (ordinal);
		int nFields = FudgeMsg_numFields (msg);
		FudgeField *aFields = new FudgeField[nFields];
		if (aFields) {
			if (FudgeMsg_getFields (aFields, nFields, msg) == nFields) {
				int n, nMatches = 0;
				for (n = 0; n < nFields; n++) {
					if ((aFields[n].flags & FUDGE_FIELD_HAS_ORDINAL) && (aFields[n].ordinal == nOrdinal)) {
						nMatches++;
					}
				}
				result = allocVector (VECSXP, nMatches);
				if (result != R_NilValue) {
					PROTECT (result);
					nMatches = 0;
					for (n = 0; n < nFields; n++) {
						if ((aFields[n].flags & FUDGE_FIELD_HAS_ORDINAL) && (aFields[n].ordinal == nOrdinal)) {
							_GetValue (result, nMatches++, aFields + n);
						}
					}
					UNPROTECT (1);
				} else {
					LOGERROR (ERR_INTERNAL);
				}
			} else {
				LOGERROR (ERR_INTERNAL);
			}
			delete aFields;
		} else {
			LOGFATAL (ERR_MEMORY);
		}
		FudgeMsg_release (msg);
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

SEXP RFudgeMsg::SetSerialiseMode (SEXP on) {
	if (isLogical (on)) {
		g_bSerialise = *INTEGER (on);
		// TODO: if serialisation has been turned on, then scanning through for existing objects that don't have tags would be good
	} else {
		LOGERROR (ERR_PARAMETER_TYPE);
	}
	return R_NilValue;
}
