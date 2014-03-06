/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.install.msi;

import java.io.File;
import java.util.Map;

/**
 * Modify an AIP file to include the default parameters for the MSMs.
 */
public class MergeModDefaultParameters extends AbstractAipProcessor {

  public MergeModDefaultParameters(final File aipFile) {
    super(aipFile);
  }

  private String mergeModuleParams(final String msmPath) {
    final MSMTable table = MSMTable.load(msmPath, MSMTable.MODULE_CONFIGURATION_TABLE);
    final StringBuilder sb = new StringBuilder();
    for (MSMTable.Row parameter : table.rows()) {
      final String name = parameter.get(MSMTable.MODULE_CONFIGURATION_NAME_FIELD);
      sb.append(name).append("=[").append(name).append("];");
    }
    final String result = sb.toString();
    System.out.println("Found " + result + " for " + msmPath);
    return result;
  }

  @Override
  protected void processFile(final AIPFile file) {
    final AIPFile.ComponentInfo mergeMods = file.get(MERGE_MODULE_COMPONENT);
    for (Map<String, String> mergeMod : mergeMods.rows()) {
      final String path = mergeMod.get(MERGE_MODULE_PATH_FIELD);
      if (path == null) {
        continue;
      }
      final String expectedParams = mergeModuleParams(path);
      final String params = mergeMod.get(MERGE_MODULE_PARAMS_FIELD);
      if ((params != null) && !params.equals(expectedParams)) {
        throw new IllegalStateException("Assumed parameters - " + expectedParams + " different to " + params);
      }
      mergeMod.put(MERGE_MODULE_PARAMS_FIELD, expectedParams);
    }
  }

  public static void main(final String[] args) {
    for (String arg : args) {
      new MergeModDefaultParameters(new File(arg)).run();
    }
  }

}
