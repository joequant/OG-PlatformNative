/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_pirate_package_fudgemsg_h
#define __inc_og_pirate_package_fudgemsg_h

extern "C" {

	SEXP RPROC FudgeMsg_getAllFields1 (SEXP msg);

}

SEXP FudgeMsg_CreateRObject (FudgeMsg msg);

#endif /* ifndef __inc_og_pirate_package_fudgemsg_h */
