/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_log_h
#define __inc_clean_log_h

void LogToFile (PCTSTR pszFilename);
void LogPrintf (PCTSTR pszFormat, ...);

#define LogDebug(fmt,...) LogPrintf (TEXT ("%s:%d: ") fmt TEXT ("\n"), TEXT (__FILE__), __LINE__, __VA_ARGS__)
#define LogOutOfMemory() LogDebug (TEXT ("Out of memory"))

#endif /* ifndef __inc_clean_log_h */
