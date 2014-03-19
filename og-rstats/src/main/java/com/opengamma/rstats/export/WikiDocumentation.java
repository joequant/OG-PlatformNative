/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.rstats.export;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.connector.AbstractMain;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.Parameter;
import com.opengamma.language.export.CategorizingDefinitionExporter;
import com.opengamma.language.export.CategorizingDefinitionExporter.Entry;
import com.opengamma.language.export.WikiDocumentationExporter;
import com.opengamma.language.export.WikiExporter;
import com.opengamma.language.export.WikiPageExporter;
import com.opengamma.language.external.ExternalFunctionProvider;
import com.opengamma.language.function.FunctionDefinitionFilter;
import com.opengamma.language.function.FunctionRepository;

/**
 * Produce Wiki documentation for the R functions.
 */
public class WikiDocumentation extends WikiExporter {

  private static final Logger s_logger = LoggerFactory.getLogger(WikiDocumentation.class);

  /**
   * The location of the Rd files from the main OpenGamma package.
   */
  private static final String OPENGAMMA_RD = "package/man";

  /**
   * The location of the Rd files from the stub OG package. Create by running R and typing:
   * <pre>
   *   library ("OpenGamma")
   *   OpenGamma:::.build.package ("/tmp/OG")
   * </pre>
   */
  private static final String OG_RD = "/tmp/OG/man";

  private static WikiPageExporter.WikiPageHook s_wikiHook;

  public WikiDocumentation(final SessionContext sessionContext, final WikiPageExporter.WikiPageHook wikiHook) {
    super(sessionContext, wikiHook);
  }

  public static void setWikiPageHook(final WikiPageExporter.WikiPageHook wikiHook) {
    s_wikiHook = wikiHook;
  }

  public static WikiPageExporter.WikiPageHook getWikiPageHook() {
    return s_wikiHook;
  }

  private static String readSection(final Reader reader) throws IOException {
    final StringBuilder sb = new StringBuilder();
    int i = reader.read();
    if (i < 0) {
      return null;
    }
    char c = (char) i;
    while (c != '}') {
      if (c == '\\') {
        c = (char) reader.read();
        if ((c == '\\') || (c == '%')) {
          sb.append(c);
        } else if ((c >= 'a') && (c <= 'z')) {
          final StringBuilder macro = new StringBuilder();
          while (c != '{') {
            macro.append(c);
            i = reader.read();
            if (i < 0) {
              throw new EOFException();
            }
            c = (char) i;
          }
          final String macroString = macro.toString();
          if ("code".equals(macroString)) {
            sb.append(readSection(reader));
          } else {
            throw new OpenGammaRuntimeException("Parse error at macro " + macroString);
          }
        }
      } else if (c == '%') {
        do {
          c = (char) reader.read();
        } while (c != '\n');
        sb.append('\n');
      } else {
        sb.append(c);
      }
      i = reader.read();
      if (i < 0) {
        throw new EOFException();
      }
      c = (char) i;
    }
    return sb.toString().replaceAll("^\\s+", "").replaceAll("\\s+$", "").replaceAll("\n[ \t]*\n\\s*", "\0").replaceAll("\\s+", " ").replaceAll("\0", "\n");
  }

  private static String readHeader(final Reader reader) throws IOException {
    final StringBuilder sb = new StringBuilder();
    int i = reader.read();
    char c = (char) i;
    while (c != '{') {
      if (c == '}') {
        if (sb.length() == 0) {
          return null;
        } else {
          throw new OpenGammaRuntimeException("Unexpected } after " + sb);
        }
      }
      if ((c != '\\') && !Character.isWhitespace(c)) {
        sb.append(c);
      }
      i = reader.read();
      if (i < 0) {
        if (sb.length() == 0) {
          return null;
        } else {
          throw new EOFException();
        }
      }
      c = (char) i;
    }
    return sb.toString();
  }

