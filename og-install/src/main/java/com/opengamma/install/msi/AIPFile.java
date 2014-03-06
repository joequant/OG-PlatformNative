/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Represents an AIP file in memory.
 */
/* package */final class AIPFile {

  private static final String COMPONENT_INFO_ID = "cid";
  private static final String ATTRIBUTE_KEY = "name";
  private static final String ATTRIBUTE_VALUE = "value";

  public static final class ComponentInfo {

    private final String _id;
    private final List<Map<String, String>> _rows = new LinkedList<Map<String, String>>();
    private final Map<String, String> _attributes = new LinkedHashMap<String, String>();

    public ComponentInfo(final String id) {
      _id = id;
    }

    public String getId() {
      return _id;
    }

    public void add(final Map<String, String> row) {
      _rows.add(new LinkedHashMap<String, String>(row));
    }

    public void add(final String attributeKey, final String attributeValue) {
      _attributes.put(attributeKey, attributeValue);
    }

    public Iterable<Map<String, String>> rows() {
      return _rows;
    }

    public Iterable<Map.Entry<String, String>> attributes() {
      return _attributes.entrySet();
    }

  }

  private final Map<String, String> _documentProperties = new LinkedHashMap<String, String>();
  private final Map<String, ComponentInfo> _components = new LinkedHashMap<String, ComponentInfo>();

  public AIPFile() {
  }

  public void setDocumentProperty(final String key, final String value) {
    _documentProperties.put(key, value);
  }

  public void add(final ComponentInfo component) {
    _components.put(component.getId(), component);
  }

  public ComponentInfo get(final String component) {
    return _components.get(component);
  }

  private static Map<String, String> parseXMLRow(final String row, int index) {
    final Map<String, String> attribs = new LinkedHashMap<String, String>();
    while (index < row.length()) {
      final StringBuilder key = new StringBuilder();
      char c = row.charAt(index++);
      while (c != '=') {
        key.append(c);
        c = row.charAt(index++);
      }
      c = row.charAt(index++);
      assert c == '\"' : (row + " index = " + index);
      final StringBuilder value = new StringBuilder();
      c = row.charAt(index++);
      while (c != '\"') {
        value.append(c);
        c = row.charAt(index++);
      }
      attribs.put(key.toString(), StringEscapeUtils.unescapeXml(value.toString()));
      c = row.charAt(index++);
      if ((c == '/') || (c == '>')) {
        break;
      }
      assert c == ' ' : (row + " index = " + index);
    }
    return attribs;
  }

  public static AIPFile load(final File source) {
    final AIPFile file = new AIPFile();
    try {
      final BufferedReader br = new BufferedReader(new FileReader(source));
      try {
        String line;
        int state = 0;
        int lineNo = 0;
        ComponentInfo componentInfo = null;
        while ((line = br.readLine()) != null) {
          lineNo++;
          switch (state) {
            case 0:
              if (line.startsWith("<?")) {
                state = 1;
                continue;
              }
              break;
            case 1:
              if (line.startsWith("<DOCUMENT ")) {
                for (Map.Entry<String, String> documentProperty : parseXMLRow(line, 10).entrySet()) {
                  file.setDocumentProperty(documentProperty.getKey(), documentProperty.getValue());
                }
                state = 2;
                continue;
              }
              break;
            case 2:
              if (line.startsWith("  <COMPONENT ")) {
                componentInfo = new ComponentInfo(parseXMLRow(line, 13).get(COMPONENT_INFO_ID));
                state = 3;
                continue;
              }
              if (line.startsWith("</DOCUMENT>")) {
                state = 1;
                continue;
              }
              break;
            case 3:
              if (line.startsWith("  </COMPONENT>")) {
                file.add(componentInfo);
                componentInfo = null;
                state = 2;
                continue;
              }
              if (line.startsWith("    <ROW ")) {
                componentInfo.add(parseXMLRow(line, 9));
                continue;
              }
              if (line.startsWith("    <ATTRIBUTE ")) {
                final Map<String, String> attrib = parseXMLRow(line, 15);
                componentInfo.add(attrib.get(ATTRIBUTE_KEY), attrib.get(ATTRIBUTE_VALUE));
                continue;
              }
              break;
          }
          throw new IllegalArgumentException("Line = " + lineNo + ", State = " + state);
        }
      } finally {
        br.close();
      }
    } catch (IOException ex) {
      throw new RuntimeException("Error reading file", ex);
    }
    return file;
  }

  private static void writeXMLLine(final PrintStream ps, final String start, final Map<String, String> attribs, final boolean endTag) throws IOException {
    ps.print(start);
    for (Map.Entry<String, String> attrib : attribs.entrySet()) {
      ps.print(' ');
      ps.print(attrib.getKey());
      ps.print("=\"");
      ps.print(StringEscapeUtils.escapeXml(attrib.getValue()));
      ps.print('\"');
    }
    if (endTag) {
      ps.print('/');
    }
    ps.println('>');
  }

  public void save(final File dest) {
    try {
      final PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(dest)));
      try {
        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        writeXMLLine(ps, "<DOCUMENT", _documentProperties, false);
        for (ComponentInfo component : _components.values()) {
          ps.print("  <COMPONENT " + COMPONENT_INFO_ID + "=\"");
          ps.print(component.getId());
          ps.println("\">");
          for (Map<String, String> row : component.rows()) {
            writeXMLLine(ps, "    <ROW", row, true);
          }
          for (Map.Entry<String, String> attrib : component.attributes()) {
            ps.print("    <ATTRIBUTE " + ATTRIBUTE_KEY + "=\"");
            ps.print(StringEscapeUtils.escapeXml(attrib.getKey()));
            ps.print("\" " + ATTRIBUTE_VALUE + "=\"");
            ps.print(StringEscapeUtils.escapeXml(attrib.getValue()));
            ps.println("\"/>");
          }
          ps.println("  </COMPONENT>");
        }
        ps.println("</DOCUMENT>");
      } finally {
        ps.close();
      }
    } catch (IOException ex) {
      throw new RuntimeException("Error writing file", ex);
    }
  }

}
