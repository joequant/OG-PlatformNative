/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_globals_h
#define __inc_og_rstats_package_globals_h

#include Client(Functions.h)
#include Client(LiveData.h)
#include Client(Procedures.h)

#ifndef GLOBALS
#define GLOBAL extern
#else
#define GLOBAL
#endif /* ifndef GLOBALS */

GLOBAL const CFunctions *g_poFunctions;
GLOBAL const CLiveData *g_poLiveData;
GLOBAL const CProcedures *g_poProcedures;

#endif /* ifndef __inc_og_rstats_package_globals_h */
