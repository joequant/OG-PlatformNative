/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.rstats.export;

import com.opengamma.language.connector.AbstractMain;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.export.CategorizingDefinitionExporter;
import com.opengamma.language.export.WikiDocumentationExporter;
import com.opengamma.language.export.WikiExporter;

/**
 * Produce Wiki documentation for the R functions.
 */
public class WikiDocumentation extends WikiExporter {

  public WikiDocumentation(final SessionContext sessionContext) {
    super(sessionContext);
  }

  @Override
  protected WikiDocumentationExporter getDocumentationExporter(final CategorizingDefinitionExporter underlying) {
    return new WikiDocumentationExporter(underlying) {

      @Override
      protected String getProcedureTerminology() {
        // Procedures are implemented as functions in R
        return getFunctionTerminology();
      }

      @SuppressWarnings("unchecked")
      @Override
      protected WikiProcedureDocumenter createProcedureDocumenter(final WikiDocumenter base) {
        return new RProcedure(this, base);
      }

    };
  }

  // TODO: for each function, look in the examples folder (package/demo) to see which ones use it and add that to the documentation block

  public static void main(final String[] args) { // CSIGNORE
    (new AbstractMain() {
      @Override
      protected boolean main(final SessionContext context, final String[] args) {
        new WikiDocumentation(context).run();
        return true;
      }
    }).runMain("R", args);
    // TODO: change runMain so that it doesn't exit - we may want to do other things
  }

}
