package com.opengamma.language.security;

import java.util.Collection;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

public final class IndexUtils {
  
  /**
   * Gets the external identifier for an index reference.
   * 
   * @param index  the index reference
   * @return the external identifier for the index reference
   */
  public static ExternalId getIndexId(String index) {
    if (index.contains("~")) {
      return ExternalId.parse(index);
    }
    // TODO - support a standard scheme for referring to indices
    return ExternalId.of("OG_INDEX", index);
  }
  
  public static IborIndex getIndexSecurity(SecuritySource securitySource, ExternalId indexId) {
    Collection<Security> indices = securitySource.get(ExternalIdBundle.of(indexId));
    if (indices.size() == 0) {
      throw new OpenGammaRuntimeException("No security found for index " + indexId);
    }
    if (indices.size() > 1) {
      throw new OpenGammaRuntimeException("Multiple securities found for index " + indexId);
    }
    Security indexSecurity = Iterables.getOnlyElement(indices);
    if (!(indexSecurity instanceof IborIndex)) {
      throw new OpenGammaRuntimeException("Unexpected index of type " + indexSecurity.getClass().getSimpleName() + "; expected " + IborIndex.class.getSimpleName());
    }
    return (IborIndex) indexSecurity;
  }
  
}
