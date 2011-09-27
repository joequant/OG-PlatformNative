/*
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_rstats_package_entities_h
#define __inc_og_rstats_package_entities_h

#include Client(Entities.h)

/// R method helper for querying CEntities objects. Note that this does not increment the reference
/// count of the object; it should typically be used on a single stack frame.
class REntities {
private:

	/// Entity source to query
	const CEntities *m_poEntities;

protected:

	/// Fetches a typed entry from the entity source
	///
	/// @param[in] index zero-based index of the entry
	/// @return the entry, or NULL if none is found
	virtual const CEntityEntry *GetEntryImpl (int index) const = 0;

	const CEntityEntry *GetEntry (SEXP index) const;
public:

	/// Creates a new helper instance.
	///
	/// @param[in] poEntities the entities object
	REntities (const CEntities *poEntities) { m_poEntities = poEntities; }

	SEXP Count () const;
	SEXP GetName (SEXP index) const;
	SEXP GetParameterFlags (SEXP index) const;
	SEXP GetParameterNames (SEXP index) const;
};

/// R method helper for querying CEntityEntry objects. Note that the paremt entities object reference
/// count is unaffected; it should typically be used on a single stack frame.
class REntityEntry {
private:

	/// Entry to query
	const CEntityEntry *m_poEntry;

public:

	/// Creates a new helper instance.
	///
	/// @param[in] poEntry the entry object
	REntityEntry (const CEntityEntry *poEntry) { m_poEntry = poEntry; }

	SEXP GetName () const;
	SEXP GetParameterFlags () const;
	SEXP GetParameterNames () const;
};

#endif /* ifndef __inc_og_rstats_package_entities_h */
