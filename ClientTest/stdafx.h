/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef STDAFX_H
#define STDAFX_H

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <tchar.h>
#include <strsafe.h>
#ifdef __cplusplus
#pragma warning(disable:4995)
#endif /* ifdef __cplusplus */
#else /* ifdef _WIN32 */
#include <stdio.h>
#include <stdlib.h>
#endif /* ifdef _WIN32 */

#include <assert.h>

#include <Util/AbstractTest.h>
#include <Util/Fudge.h>

#define Client(path) #path

#endif /* ifndef STDAFX_H */
