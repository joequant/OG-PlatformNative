/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.identifier;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.types.OpenGammaTypes;

/**
 * Converts identifier types. For example the ExternalId to/from a singleton bundle.
 */
public class IdentifierConverter extends AbstractMappedConverter {

  /**
   * Default instance.
   */
  public static final IdentifierConverter INSTANCE = new IdentifierConverter();

  protected IdentifierConverter() {
    conversion(TypeMap.ZERO_LOSS, OpenGammaTypes.EXTERNAL_ID_ALLOW_NULL, OpenGammaTypes.EXTERNAL_ID_BUNDLE_ALLOW_NULL, new Action<ExternalId, ExternalIdBundle>() {
      @Override
      protected ExternalIdBundle convert(final ExternalId value) {
        return ExternalIdBundle.of(value);
      }
    }, new Action<ExternalIdBundle, ExternalId>() {

      @Override
      protected ExternalIdBundle cast(final Object value) {
        final ExternalIdBundle bundle = (ExternalIdBundle) value;
        if (bundle.getExternalIds().size() == 1) {
          return bundle;
        } else {
          return null;
        }
      }

      @Override
      protected ExternalId convert(final ExternalIdBundle value) {
        return value.iterator().next();
      }

    });
    conversion(TypeMap.ZERO_LOSS, OpenGammaTypes.UNIQUE_ID_ALLOW_NULL, OpenGammaTypes.OBJECT_ID_ALLOW_NULL, new Action<UniqueId, ObjectId>() {
      @Override
      protected ObjectId convert(final UniqueId value) {
        return value.getObjectId();
      }
    }, new Action<ObjectId, UniqueId>() {
      @Override
      protected UniqueId convert(final ObjectId value) {
        return value.atLatestVersion();
      }
    });
  }

}
