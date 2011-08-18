/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef STDAFX_H
#define STDAFX_H

#define _T(str)			#str
#define Client(path)	_T(Client_##path)

#undef STDAFX_H
#include Client(stdafx.h)

// Define it here as we might need __cdecl, __stdcall, etc ... on some platforms
#define RPROC

#include <R.h>
#include <Rinternals.h>
#include <R_ext/Rdynload.h>
#include <log4cxx/helpers/loglog.h>

#endif /* ifndef STDAFX_H */
