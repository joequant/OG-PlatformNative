/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "DataInfo.h"
#include "Errors.h"
#include "RCallback.h"

LOGGING (com.opengamma.rstats.package.DataInfo);

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
