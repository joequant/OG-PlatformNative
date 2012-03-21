/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.rstats.export;

import com.opengamma.language.export.AbstractDocumentationExporter.Documenter;
import com.opengamma.language.export.WikiDocumentationExporter;
import com.opengamma.language.function.Definition;

/**
 * Produce Wiki documentation for an R function (or constant).
 */
public class RFunction extends WikiDocumentationExporter.WikiFunctionDocumenter {

  public RFunction(final WikiDocumentationExporter parent, final Documenter<Definition> base) {
    parent.super(base);
  }

  @Override
  protected String getNoParameters(final Definition definition) {
    if (definition.getReturnCount() < 0) {
      return "";
    } else {
      return super.getNoParameters(definition);
    }
  }

  @Override
  protected String getMultipleResults(final Definition definition, final int resultCount) {
    if (resultCount < 0) {
      return "This is a constant - do not invoke it as a function, refer to it directly by value.\n\n";
    } else {
      return super.getMultipleResults(definition, resultCount);
    }
  }

}
