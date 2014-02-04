/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to the OpenGamma utility types.
 */
public final class OpenGammaTypes {

  /**
   * Prevents instantiation.
   */
  private OpenGammaTypes() {
  }

  public static final JavaTypeInfo<Currency> CURRENCY = JavaTypeInfo.builder(Currency.class).get();

  public static final JavaTypeInfo<Expiry> EXPIRY = JavaTypeInfo.builder(Expiry.class).get();

  public static final JavaTypeInfo<Expiry> EXPIRY_ALLOW_NULL = JavaTypeInfo.builder(Expiry.class).allowNull().get();

  public static final JavaTypeInfo<ExternalId> EXTERNAL_ID = JavaTypeInfo.builder(ExternalId.class).get();

  public static final JavaTypeInfo<ExternalId> EXTERNAL_ID_ALLOW_NULL = JavaTypeInfo.builder(ExternalId.class).allowNull().get();

  public static final JavaTypeInfo<ExternalIdBundle> EXTERNAL_ID_BUNDLE = JavaTypeInfo.builder(ExternalIdBundle.class).get();

  public static final JavaTypeInfo<ExternalIdBundle> EXTERNAL_ID_BUNDLE_ALLOW_NULL = JavaTypeInfo.builder(ExternalIdBundle.class).allowNull().get();

  public static final JavaTypeInfo<ObjectId> OBJECT_ID = JavaTypeInfo.builder(ObjectId.class).get();

  public static final JavaTypeInfo<ObjectId> OBJECT_ID_ALLOW_NULL = JavaTypeInfo.builder(ObjectId.class).allowNull().get();

  public static final JavaTypeInfo<Tenor> TENOR = JavaTypeInfo.builder(Tenor.class).get();

  public static final JavaTypeInfo<UniqueId> UNIQUE_ID = JavaTypeInfo.builder(UniqueId.class).get();

  public static final JavaTypeInfo<UniqueId> UNIQUE_ID_ALLOW_NULL = JavaTypeInfo.builder(UniqueId.class).allowNull().get();

}
