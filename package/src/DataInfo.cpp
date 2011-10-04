/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "DataInfo.h"
#include "Errors.h"
#include "RCallback.h"

LOGGING (com.opengamma.rstats.package.DataInfo);

/// Applies the content of a DataInfo metadata message to the SEXP value. If the metadata contains a wrapper
/// class name, the interop function declared on that class is used to convert the value from its R/Java
/// encoding to a rich R object.
///
/// @param[in] poR callback service for invoking R (defined on the original caller's environment)
/// @param[in] rawResult the original result to process
/// @param[in] pInfo metadata to apply, never NULL
/// @return the new result if there was an interop conversion, otherwise the original result object
SEXP CDataInfo::Apply (const CRCallback *poR, SEXP rawResult, const com_opengamma_rstats_msg_DataInfo *pInfo) {
	int nUnprotect = 0;
	if (pInfo->_wrapperClass) {
		SEXP newResult = poR->InteropConvert (rawResult, pInfo->_wrapperClass);
		if (newResult != R_NilValue) {
			PROTECT (newResult);
			rawResult = newResult;
			nUnprotect++;
		}
	}
	if (nUnprotect) {
		UNPROTECT (nUnprotect);
	}
	return rawResult;
}