  protected com.opengamma.language.function.Definition loadRdFile(final File file) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(file), 4096);
    String category = Categories.MISC;
    String name = null;
    final List<Parameter> parameters = new LinkedList<Parameter>();
    List<String> aliases = null;
    String description = null;
    boolean constant = false;
    try {
      String s = readHeader(reader);
      while (s != null) {
        if ("name".equals(s)) {
          name = readSection(reader);
          reader.mark(1);
          if (reader.read() == '%') {
            category = reader.readLine().trim();
            if (category.length() == 0) {
              return null;
            }
            if (category.startsWith("CONST_")) {
              constant = true;
              category = category.substring(6);
            }
          } else {
            reader.reset();
          }
        } else if ("alias".equals(s)) {
          final String alias = readSection(reader);
          if (!alias.equals(name)) {
            if (alias.indexOf(',') > 0) {
              return null;
            } else {
              if (aliases == null) {
                aliases = new LinkedList<String>();
              }
              aliases.add(alias);
            }
          }
        } else if ("title".equals(s)) {
          readSection(reader);
          // Ignore title
        } else if ("description".equals(s)) {
          description = readSection(reader);
        } else if ("arguments".equals(s)) {
          s = readHeader(reader);
          while (s != null) {
            if ("item".equals(s)) {
              final String paramName = readSection(reader);
              if (reader.read() != '{') {
                throw new OpenGammaRuntimeException("Parse error after " + s + ", " + paramName);
              }
              final Parameter parameter = new Parameter(paramName, true);
              parameter.setDescription(readSection(reader));
              reader.mark(1);
              if (reader.read() == '%') {
                s = reader.readLine();
                if ("optional".equals(s)) {
                  parameter.setRequired(false);
                }
              } else {
                reader.reset();
              }
              parameters.add(parameter);
            } else {
              throw new OpenGammaRuntimeException("Parse error at macro " + s);
            }
            s = readHeader(reader);
          }
        } else if ("usage".equals(s)) {
          s = readHeader(reader);
          while (s != null) {
            reader.readLine();
            s = readHeader(reader);
          }
          // Ignore usage block
        } else {
          throw new OpenGammaRuntimeException("Parse error at macro " + s);
        }
        s = readHeader(reader);
      }
    } finally {
      reader.close();
    }
    return new com.opengamma.language.function.Definition(name, description, aliases, category, parameters, constant ? -1 : 1);
  }

  /**
   * Loads the contents of the Rd files as meta function definitions.
   * 
   * @param rdPath path to the Rd files
   * @param functions the functions collection to update
   */
  protected void loadRdFiles(final String rdPath, final Collection<com.opengamma.language.function.Definition> functions) {
    final Set<String> existing = new HashSet<String>();
    for (com.opengamma.language.function.Definition function : functions) {
      existing.add(function.getName());
      if (function.getAlias() != null) {
        existing.addAll(function.getAlias());
      }
    }
    final File path = new File(rdPath);
    final File[] files = path.listFiles(new FileFilter() {
      @Override
      public boolean accept(final File file) {
        final String name = file.getName();
        if (!name.startsWith(".") && name.endsWith(".Rd")) {
          return !existing.contains(name.substring(0, name.length() - 3));
        } else {
          return false;
        }
      }
    });
    for (File file : files) {
      try {
        s_logger.debug("Loading file {}", file);
        final com.opengamma.language.function.Definition function = loadRdFile(file);
        if (function != null) {
          functions.add(function);
        }
      } catch (Throwable e) {
        throw new OpenGammaRuntimeException("In file " + file, e);
      }
    }
  }

  @Override
  protected Collection<com.opengamma.language.function.Definition> getFilteredFunctions(final FunctionRepository repository, final FunctionDefinitionFilter filter) {
    final Collection<com.opengamma.language.function.Definition> functions = super.getFilteredFunctions(repository, filter);
    s_logger.info("Loading core functions");
    loadRdFiles(OPENGAMMA_RD, functions);
    s_logger.info("Loading stub functions");
    loadRdFiles(OG_RD, functions);
    s_logger.info("Functions loaded");
    return functions;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes" })
  protected WikiDocumentationExporter getDocumentationExporter(final CategorizingDefinitionExporter underlying) {
    return new WikiDocumentationExporter(underlying) {

      @Override
      protected String getProcedureTerminology() {
        // Procedures are implemented as functions in R
        return getFunctionTerminology();
      }

      @Override
      protected WikiDocumenter createBaseDocumenter() {
        return new RDocumenter(this);
      }

      @Override
      protected WikiFunctionDocumenter createFunctionDocumenter(final WikiDocumenter base) {
        return new RFunction(this, base);
      }

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
    final WikiPageExporter exporter = new WikiPageExporter(underlying) {

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

      @Override
      protected String getPage(final String name) {
        if (Character.isLowerCase(name.charAt(0))) {
          // Something from 'R' land
          for (String component : name.split("\\.")) {
            final char c = component.charAt(0);
            if (Character.isUpperCase(c)) {
              return Character.toString(c);
            }
          }
          return Character.toString(Character.toUpperCase(name.charAt(0)));
        } else {
          // Something from OG-Language; use the system default
          return super.getPage(name);
        }
      }

    };
    return exporter;
  }

  // TODO: for each function, look in the examples folder (package/demo) to see which ones use it and add that to the documentation block

  public static void main(final String[] args) { // CSIGNORE
    ExternalFunctionProvider.setExcludeTests(true);
    (new AbstractMain() {
      @Override
      protected boolean main(final SessionContext context, final String[] args) {
        new WikiDocumentation(context, getWikiPageHook()).run();
        return true;
      }
    }).runMain("R", args);
    System.exit(0);
  }

}
