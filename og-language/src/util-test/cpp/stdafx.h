/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef STDAFX_H
#define STDAFX_H

// Common include file for all source code in UtilTest

#if defined (_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#ifdef __cplusplus
#pragma warning(disable:4995) /* suppress #pragma deprecated warnings from standard C++ headers */
#endif /* ifdef __cplusplus */
#else
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#endif
#include <assert.h>

#include <util/cpp/Unicode.h>
#include <util/cpp/AbstractTest.h>

#endif /* ifndef STDAFX_H */
