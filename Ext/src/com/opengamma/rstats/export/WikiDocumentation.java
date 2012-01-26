/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.rstats.export;

import com.opengamma.language.connector.AbstractMain;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.export.CategorizingDefinitionExporter;
import com.opengamma.language.export.CategorizingDefinitionExporter.Entry;
import com.opengamma.language.export.WikiDocumentationExporter;
import com.opengamma.language.export.WikiExporter;
import com.opengamma.language.export.WikiPageExporter;

/**
 * Produce Wiki documentation for the R functions.
 */
public class WikiDocumentation extends WikiExporter {

  public WikiDocumentation(final SessionContext sessionContext) {
    super(sessionContext);
  }

  // TODO: pull data from the Rd files too so that they end up in the wiki

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

      @Override
      protected String[] getWikiDocFolders() {
        return new String[] {"./wikiDoc", "../OG-Language/wikiDoc" };
      }

    };
  }

  @Override
  protected WikiPageExporter getPageExporter(final CategorizingDefinitionExporter underlying) {
    return new WikiPageExporter(underlying) {

      @Override
      protected String pageAddressCategory(final String category) {
        return "R " + prettyPrintCategory(category) + " Functions";
      }

      @Override
      protected String createPageName(final String category, final String page) {
        return "R " + prettyPrintCategory(category) + " Functions - " + page;
      }

      @Override
      protected String createPageName(final Entry entry) {
        return "R Function - " + entry.getName();
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
    System.exit(0);
  }

}
