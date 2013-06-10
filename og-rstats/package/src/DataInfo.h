/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_datainfo_h
#define __inc_og_rstats_package_datainfo_h

#include "com_opengamma_rstats_msg_DataInfo.h"
#include "RCallback.h"

/// Helper functions for converting the OG-Language DataInfo construct to/from the R SEXP structure.
class CDataInfo {
private:

	/// Prevents instantiation.
	CDataInfo () { }

	/// Prevents instantiation.
	~CDataInfo () { }

public:

	/// Release the memory allocated for a DataInfo.
	///
	/// @param[in] pInfo structure to release
	static void Release (com_opengamma_rstats_msg_DataInfo *pInfo) { com_opengamma_rstats_msg_DataInfo_free (pInfo); }

	static SEXP Apply (const CRCallback *poR, SEXP rawResult, const com_opengamma_rstats_msg_DataInfo *pInfo);
};

#endif /* ifndef __inc_og_rstats_package_datainfo_h */
