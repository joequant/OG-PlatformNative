/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_clean_processes_h
#define __inc_clean_processes_h

#include "StringSet.h"

class CProcesses {
private:
	HANDLE m_hSnapshot;
	BOOL Snapshot ();
public:
	CProcesses ();
	~CProcesses ();
	void Report ();
};

#endif /* ifndef __inc_clean_processes_h */
