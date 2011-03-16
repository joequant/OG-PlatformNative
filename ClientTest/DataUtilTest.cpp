/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Test the objects and functions in Client/DataUtil.cpp

#include "Client/DataUtil.h"

LOGGING (com.opengamma.rstats.client.DataUtilTest);

static com_opengamma_language_Value *Alloc () {
	com_opengamma_language_Value *pValue = new com_opengamma_language_Value;
	ASSERT (pValue);
	memset (pValue, 0, sizeof (com_opengamma_language_Value));
	return pValue;
}

static com_opengamma_language_Value *Boolean (fudge_bool value = FUDGE_TRUE) {
	com_opengamma_language_Value *pValue = Alloc ();
	ASSERT (pValue->_boolValue = new fudge_bool);
	*pValue->_boolValue = value;
	return pValue;
}

static com_opengamma_language_Value *Integer (fudge_i32 value = 42) {
	com_opengamma_language_Value *pValue = Alloc ();
	ASSERT (pValue->_intValue = new fudge_i32);
	*pValue->_intValue = value;
	return pValue;
}

static com_opengamma_language_Value *Double (fudge_f64 value = 3.141) {
	com_opengamma_language_Value *pValue = Alloc ();
	ASSERT (pValue->_doubleValue = new fudge_f64);
	*pValue->_doubleValue = value;
	return pValue;
}

static com_opengamma_language_Value *String (const TCHAR *value = TEXT ("Foo")) {
	com_opengamma_language_Value *pValue = Alloc ();
	ASSERT (pValue->_stringValue = _tcsdup (value));
	return pValue;
}

static com_opengamma_language_Value *Message () {
	com_opengamma_language_Value *pValue = Alloc ();
	ASSERT (FudgeMsg_create (&pValue->_messageValue) == FUDGE_OK);
	return pValue;
}

static com_opengamma_language_Value *Error () {
	com_opengamma_language_Value *pValue = Alloc ();
	ASSERT (pValue->_errorValue = new fudge_i32);
	ASSERT (pValue->_stringValue = _tcsdup (TEXT ("Error message")));
	return pValue;
}

static void TypeOf () {
	ASSERT (CDataUtil::TypeOf (Boolean ()) == DATATYPE_BOOLEAN);
	ASSERT (CDataUtil::TypeOf (Integer ()) == DATATYPE_INTEGER);
	ASSERT (CDataUtil::TypeOf (Double ()) == DATATYPE_DOUBLE);
	ASSERT (CDataUtil::TypeOf (String ()) == DATATYPE_STRING);
	ASSERT (CDataUtil::TypeOf (Message ()) == DATATYPE_MESSAGE);
	ASSERT (CDataUtil::TypeOf (Error ()) == DATATYPE_ERROR);
	com_opengamma_language_Value *integers[] = { Integer (), Integer (), Integer (), NULL };
	int nLength1, nLength2;
	ASSERT (CDataUtil::TypeOf (integers, &nLength1) == DATATYPE_INTEGER);
	ASSERT (nLength1 == 3);
	com_opengamma_language_Value *strings[] = { String (), String (), NULL };
	ASSERT (CDataUtil::TypeOf (strings, &nLength1) == DATATYPE_STRING);
	ASSERT (nLength1 == 2);
	com_opengamma_language_Value *mixed1[] = { Integer (), Double (), String (), Boolean (), NULL };
	ASSERT (CDataUtil::TypeOf (mixed1, &nLength1) == (DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING | DATATYPE_BOOLEAN));
	ASSERT (nLength1 == 4);
	com_opengamma_language_Value *mixed2[] = { Integer (), Error (), Integer (), NULL };
	ASSERT (CDataUtil::TypeOf (mixed2) == (DATATYPE_INTEGER | DATATYPE_ERROR));
	com_opengamma_language_Value **integerMatrix[] = { integers, integers, NULL };
	ASSERT (CDataUtil::TypeOf (integerMatrix) == DATATYPE_INTEGER);
	com_opengamma_language_Value **stringMatrix[] = { strings, strings, NULL };
	ASSERT (CDataUtil::TypeOf (stringMatrix, &nLength1) == DATATYPE_STRING);
	ASSERT (nLength1 == 2);
	com_opengamma_language_Value **mixedMatrix[] = { strings, integers, mixed1, NULL };
	ASSERT (CDataUtil::TypeOf (mixedMatrix, &nLength1, &nLength2) == (DATATYPE_STRING | DATATYPE_INTEGER | DATATYPE_BOOLEAN | DATATYPE_DOUBLE));
	ASSERT (nLength2 == 4);
}

static void SingleType () {
	ASSERT (CDataUtil::SingleType (0) == 0);
	ASSERT (CDataUtil::SingleType (DATATYPE_BOOLEAN | DATATYPE_STRING) == DATATYPE_STRING);
	ASSERT (CDataUtil::SingleType (DATATYPE_ERROR | DATATYPE_MESSAGE) == DATATYPE_ERROR);
	ASSERT (CDataUtil::SingleType (DATATYPE_INTEGER | DATATYPE_DOUBLE) == DATATYPE_DOUBLE);
}

