/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_package_stdafx_h
#define __inc_og_pirate_package_stdafx_h

#define _T(str)			#str
#define Client(path)	_T(Client_##path)

#include Client(stdafx.h)

// Define it here as we might need __cdecl, __stdcall, etc ... on some platforms
#define RPROC

#include <R.h>
#include <Rinternals.h>
#include <R_ext/Rdynload.h>

#endif /* ifndef __inc_og_pirate_package_stdafx_h */
