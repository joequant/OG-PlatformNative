/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_datainfo_h
#define __inc_og_rstats_package_datainfo_h

#include "com_opengamma_rstats_msg_DataInfo.h"
#include "RCallback.h"

class CDataInfo {
private:
	CDataInfo () { }
	~CDataInfo () { }
public:
	static SEXP Apply (const CRCallback *poR, SEXP rawResult, const com_opengamma_rstats_msg_DataInfo *pInfo);
	static void Release (com_opengamma_rstats_msg_DataInfo *pInfo) { com_opengamma_rstats_msg_DataInfo_free (pInfo); }
};

#endif /* ifndef __inc_og_rstats_package_datainfo_h */
