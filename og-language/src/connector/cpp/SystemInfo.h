/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_systeminfo_h
#define __inc_og_language_connector_systeminfo_h

#include "RequestBuilder.h"
#include "com_opengamma_language_config_SystemInfo.h"

#ifndef CLASS_com_opengamma_language_config_SystemInfo
# define CLASS_com_opengamma_language_config_SystemInfo com_opengamma_language_config_SystemInfo
#endif /* ifndef CLASS_com_opengamma_language_config_SystemInfo */

/// Message builder for SystemInfo query.
REQUESTBUILDER_BEGIN (CSystemInfoQuery)
	REQUESTBUILDER_REQUEST (CLASS_com_opengamma_language_config_SystemInfo)
private:
	fudge_i32 m_nField;
public:
	void SetQueryField (int nField) {
		m_request.fudgeCountGet = 1;
		m_request._get = &m_nField;
		m_nField = nField;
	}
	REQUESTBUILDER_RESPONSE (CLASS_com_opengamma_language_config_SystemInfo)
REQUESTBUILDER_END

#endif /* ifndef __inc_og_language_connector_systeminfo_h */
