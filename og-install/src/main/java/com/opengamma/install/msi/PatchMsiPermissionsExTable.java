/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Modify an AIP file to set explicit DACLs for inheritable objects.
 * <p>
 * By using this we can put MsiLockPermissionsEx table entries in AIP files that correspond to significant folders, and then patch the AIP prior to MSI generation so that all of the "inherited" DACLs
 * are present also. If we don't do this then the following problem occurs if we have the following file system:
 * 
 * <pre>
 *   ProgramFiles
 *     OpenGamma Ltd
 *       My Application     (Has a DACL defined in MsiLockPermissionsEx)
 *         Foo              (No explicit DACL - want the one from "My Application" inherited)
 * </pre>
 * <p>
 * The ordering of <code>FolderCreate</code> actions seems arbitrary. If the <code>Foo</code> folder gets constructed first then the <code>My Application</code> and <code>OpenGamma Ltd</code> parents
 * also get constructed and all get an inherited DACL from the <code>ProgramFiles</code> folder. When the <code>My Application</code> gets created, with the DACL, the DACL is set only for that object
 * and is not propagated to the children. This gives an undesirable result which does not happen if the <code>FolderCreate</code> operations get ordered differently.
 * <p>
 * By running this script, we go from the one explicit DACL in the original AIP to having two - one for <code>My Application</code> and the derived one for <code>Foo</code>. This means the order in
 * which the folders are created no longer affects the outcome.
 */
public class PatchMsiPermissionsExTable extends AbstractAipProcessor {

  /**
   * Translates an SDDL to the form that must be set on a child object.
   * <p>
   * This implementation assumes that we are only working with container-inherit (CI) entries so just need to apply "ID" to the mask for any that don't have it set.
   * 
   * @param sddl the parent SDDL
   * @return the child object's SDDL
   */
  /* package */static String inherit(final String sddl) {
    final StringBuilder sb = new StringBuilder("D:AI");
    int i = sddl.indexOf('(');
    do {
      int j = sddl.indexOf(')', i);
      final String ace = sddl.substring(i + 1, j);
      sb.append('(');
      final String[] elems = ace.split(";");
      sb.append(elems[0]).append(';');
      boolean inherited = false;
      for (int k = 0; k < elems[1].length(); k += 2) {
        if ("ID".equals(elems[1].substring(k, k + 2))) {
          inherited = true;
          break;
        }
      }
      if (inherited) {
        sb.append(elems[1]).append(';');
      } else {
        sb.append(elems[1]).append("ID;");
      }
      sb.append(elems[2]).append(';');
      sb.append(elems[3]).append(';');
      sb.append(elems[4]).append(';');
      sb.append(elems[5]).append(')');
      i = sddl.indexOf('(', j);
    } while (i > 0);
    return sb.toString();
  }

  private static final class Lock {

    private final String _key;
    private final String _object;
    private final String _table;
    private final String _sddl;
    private final String _condition;
    private String _inherit;

    public Lock(final String key, final String object, final String table, final String sddl, final String condition) {
      _key = key;
      _object = object;
      _table = table;
      _sddl = sddl;
      _condition = condition;
    }

    public Lock inheritTo(final String object) {
      if (_inherit == null) {
        _inherit = inherit(_sddl);
      }
      return new Lock(object.replace("_Dir", "_Perm"), object, _table, _inherit, _condition);
    }

    public Map<String, String> toComponentRow() {
      final Map<String, String> row = new LinkedHashMap<String, String>();
      row.put(MSI_LOCK_PERMISSIONS_EX_KEY_FIELD, _key);
      row.put(MSI_LOCK_PERMISSIONS_EX_OBJECT_FIELD, _object);
      row.put(MSI_LOCK_PERMISSIONS_EX_TABLE_FIELD, _table);
      row.put(MSI_LOCK_PERMISSIONS_EX_SDDL_FIELD, _sddl);
      if ((_condition != null) && (_condition.length() > 0)) {
        row.put(MSI_LOCK_PERMISSIONS_EX_CONDITION_FIELD, _condition);
      }
      return row;
    }

