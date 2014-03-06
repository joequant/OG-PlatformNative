/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import java.io.File;
import java.io.IOException;

import com.opengamma.util.ArgumentChecker;

/**
 * Common class for an AIP modifying tool.
 * <p>
 * An instance is constructed with reference to an AIP file. This is loaded and held in memory. The in-memory structure is processed and, if no exceptions occur, is then written back out to a
 * temporary file. The temporary file then overwrites the original.
 */
/* package */abstract class AbstractAipProcessor implements Runnable {

  public static final String CREATE_FOLDER_COMPONENT = "caphyon.advinst.msicomp.MsiCreateFolderComponent";
  public static final String CREATE_FOLDER_DIRECTORY_FIELD = MSMTable.CREATE_FOLDER_DIRECTORY_FIELD;

  public static final String DIRECTORY_COMPONENT = "caphyon.advinst.msicomp.MsiDirsComponent";
  public static final String DIRECTORY_KEY_FIELD = MSMTable.DIRECTORY_KEY_FIELD;
  public static final String DIRECTORY_PARENT_FIELD = MSMTable.DIRECTORY_PARENT_FIELD;
  public static final String DIRECTORY_NAME_FIELD = MSMTable.DIRECTORY_NAME_FIELD;

  public static final String MERGE_MODULE_COMPONENT = "caphyon.advinst.msicomp.MsiMergeModsComponent";
  public static final String MERGE_MODULE_PATH_FIELD = "Path";
  public static final String MERGE_MODULE_PARAMS_FIELD = "Params";

  public static final String MSI_LOCK_PERMISSIONS_EX_COMPONENT = "caphyon.advinst.msicomp.MsiLockPermissionsExComponent";
  public static final String MSI_LOCK_PERMISSIONS_EX_KEY_FIELD = MSMTable.MSI_LOCK_PERMISSIONS_EX_KEY_FIELD;
  public static final String MSI_LOCK_PERMISSIONS_EX_OBJECT_FIELD = MSMTable.MSI_LOCK_PERMISSIONS_EX_OBJECT_FIELD;
  public static final String MSI_LOCK_PERMISSIONS_EX_TABLE_FIELD = MSMTable.MSI_LOCK_PERMISSIONS_EX_TABLE_FIELD;
  public static final String MSI_LOCK_PERMISSIONS_EX_SDDL_FIELD = MSMTable.MSI_LOCK_PERMISSIONS_EX_SDDL_FIELD;
  public static final String MSI_LOCK_PERMISSIONS_EX_CONDITION_FIELD = MSMTable.MSI_LOCK_PERMISSIONS_EX_CONDITION_FIELD;

  public static final String PROPERTY_COMPONENT = "caphyon.advinst.msicomp.MsiPropsComponent";
  public static final String PROPERTY_KEY_FIELD = MSMTable.PROPERTY_KEY_FIELD;
  public static final String PROPERTY_VALUE_FIELD = MSMTable.PROPERTY_VALUE_FIELD;

  private final File _aipFile;

  public AbstractAipProcessor(final File aipFile) {
    _aipFile = ArgumentChecker.notNull(aipFile, "aipFile");
  }

  protected File getAipFile() {
    return _aipFile;
  }

  protected File createTempFile() {
    try {
      return File.createTempFile("installer", ".aip");
    } catch (IOException ex) {
      throw new RuntimeException("Couldn't create temporary file", ex);
    }
  }

  /**
   * Applies the operation(s) to the in-memory representation of the AIP file.
   * 
   * @param file the file to modify, not null
   */
  protected abstract void processFile(AIPFile file);

  protected void processFile(final File source, final File dest) {
    final AIPFile file = AIPFile.load(source);
    processFile(file);
    file.save(dest);
  }

  protected void renameTempFile(final File tmp) {
    if (!getAipFile().delete()) {
      throw new RuntimeException("Couldn't delete " + getAipFile() + " before renaming temporary file");
    }
    if (!tmp.renameTo(getAipFile())) {
      throw new RuntimeException("Couldn't rename " + tmp + " to " + getAipFile());
    }
  }

  @Override
  public void run() {
    final File tmp = createTempFile();
    try {
      processFile(getAipFile(), tmp);
      renameTempFile(tmp);
    } finally {
      tmp.delete();
    }
  }

}
