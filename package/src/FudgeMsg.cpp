/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "FudgeMsg.h"
#include "Errors.h"
#include "RCallback.h"

LOGGING (com.opengamma.rstats.package.FudgeMsg);

#define R_FUDGEMSG_CLASS		"FudgeMsg"
#define R_FUDGEMSG_POINTER		"message"
#define R_FUDGEFIELD_VALUE		"Value"
#define R_FUDGEFIELD_ORDINAL	"Ordinal"
#define R_FUDGEFIELD_NAME		"Name"
#define R_FUDGEINDICATOR		"indicator"

static SEXP _CreateByteArray (const fudge_byte *bytes, int elements) {
	LOGWARN (TEXT ("byte[") << elements << TEXT ("] array"));
	// TODO
	return R_NilValue;
}

static SEXP _CreateShortArray (const fudge_byte *bytes, int elements) {
	LOGWARN (TEXT ("short[") << elements << TEXT ("] array"));
	// TODO
	return R_NilValue;
}

static SEXP _CreateIntArray (const fudge_byte *bytes, int elements) {
	LOGWARN (TEXT ("int[") << elements << TEXT ("] array"));
	// TODO
	return R_NilValue;
}

static SEXP _CreateLongArray (const fudge_byte *bytes, int elements) {
	LOGWARN (TEXT ("long[") << elements << TEXT ("] array"));
	// TODO
	return R_NilValue;
}

static SEXP _CreateFloatArray (const fudge_byte *bytes, int elements) {
	LOGWARN (TEXT ("float[") << elements << TEXT ("] array"));
	// TODO
	return R_NilValue;
}

static SEXP _CreateDoubleArray (const fudge_byte *bytes, int elements) {
	LOGWARN (TEXT ("double[") << elements << TEXT ("] array"));
	// TODO
	return R_NilValue;
}