    @Override
    public String toString() {
      return "(" + _key + ", " + _object + ", " + _table + ", " + _sddl + ", " + _condition + ")";
    }

  }

  /**
   * Represents the permissioning state from the AIP file. This includes a reference to the component defining the <code>MsiLockPermissionsEx</code> table as well as any locks that have been gathered
   * from the explicit definitions in the AIP and MSMs.
   */
  private static final class MsiPermissionsEx {

    private final Map<Pair<String, String>, Lock> _locks = new HashMap<Pair<String, String>, Lock>();
    private final AIPFile.ComponentInfo _componentInfo;

    public MsiPermissionsEx(final AIPFile file) {
      // Load the explicit locks
      AIPFile.ComponentInfo component = file.get(MSI_LOCK_PERMISSIONS_EX_COMPONENT);
      if (component != null) {
        _componentInfo = component;
        for (Map<String, String> row : component.rows()) {
          add(new Lock(row.get(MSI_LOCK_PERMISSIONS_EX_KEY_FIELD), row.get(MSI_LOCK_PERMISSIONS_EX_OBJECT_FIELD), row.get(MSI_LOCK_PERMISSIONS_EX_TABLE_FIELD),
              row.get(MSI_LOCK_PERMISSIONS_EX_SDDL_FIELD), row.get(MSI_LOCK_PERMISSIONS_EX_CONDITION_FIELD)));
        }
      } else {
        _componentInfo = new AIPFile.ComponentInfo(MSI_LOCK_PERMISSIONS_EX_COMPONENT);
        file.add(_componentInfo);
      }
      // Load any locks inherited from the MSMs
      component = file.get(MERGE_MODULE_COMPONENT);
      if (component != null) {
        for (Map<String, String> row : component.rows()) {
          MSMTable msmLocks = MSMTable.load(row.get(MERGE_MODULE_PATH_FIELD), MSMTable.MSI_LOCK_PERMISSIONS_EX_TABLE);
          if (msmLocks != null) {
            for (MSMTable.Row lock : msmLocks.rows()) {
              add(new Lock(lock.get(MSMTable.MSI_LOCK_PERMISSIONS_EX_KEY_FIELD), lock.get(MSMTable.MSI_LOCK_PERMISSIONS_EX_OBJECT_FIELD),
                  lock.get(MSMTable.MSI_LOCK_PERMISSIONS_EX_TABLE_FIELD), lock.get(MSMTable.MSI_LOCK_PERMISSIONS_EX_SDDL_FIELD), lock.get(MSMTable.MSI_LOCK_PERMISSIONS_EX_CONDITION_FIELD)));
            }
          }
        }
      }
    }

    public Lock getFolderLock(final String folder) {
      return _locks.get(Pairs.of(folder, MSMTable.CREATE_FOLDER_TABLE));
    }

    private void add(final Lock lock) {
      _locks.put(Pairs.of(lock._object, lock._table), lock);
    }

    public void patch(final Lock parentLock, final String object) {
      final Lock objectLock = parentLock.inheritTo(object);
      add(objectLock);
      _componentInfo.add(objectLock.toComponentRow());
    }

  }

  public PatchMsiPermissionsExTable(final File aipFile) {
    super(aipFile);
  }

  /**
   * Implementation of {@link #patchFolderPermissions}.
   */
  private static class FolderPermissions {

    private static final class FolderInfo {

      private final String _id;
      private final String _parent;
      private final String _name;
      private final Set<String> _aliases = new HashSet<String>();

      public FolderInfo(final String id, final String parent, final String name) {
        _id = id;
        _parent = parent;
        _name = name;
      }

      public String getId() {
        return _id;
      }

      public String getParent() {
        return _parent;
      }

      public String getName() {
        return _name;
      }

      public Set<String> getAliases() {
        return _aliases;
      }

