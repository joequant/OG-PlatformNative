/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"
#include "Entities.h"
#include "globals.h"
#include "Errors.h"
#include "Parameters.h"

LOGGING (com.opengamma.rstats.package.Entities);

/// Returns the indexed entry.
///
/// @param[in] index zero-based index of the entry
/// @return the entry, or NULL if an error was issued (e.g. the index is invalid)
const CEntityEntry *REntities::GetEntry (SEXP index) const {
	if (m_poEntities) {
		if (isInteger (index)) {
			const CEntityEntry *poEntry = GetEntryImpl (*INTEGER (index));
			if (poEntry != NULL) {
				return poEntry;
			} else {
				LOGERROR (ERR_PARAMETER_VALUE);
				return NULL;
			}
		} else {
			LOGERROR (ERR_PARAMETER_TYPE);
			return NULL;
		}
	} else {
		LOGERROR (ERR_INITIALISATION);
		return NULL;
	}
}

/// Converts a Data result value to the R SEXP result, applying additional information from the metadata object.
///
/// @param[in] pResult result value, may be NULL
/// @param[in] pInfo result metadata, may be NULL
/// @return the SEXP result
SEXP REntities::ProcessResult (CRCallback *poR, com_opengamma_language_Data *pResult, com_opengamma_rstats_msg_DataInfo *pInfo) {
	if (pResult) {
		SEXP result = CData::ToSEXP (pResult);
		PROTECT (result);
		int unprotectCount = 1;
		if (pResult->_single && pResult->_single->_messageValue && (!pInfo || !pInfo->_wrapperClass)) {
			// Result is a message and there is no class wrapper
			FudgeField field;
			if ((FudgeMsg_getFieldByOrdinal (&field, pResult->_single->_messageValue, 0) == FUDGE_OK) && (field.type == FUDGE_TYPE_STRING)) {
				SEXP newResult = poR->FromFudgeMsg (field.data.string, result);
				if (newResult != R_NilValue) {
					PROTECT (newResult);
					result = newResult;
					unprotectCount++;
				}
			}
		}
		if (pInfo) {
			result = CDataInfo::Apply (poR, result, pInfo);
		}
		UNPROTECT (unprotectCount);
		return result;
	} else {
		return R_NilValue;
	}
}

/// Returns the number of entities.
///
/// @return the number of entities
SEXP REntities::Count () const {
	if (m_poEntities) {
		SEXP count = allocVector (INTSXP, 1);
		*INTEGER (count) = m_poEntities->Size ();
		return count;
	} else {
		LOGERROR (ERR_INITIALISATION);
		return R_NilValue;
	}
}

/// Returns the category of the indexed entity.
///
/// @param[in] index zero based index of the entity
/// @return the category
SEXP REntities::GetCategory (SEXP index) const {
	REntityEntry oE (GetEntry (index));
	return oE.GetCategory ();
}

/// Returns the description of the indexed entity.
///
/// @param[in] index zero based index of the entity
/// @return the description
SEXP REntities::GetDescription (SEXP index) const {
	REntityEntry oE (GetEntry (index));
	return oE.GetDescription ();
}

/// Returns the name of the indexed entity.
///
/// @param[in] index zero based index of the entity
/// @return the name
SEXP REntities::GetName (SEXP index) const {
	REntityEntry oE (GetEntry (index));
	return oE.GetName ();
}

/// Returns the parameter flags of the indexed entity.
///
/// @param[in] index zero based index of the entity
/// @return the parameter flags
SEXP REntities::GetParameterFlags (SEXP index) const {
	REntityEntry oE (GetEntry (index));
	return oE.GetParameterFlags ();
}

/// Returns the parameter names of the indexed entity.
///
/// @param[in] index zero based index of the entity
/// @return the parameter names
SEXP REntities::GetParameterNames (SEXP index) const {
	REntityEntry oE (GetEntry (index));
	return oE.GetParameterNames ();
}

/// Returns the parameter descriptions of the indexed entity.
///
/// @param[in] index zero based index of the entity
/// @return the parameter descriptions
SEXP REntities::GetParameterDescriptions (SEXP index) const {
	REntityEntry oE (GetEntry (index));
	return oE.GetParameterDescriptions ();
}

/// Returns the category of the entiry.
///
/// @return the category
SEXP REntityEntry::GetCategory () const {
	if (m_poEntry) {
		const char *pszCategory = m_poEntry->GetCategory ();
		return pszCategory ? mkString (pszCategory) : R_NilValue;
	} else {
		return R_NilValue;
	}
}

/// Returns the description of the entity.
///
/// @return the description
SEXP REntityEntry::GetDescription () const {
	if (m_poEntry) {
		const char *pszDescription = m_poEntry->GetDescription ();
		return pszDescription ? mkString (pszDescription) : R_NilValue;
	} else {
		return R_NilValue;
	}
}

/// Returns the name of the entity.
///
/// @return the name
SEXP REntityEntry::GetName () const {
	if (m_poEntry) {
		return mkString (m_poEntry->GetName ());
	} else {
		return R_NilValue;
	}
}

/// Returns the parameter flags of the entity.
///
/// @return the parameter flags
SEXP REntityEntry::GetParameterFlags () const {
	if (m_poEntry) {
		SEXP flags = allocVector (INTSXP, m_poEntry->GetParameterCount ());
		int n;
		for (n = 0; n < m_poEntry->GetParameterCount (); n++) {
			INTEGER (flags)[n] = m_poEntry->GetParameter (n)->GetFlags ();
		}
		return flags;
	} else {
		return R_NilValue;
	}
}

/// Returns the parameter names of the entity
///
/// @return the parameter flags
SEXP REntityEntry::GetParameterNames () const {
	if (m_poEntry) {
		SEXP names = allocVector (STRSXP, m_poEntry->GetParameterCount ());
		PROTECT (names);
		int n;
		for (n = 0; n < m_poEntry->GetParameterCount (); n++) {
			SEXP name = mkChar (m_poEntry->GetParameter (n)->GetName ());
			PROTECT (name);
			SET_STRING_ELT (names, n, name);
		}
		UNPROTECT (1 + n);
		return names;
	} else {
		return R_NilValue;
	}
}

/// Returns the parameter descriptions of the entity
///
/// @return the parameter descriptions
SEXP REntityEntry::GetParameterDescriptions () const {
	if (m_poEntry) {
		SEXP names = allocVector (STRSXP, m_poEntry->GetParameterCount ());
		PROTECT (names);
		int n;
		for (n = 0; n < m_poEntry->GetParameterCount (); n++) {
			const char *pszDescription = m_poEntry->GetParameter (n)->GetDescription ();
			SEXP name = pszDescription ? mkChar (pszDescription) : R_NaString;
			PROTECT (name);
			SET_STRING_ELT (names, n, name);
		}
		UNPROTECT (1 + n);
		return names;
	} else {
		return R_NilValue;
	}
}
