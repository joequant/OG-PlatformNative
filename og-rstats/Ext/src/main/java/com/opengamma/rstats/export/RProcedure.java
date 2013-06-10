/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.rstats.export;

import com.opengamma.language.export.AbstractDocumentationExporter.Documenter;
import com.opengamma.language.export.WikiDocumentationExporter;
import com.opengamma.language.procedure.Definition;

/**
 * Produce Wiki documentation for an R procedure.
 */
public class RProcedure extends WikiDocumentationExporter.WikiProcedureDocumenter {

  public RProcedure(final WikiDocumentationExporter parent, final Documenter<Definition> base) {
    parent.super(base);
  }

  @Override
  protected String getOneResult(final Definition definition) {
    // No-op
    return "";
  }

}