static void CanCoerce () {
	ASSERT (CDataUtil::CanCoerce (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING, DATATYPE_BOOLEAN));
	ASSERT (CDataUtil::CanCoerce (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING, DATATYPE_INTEGER));
	ASSERT (CDataUtil::CanCoerce (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING, DATATYPE_STRING));
	ASSERT (CDataUtil::CanCoerce (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING, DATATYPE_STRING));
	ASSERT (!CDataUtil::CanCoerce (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING | DATATYPE_MESSAGE, DATATYPE_INTEGER));
	ASSERT (!CDataUtil::CanCoerce (DATATYPE_BOOLEAN | DATATYPE_INTEGER | DATATYPE_DOUBLE | DATATYPE_STRING, DATATYPE_MESSAGE));
}

#define COERCE_INIT \
	com_opengamma_language_Value *v1 = Integer (1); \
	com_opengamma_language_Value *v2 = String ("1000"); \
	com_opengamma_language_Value *v3 = Boolean (FUDGE_FALSE); \
	com_opengamma_language_Value *v4 = Double (1.0); \
	com_opengamma_language_Value *r1[] = { v1, v2, NULL }; \
	com_opengamma_language_Value *r2[] = { v3, v4, NULL }; \
	com_opengamma_language_Value **m[] = { r1, r2, NULL };
#define COERCE_DONE \
	com_opengamma_language_Value_free (v1); \
	com_opengamma_language_Value_free (v2); \
	com_opengamma_language_Value_free (v3); \
	com_opengamma_language_Value_free (v4);

static void Coerce () {
	{
		COERCE_INIT
		ASSERT (CDataUtil::Coerce (DATATYPE_INTEGER, m));
		ASSERT (CDataUtil::TypeOf (m) == DATATYPE_INTEGER);
		ASSERT (*v1->_intValue == 1);
		ASSERT (!v2->_stringValue);
		ASSERT (*v2->_intValue == 1000);
		ASSERT (!v3->_boolValue);
		ASSERT (*v3->_intValue == 0);
		ASSERT (!v4->_doubleValue);
		ASSERT (*v4->_intValue == 1);
		COERCE_DONE
	}
	{
		COERCE_INIT
		ASSERT (CDataUtil::Coerce (DATATYPE_DOUBLE, m));
		ASSERT (CDataUtil::TypeOf (m) == DATATYPE_DOUBLE);
		ASSERT (!v1->_intValue);
		ASSERT (*v1->_doubleValue == 1.0);
		ASSERT (!v2->_stringValue);
		ASSERT (*v2->_doubleValue == 1000.0);
		ASSERT (!v3->_boolValue);
		ASSERT (*v3->_doubleValue == 0.0);
		ASSERT (*v4->_doubleValue == 1.0);
		COERCE_DONE
	}
	{
		COERCE_INIT
		ASSERT (CDataUtil::Coerce (DATATYPE_STRING, m));
		ASSERT (CDataUtil::TypeOf (m) == DATATYPE_STRING);
		ASSERT (!v1->_intValue);
		ASSERT (!_tcscmp (v1->_stringValue, TEXT ("1")));
		ASSERT (!_tcscmp (v2->_stringValue, TEXT ("1000")));
		ASSERT (!v3->_boolValue);
		ASSERT (!_tcscmp (v3->_stringValue, TEXT ("")));
		ASSERT (!v4->_doubleValue);
		LOGDEBUG (TEXT ("v4=") << v4->_stringValue);
		ASSERT (!_tcsncmp (v4->_stringValue, TEXT ("1.0"), 3));
		COERCE_DONE
	}
	{
		COERCE_INIT
		ASSERT (CDataUtil::Coerce (DATATYPE_BOOLEAN, m));
		ASSERT (CDataUtil::TypeOf (m) == DATATYPE_BOOLEAN);
		ASSERT (!v1->_intValue);
		ASSERT (*v1->_boolValue);
		ASSERT (!v2->_stringValue);
		ASSERT (*v2->_boolValue);
		ASSERT (!*v3->_boolValue);
		ASSERT (!v4->_doubleValue);
		ASSERT (*v4->_boolValue);
		COERCE_DONE
	}
	{
		COERCE_INIT
		ASSERT (!CDataUtil::Coerce (DATATYPE_MESSAGE, m));
		ASSERT (CDataUtil::TypeOf (m) == (DATATYPE_INTEGER | DATATYPE_STRING | DATATYPE_DOUBLE | DATATYPE_BOOLEAN));
		COERCE_DONE
	}
}

BEGIN_TESTS (DataUtilTest)
	TEST (TypeOf)
	TEST (SingleType)
	TEST (CanCoerce)
	TEST (Coerce)
END_TESTS
