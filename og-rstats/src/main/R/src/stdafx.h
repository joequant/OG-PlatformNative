/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef STDAFX_H
#define STDAFX_H

#include <Util/Quote.h>
#define Client(path)	QUOTE_(Client_##path)

#undef STDAFX_H
#include Client(stdafx.h)

// Define it here as we might need __cdecl, __stdcall, etc ... on some platforms
#define RPROC

#include <R.h>
#include <Rinternals.h>
#include <R_ext/Rdynload.h>
#ifdef __cplusplus
#include <log4cxx/helpers/loglog.h>
#ifdef _WIN32
#include <hash_map>
#else /* ifdef _WIN32 */
#include <apr-1/apr_hash.h>
#endif /* ifdef _WIN32 */
#endif /* ifdef __cplusplus */

#endif /* ifndef STDAFX_H */
