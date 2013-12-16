/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "log.h"
#include "discovery.h"
#include "processes.h"
#include "services.h"
#include "folders.h"
#include "registry.h"

#define ARG_OUTPUT			'o'
#define ARG_SERVICE			's'
#define ARG_DEBUG_FOLDER	'f'
#define ARG_DEBUG_REGISTRY	'r'
#define ARG_DELETE_FOLDER	'd'
#define ARG_DELETE_REGISTRY	'e'

static void _ConfigureLog () {
	int i;
	for (i = 1; i < __argc; i++) {
		if (__targv[i] && (*__targv[i] == ARG_OUTPUT)) {
			LogToFile (__targv[i] + 1);
		}
	}
}

static void _WriteDiscoveryLog () {
	CDiscovery oDiscovery;
	int i;
	for (i = 1; i < __argc; i++) {
		if (__targv[i]) {
			switch (*__targv[i]) {
			case ARG_SERVICE :
				oDiscovery.ReportService (__targv[i] + 1);
				break;
			case ARG_DEBUG_FOLDER :
				oDiscovery.ReportFolders (__argv[i] + 1);
				break;
			case ARG_DEBUG_REGISTRY :
				oDiscovery.ReportRegistry (__argv[i] + 1);
				break;
			}
		}
	}
	oDiscovery.WriteConfiguration ();
}

static void _StopKillAndDeleteServices () {
	CServices oServices;
	int i;
	for (i = 1; i < __argc; i++) {
		if (__targv[i] && (*__targv[i] == ARG_SERVICE)) {
			oServices.Watch (__targv[i] + 1);
		}
	}
	oServices.StopKillAndDelete ();
}

static void _DeleteFolders () {
	CFolders oFolders;
	int i;
	for (i = 1; i < __argc; i++) {
		if (__targv[i] && (*__targv[i] == ARG_DELETE_FOLDER)) {
			oFolders.Watch (__targv[i] + 1);
		}
	}
	oFolders.Delete ();
}

static void _DeleteRegistryKeys () {
	CRegistry oRegistry;
	int i;
	for (i = 1; i < __argc; i++) {
		if (__targv[i] && (*__targv[i] == ARG_DELETE_REGISTRY)) {
			oRegistry.Watch (__targv[i] + 1);
		}
	}
	oRegistry.Delete ();
}

static BOOL _FindErrors () {
	CServices oServices;
	int i;
	for (i = 1; i < __argc; i++) {
		if (__targv[i] && (*__targv[i] == ARG_SERVICE)) {
			oServices.Watch (__targv[i] + 1);
		}
	}
	oServices.Check ();
	PCTSTR pszFatal = LogGetFatal ();
	if (pszFatal) {
		MessageBox (HWND_DESKTOP, pszFatal, TEXT ("OpenGamma pre-Installation Checklist"), MB_OK);
		return TRUE;
	} else {
		return FALSE;
	}
}

#ifdef _DEBUG
int main () {
#else /* ifdef _DEBUG */
int WINAPI WinMain (HINSTANCE hInstance, HINSTANCE hPrevInstance, PTSTR pszCmdLine, int nCmdShow) {
#endif /* ifdef _DEBUG */
	_ConfigureLog ();
	_WriteDiscoveryLog ();
	_StopKillAndDeleteServices ();
	_DeleteFolders ();
	_DeleteRegistryKeys ();
	return _FindErrors () ? EXIT_FAILURE : EXIT_SUCCESS;
}
