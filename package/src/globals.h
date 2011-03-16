/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_package_globals_h
#define __inc_og_pirate_package_globals_h

#include Client(Functions.h)
#include Client(LiveData.h)
#include Client(Procedures.h)

#ifndef GLOBALS
#define GLOBAL extern
#else
#define GLOBAL
#endif /* ifndef GLOBALS */

GLOBAL CFunctions *g_poFunctions;
GLOBAL CLiveData *g_poLiveData;
GLOBAL CProcedures *g_poProcedures;

#endif /* ifndef __inc_og_pirate_package_globals_h */
