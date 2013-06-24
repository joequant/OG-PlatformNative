/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.rstats.export;

import com.opengamma.language.definition.Definition;
import com.opengamma.language.export.WikiDocumentationExporter;
import com.opengamma.language.export.WikiDocumentationExporter.WikiDocumenter;

/**
 * Produce Wiki documentation for an R entity.
 * 
 * @param <D> definition type
 */
public class RDocumenter<D extends Definition> extends WikiDocumenter<D> {

  public RDocumenter(final WikiDocumentationExporter parent) {
    parent.super();
  }

  @Override
  protected String getBlurb(final D definition) {
    String blurb = super.getBlurb(definition);
    if (definition.getName().startsWith("fromFudgeMsg.")) {
      blurb = blurb + getWikiDoc("fromFudgeMsg");
    } else if (definition.getName().startsWith("Interop.")) {
      blurb = blurb + getWikiDoc("Interop");
    }
    return blurb;
  }

}