      public void addAlias(final String alias) {
        _aliases.add(alias);
      }

    }

    private final AIPFile _file;
    private final MsiPermissionsEx _permissions;
    private final Map<String, FolderInfo> _folderInfo = new HashMap<String, FolderInfo>();

    public FolderPermissions(final AIPFile file, final MsiPermissionsEx permissions) {
      _file = file;
      _permissions = permissions;
    }

    /**
     * Load folder metadata from the AIP and referenced MSM files.
     */
    public void loadFolderInfo() {
      // Load all of the explicit folder parents
      AIPFile.ComponentInfo component = _file.get(DIRECTORY_COMPONENT);
      if (component != null) {
        for (Map<String, String> row : component.rows()) {
          final FolderInfo info = new FolderInfo(row.get(DIRECTORY_KEY_FIELD), row.get(DIRECTORY_PARENT_FIELD), createFolderName(row.get(DIRECTORY_NAME_FIELD)));
          _folderInfo.put(info.getId(), info);
        }
      }
      // Load all of the inherited folder parents
      component = _file.get(MERGE_MODULE_COMPONENT);
      if (component != null) {
        for (Map<String, String> row : component.rows()) {
          MSMTable msmDirectories = MSMTable.load(row.get(MERGE_MODULE_PATH_FIELD), MSMTable.DIRECTORY_TABLE);
          if (msmDirectories != null) {
            for (MSMTable.Row directory : msmDirectories.rows()) {
              final FolderInfo info = new FolderInfo(directory.get(MSMTable.DIRECTORY_KEY_FIELD), directory.get(MSMTable.DIRECTORY_PARENT_FIELD),
                  createFolderName(directory.get(MSMTable.DIRECTORY_NAME_FIELD)));
              _folderInfo.put(info.getId(), info);
            }
          }
        }
      }
    }

    private String createFolderName(final String name) {
      if (name.startsWith("[|")) {
        final AIPFile.ComponentInfo ci = _file.get(PROPERTY_COMPONENT);
        if (ci != null) {
          final String property = name.substring(2, name.length() - 1);
          for (Map<String, String> row : ci.rows()) {
            if (property.equals(row.get(PROPERTY_KEY_FIELD))) {
              return row.get(PROPERTY_VALUE_FIELD);
            }
          }
        }
        return name;
      } else {
        final int i = name.indexOf('|');
        if (i >= 0) {
          return createFolderName(name.substring(i + 1));
        } else {
          return name;
        }
      }
    }

    /**
     * Patch the APPDIR to refer to either Platform32_Dir or Platform64_Dir (defined in Folders.MSM). The permissioning model for i386 and x64 must be the same for this approach to work.
     */
    public void setAppDir() {
      // Assume APPDIRs parent is platform32_Dir or platform64_Dir if either are defined (for permissioning purposes). We assume
      // that the permission model for i386 and x64 is the same.
      for (Map.Entry<String, FolderInfo> folderParent : _folderInfo.entrySet()) {
        if (folderParent.getKey().startsWith("Platform32_Dir") || folderParent.getKey().startsWith("Platform64_Dir")) {
          _folderInfo.put("APPDIR", new FolderInfo("APPDIR", folderParent.getKey(), "."));
          return;
        }
      }
    }

    private String getPath(final String folderId) {
      final FolderInfo folder = _folderInfo.get(folderId);
      if (folder == null) {
        return "";
      }
      if ("".equals(folder.getParent())) {
        return folder.getName();
      } else {
        if (".".equals(folder.getName())) {
          return getPath(folder.getParent());
        } else {
          return getPath(folder.getParent()) + "\\" + folder.getName();
        }
      }
    }