static void RPROC FudgeMsg_finalizer (SEXP msgptr) {
	void *ptr = R_ExternalPtrAddr (msgptr);
	if (ptr) {
		FudgeMsg msg = (FudgeMsg)ptr;
		if (FudgeMsg_release (msg) == FUDGE_OK) {
			LOGDEBUG (TEXT ("Message pointer released"));
			R_ClearExternalPtr (msgptr);
		} else {
			LOGERROR (ERR_INTERNAL);
		}
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
}

SEXP RFudgeMsg::FromFudgeMsg (FudgeMsg msg) {
	FudgeMsg_retain (msg);
	SEXP msgptr = R_MakeExternalPtr (msg, R_NilValue, R_NilValue);
	PROTECT (msgptr);
	SEXP cls = R_getClassDef (R_FUDGEMSG_CLASS);
	PROTECT (cls);
	SEXP obj = R_do_new_object (cls);
	PROTECT (obj);
	SEXP field = mkString (R_FUDGEMSG_POINTER);
	PROTECT (field);
	R_do_slot_assign (obj, field, msgptr);
	R_RegisterCFinalizerEx (msgptr, FudgeMsg_finalizer, FALSE);
	UNPROTECT (4);
	return obj;
}

static FudgeMsg _GetFudgeMsg (SEXP msgValue) {
	FudgeMsg msg = NULL;
	SEXP slot = mkString (R_FUDGEMSG_POINTER);
	PROTECT (slot);
	if (R_has_slot (msgValue, slot)) {
		SEXP msgPointer = R_do_slot (msgValue, slot);
		msg = (FudgeMsg)R_ExternalPtrAddr (msgPointer);
		// Safe to pass NULL to FudgeMsg_retain
		FudgeMsg_retain (msg);
	}
	UNPROTECT (1);
	return msg;
}

FudgeMsg RFudgeMsg::ToFudgeMsg (const CRCallback *poR, SEXP value) {
	if (isObject (value)) {
		SEXP cls = getAttrib (value, install ("class"));
		if (isString (cls)) {
			if (!strcmp (CHAR (STRING_ELT (cls, 0)), R_FUDGEMSG_CLASS)) {
				FudgeMsg msg = _GetFudgeMsg (value);
				if (msg) {
					return msg;
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
				}
			} else {
				LOGDEBUG ("Class " << CHAR (STRING_ELT (cls, 0)) << " not a FudgeMsg");
				value = poR->ToFudgeMsg (value);
				if (value != R_NilValue) {
					FudgeMsg msg = _GetFudgeMsg (value);
					if (msg) {
						return msg;
					} else {
						LOGERROR (ERR_PARAMETER_VALUE);
					}
				} else {
					LOGERROR (ERR_PARAMETER_VALUE);
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

SEXP RFudgeMsg::GetAllFields (SEXP message) {
	SEXP result = R_NilValue;
	FudgeMsg msg = (FudgeMsg)R_ExternalPtrAddr (message);
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
				char sz[32], *psz;
				for (n = 0; n < nFields; n++) {
					int nListSize = 1;
					if (aFields[n].flags & FUDGE_FIELD_HAS_NAME) nListSize++;
					if (aFields[n].flags & FUDGE_FIELD_HAS_ORDINAL) nListSize++;
					SEXP field = allocList (nListSize);
					SET_VECTOR_ELT (result, n, field);
					if (aFields[n].flags & FUDGE_FIELD_HAS_NAME) {
						if (FudgeString_convertToASCIIZ (&psz, aFields[n].name) == FUDGE_OK) {
							SEXP name = mkString (psz);
							free (psz);
							SETCAR (field, name);
							SET_TAG (field, strName);
							field = CDR (field);
						}
					}
					if (aFields[n].flags & FUDGE_FIELD_HAS_ORDINAL) {
						SETCAR (field, ScalarInteger (aFields[n].ordinal));
						SET_TAG (field, strOrdinal);
						field = CDR (field);
					}
					SEXP elem = R_NilValue;
					switch (aFields[n].type) {
					case FUDGE_TYPE_INDICATOR :
						elem = mkString (R_FUDGEINDICATOR);
						break;
					case FUDGE_TYPE_BOOLEAN :
						elem = ScalarLogical (aFields[n].data.boolean);
						break;
					case FUDGE_TYPE_BYTE :
						elem = ScalarInteger (aFields[n].data.byte);
						break;
					case FUDGE_TYPE_SHORT :
						elem = ScalarInteger (aFields[n].data.i16);
						break;
					case FUDGE_TYPE_INT :
						elem = ScalarInteger (aFields[n].data.i32);
						break;
					case FUDGE_TYPE_LONG :
						elem = ScalarReal (aFields[n].data.i64);
						break;
					case FUDGE_TYPE_FLOAT :
						elem = ScalarReal (aFields[n].data.f32);
						break;
					case FUDGE_TYPE_DOUBLE :
						elem = ScalarReal (aFields[n].data.f64);
						break;
					case FUDGE_TYPE_BYTE_ARRAY :
						elem = _CreateByteArray (aFields[n].data.bytes, aFields[n].numbytes / sizeof (fudge_byte));
						break;
					case FUDGE_TYPE_SHORT_ARRAY :
						elem = _CreateShortArray (aFields[n].data.bytes, aFields[n].numbytes / sizeof (fudge_i16));
						break;
					case FUDGE_TYPE_INT_ARRAY :
						elem = _CreateIntArray (aFields[n].data.bytes, aFields[n].numbytes / sizeof (fudge_i32));
						break;
					case FUDGE_TYPE_LONG_ARRAY :
						elem = _CreateLongArray (aFields[n].data.bytes, aFields[n].numbytes / sizeof (fudge_i64));
						break;
					case FUDGE_TYPE_FLOAT_ARRAY :
						elem = _CreateFloatArray (aFields[n].data.bytes, aFields[n].numbytes / sizeof (fudge_f32));
						break;
					case FUDGE_TYPE_DOUBLE_ARRAY :
						elem = _CreateDoubleArray (aFields[n].data.bytes, aFields[n].numbytes / sizeof (fudge_f64));
						break;
					case FUDGE_TYPE_STRING :
						if (FudgeString_convertToASCIIZ (&psz, aFields[n].data.string) == FUDGE_OK) {
							elem = mkString (psz);
							free (psz);
						}
						break;
					case FUDGE_TYPE_FUDGE_MSG :
						elem = FromFudgeMsg (aFields[n].data.message);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_4 :
						elem = _CreateByteArray (aFields[n].data.bytes, 4);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_8 :
						elem = _CreateByteArray (aFields[n].data.bytes, 8);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_16 :
						elem = _CreateByteArray (aFields[n].data.bytes, 16);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_20 :
						elem = _CreateByteArray (aFields[n].data.bytes, 20);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_32 :
						elem = _CreateByteArray (aFields[n].data.bytes, 32);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_64 :
						elem = _CreateByteArray (aFields[n].data.bytes, 64);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_128 :
						elem = _CreateByteArray (aFields[n].data.bytes, 128);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_256 :
						elem = _CreateByteArray (aFields[n].data.bytes, 256);
						break;
					case FUDGE_TYPE_BYTE_ARRAY_512 :
						elem = _CreateByteArray (aFields[n].data.bytes, 512);
						break;
					case FUDGE_TYPE_DATE :
						StringCbPrintfA (sz, sizeof (sz), "%04d-%02d-%02d", aFields[n].data.datetime.date.year, aFields[n].data.datetime.date.month, aFields[n].data.datetime.date.day);
						elem = mkString (sz);
						break;
					case FUDGE_TYPE_TIME :
						StringCbPrintfA (sz, sizeof (sz), "%d.%d", aFields[n].data.datetime.time.seconds, aFields[n].data.datetime.time.nanoseconds);
						elem = mkString (sz);
						break;
					case FUDGE_TYPE_DATETIME :
						StringCbPrintfA (sz, sizeof (sz), "%04d-%02d-%02d %d.%d", aFields[n].data.datetime.date.year, aFields[n].data.datetime.date.month, aFields[n].data.datetime.date.day, aFields[n].data.datetime.time.seconds, aFields[n].data.datetime.time.nanoseconds);
						elem = mkString (sz);
						break;
					default :
						StringCbPrintfA (sz, sizeof (sz), "Unknown Fudge type %d", aFields[n].type);
						elem = mkString (sz);
						break;
					}
					SETCAR (field, elem);
					SET_TAG (field, strValue);
				}
				UNPROTECT (4);
			} else {
				LOGFATAL (ERR_INTERNAL);
			}
			delete aFields;
		} else {
			LOGFATAL (ERR_MEMORY);
		}
	} else {
		LOGERROR (ERR_PARAMETER_VALUE);
	}
	return result;
}

