/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "discovery.h"
#include "processes.h"
#include "services.h"
#include "registry.h"
#include "folders.h"
#include "Log.h"

void CDiscovery::ReportProcesses () {
	CProcesses oProcesses;
	oProcesses.Report ();
}

void CDiscovery::ReportServices () {
	CServices oServices;
	int i;
	for (i = 0; i < m_oServices.Size (); i++) {
		oServices.Watch (m_oServices.Get (i));
	}
	oServices.Report ();
}

void CDiscovery::ReportRegistry () {
	CRegistry oRegistry;
	int i;
	for (i = 0; i < m_oRegistry.Size (); i++) {
		oRegistry.Watch (m_oRegistry.Get (i));
	}
	oRegistry.Report ();
}

void CDiscovery::ReportFolders () {
	CFolders oFolders;
	int i;
	for (i = 0; i < m_oFolders.Size (); i++) {
		oFolders.Watch (m_oFolders.Get (i));
	}
	oFolders.Report ();
}

void CDiscovery::WriteConfiguration () {
	ReportProcesses ();
	ReportServices ();
	ReportRegistry ();
	ReportFolders ();
}