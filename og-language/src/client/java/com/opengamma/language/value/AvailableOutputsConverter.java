/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.types.CollectionTypes;
import com.opengamma.language.definition.types.EngineTypes;

/**
 * Converts a {@link AvailableOutputs} instance to a set of value requirement names. If a specific language binding needs to expose more information, replace this converter with a more appropriate one
 * during context initialization.
 */
public class AvailableOutputsConverter extends AbstractMappedConverter {

  /**
   * Default instance.
   */
  public static final AvailableOutputsConverter INSTANCE = new AvailableOutputsConverter();

  protected AvailableOutputsConverter() {
    conversion(TypeMap.ZERO_LOSS, EngineTypes.AVAILABLE_OUTPUTS, CollectionTypes.MAP, new Action<AvailableOutputs, Map>() {
      @Override
      protected Map convert(final AvailableOutputs value) {
        final Set<AvailableOutput> outputs = value.getOutputs();
        final Map<String, ValueProperties> map = Maps.newHashMapWithExpectedSize(outputs.size());
        for (AvailableOutput output : outputs) {
          map.put(output.getValueName(), output.getProperties());
        }
        return map;
      }
    });
  }

}