    /**
     * Find any aliases - paths defined in MSMs that match each other, or that match something defined in the AIP.
     */
    public void findAliases() {
      final Map<String, Set<String>> aliases = new HashMap<String, Set<String>>();
      for (FolderInfo folder : _folderInfo.values()) {
        final String path = getPath(folder.getId());
        Set<String> folderAliases = aliases.get(path);
        if (folderAliases == null) {
          folderAliases = new HashSet<String>();
          aliases.put(path, folderAliases);
        }
        folderAliases.add(folder.getId());
      }
      for (Set<String> alias : aliases.values()) {
        if (alias.size() > 1) {
          for (String folderId : alias) {
            final FolderInfo folder = _folderInfo.get(folderId);
            for (String aliasId : alias) {
              if (folderId != aliasId) {
                folder.addAlias(aliasId);
              }
            }
          }
        }
      }
    }

    private Lock getInheritedLock(final Set<String> folderIds) {
      final Set<String> parents = new HashSet<String>();
      for (String folderId : folderIds) {
        Lock lock = _permissions.getFolderLock(folderId);
        if (lock != null) {
          return lock;
        }
        final FolderInfo folder = _folderInfo.get(folderId);
        if (folder != null) {
          for (String aliasId : folder.getAliases()) {
            lock = _permissions.getFolderLock(aliasId);
            if (lock != null) {
              return lock;
            }
            final FolderInfo alias = _folderInfo.get(aliasId);
            if (alias != null) {
              parents.add(alias.getParent());
            }
          }
          parents.add(folder.getParent());
        }
      }
      if (parents.isEmpty()) {
        return null;
      }
      return getInheritedLock(parents);
    }

    /**
     * Sets explicit DACL entries on the folder if it has none by looking at the folder hierarchy for inheritable permissions.
     * 
     * @param folderId the folder being created, not null
     */
    public void patch(final String folderId) {
      if (_permissions.getFolderLock(folderId) != null) {
        System.out.println("Explicit lock " + folderId);
        return;
      }
      final FolderInfo folder = _folderInfo.get(folderId);
      if (folder == null) {
        throw new IllegalArgumentException(folderId);
      }
      final Lock lock = getInheritedLock(ImmutableSet.of(folderId));
      if (lock != null) {
        System.out.println("Implied lock " + folderId);
        _permissions.patch(lock, folderId);
      }
    }

  }

  /**
   * Adds an implied DACL for every entry in the <code>CreateFolder</code> table that doesn't have one and corresponds to a child folder of one which does have an explicit DACL.
   * 
   * @param file the AIP file to modify, not null
   * @param permissions the permissioning state, not null
   */
  private void patchFolderPermissions(final AIPFile file, final MsiPermissionsEx permissions) {
    final FolderPermissions folders = new FolderPermissions(file, permissions);
    folders.loadFolderInfo();
    folders.setAppDir();
    folders.findAliases();
    // Work through the explicit folder creation operations
    AIPFile.ComponentInfo component = file.get(CREATE_FOLDER_COMPONENT);
    if (component != null) {
      for (Map<String, String> row : component.rows()) {
        folders.patch(row.get(CREATE_FOLDER_DIRECTORY_FIELD));
      }
    }
    // Work through the inherited folder creation operations
    component = file.get(MERGE_MODULE_COMPONENT);
    if (component != null) {
      for (Map<String, String> row : component.rows()) {
        MSMTable msmCreateFolders = MSMTable.load(row.get(MERGE_MODULE_PATH_FIELD), MSMTable.CREATE_FOLDER_TABLE);
        if (msmCreateFolders != null) {
          for (MSMTable.Row createFolder : msmCreateFolders.rows()) {
            folders.patch(createFolder.get(MSMTable.CREATE_FOLDER_DIRECTORY_FIELD));
          }
        }
      }
    }
  }

  @Override
  protected void processFile(final AIPFile file) {
    final MsiPermissionsEx permissions = new MsiPermissionsEx(file);
    patchFolderPermissions(file, permissions);
    // TODO: Registry keys
  }

  public static void main(final String[] args) {
    for (String arg : args) {
      new PatchMsiPermissionsExTable(new File(arg)).run();
    }
  }

}
