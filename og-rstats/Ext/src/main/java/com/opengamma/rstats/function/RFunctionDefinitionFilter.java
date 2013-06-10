/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.rstats.function;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.language.function.Definition;
import com.opengamma.language.function.FunctionDefinitionFilter;
import com.opengamma.language.function.MetaFunction;

/**
 * Renames functions that collide with those from the base/core R packages. 
 */
public class RFunctionDefinitionFilter implements FunctionDefinitionFilter {

  private final Map<String, String> _renames = new HashMap<String, String>();

  public RFunctionDefinitionFilter() {
    _renames.put("Position", "PortfolioPosition");
  }

  @Override
  public Definition createDefinition(final MetaFunction definition) {
    final String renameTo = _renames.get(definition.getName());
    if (renameTo != null) {
      final MetaFunction copyOf = definition.clone();
      copyOf.setName(renameTo);
      return copyOf;
    }
    return definition;
  }

}
