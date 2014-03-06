/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class for loading information from an MSM table.
 */
/* package */class MSMTable {

  public static final String CREATE_FOLDER_TABLE = "CreateFolder";
  public static final String CREATE_FOLDER_DIRECTORY_FIELD = "Directory_";

  public static final String DIRECTORY_TABLE = "Directory";
  public static final String DIRECTORY_KEY_FIELD = DIRECTORY_TABLE;
  public static final String DIRECTORY_PARENT_FIELD = "Directory_Parent";
  public static final String DIRECTORY_NAME_FIELD = "DefaultDir";

  public static final String MODULE_CONFIGURATION_TABLE = "ModuleConfiguration";
  public static final String MODULE_CONFIGURATION_NAME_FIELD = "Name";

  public static final String MSI_LOCK_PERMISSIONS_EX_TABLE = "MsiLockPermissionsEx";
  public static final String MSI_LOCK_PERMISSIONS_EX_KEY_FIELD = MSI_LOCK_PERMISSIONS_EX_TABLE;
  public static final String MSI_LOCK_PERMISSIONS_EX_OBJECT_FIELD = "LockObject";
  public static final String MSI_LOCK_PERMISSIONS_EX_TABLE_FIELD = "Table";
  public static final String MSI_LOCK_PERMISSIONS_EX_SDDL_FIELD = "SDDLText";
  public static final String MSI_LOCK_PERMISSIONS_EX_CONDITION_FIELD = "Condition";

  public static final String PROPERTY_TABLE = "Property";
  public static final String PROPERTY_KEY_FIELD = PROPERTY_TABLE;
  public static final String PROPERTY_VALUE_FIELD = "Value";

  public class Row {

    private final String[] _values;

    private Row(final String[] values) {
      _values = values;
    }

    public String get(final String column) {
      return _values[_fields.get(column)];
    }

  }

  private final Map<String, Integer> _fields = new HashMap<String, Integer>();
  private final List<String[]> _values = new ArrayList<String[]>();
  private final Iterable<Row> _rows = new Iterable<Row>() {
    @Override
    public Iterator<Row> iterator() {
      final Iterator<String[]> itr = _values.iterator();
      return new Iterator<Row>() {

        @Override
        public boolean hasNext() {
          return itr.hasNext();
        }

        @Override
        public Row next() {
          return new Row(itr.next());
        }

        @Override
        public void remove() {
          itr.remove();
        }

      };
    }
  };

  private MSMTable() {
  }

  private void setFields(final String[] fields) {
    for (int index = 0; index < fields.length; index++) {
      _fields.put(fields[index], index);
    }
  }

  private void add(final String[] row) {
    _values.add(row);
  }

  public Iterable<Row> rows() {
    return _rows;
  }

  public static MSMTable load(final String msmPath, final String tableName) {
    if (!new File(msmPath).exists()) {
      System.err.println("MSM " + msmPath + " not found");
      return null;
    }
    synchronized (MSMTable.class) {
      final String tmpDir = System.getProperty("java.io.tmpdir");
      try {
        final Process msidb = Runtime.getRuntime().exec(new String[] {"msidb", "-d" + msmPath, "-f" + tmpDir, "-e" + tableName });
        if (msidb.waitFor() != 0) {
          return null;
        }
      } catch (Exception ex) {
        throw new RuntimeException("Error running msidb", ex);
      }
      try (final BufferedReader br = new BufferedReader(new FileReader(tmpDir + "\\" + tableName + ".idt"))) {
        final MSMTable table = new MSMTable();
        final String[] fields = br.readLine().split("\t");
        table.setFields(fields);
        br.readLine();
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
          final String[] values = line.split("\t");
          if (values.length > 0) {
            if (values.length < fields.length) {
              // String#split will lose any trailing "empty" fields, so we need to put them back in to avoid index exceptions later
              final String[] paddedValues = new String[fields.length];
              System.arraycopy(values, 0, paddedValues, 0, values.length);
              for (int i = values.length; i < paddedValues.length; i++) {
                paddedValues[i] = "";
              }
              table.add(paddedValues);
            } else {
              table.add(values);
            }
          }
        }
        return table;
      } catch (IOException ex) {
        throw new RuntimeException("Error reading module configuration table", ex);
      }
    }
  }

}
