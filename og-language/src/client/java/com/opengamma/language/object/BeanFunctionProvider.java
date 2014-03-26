/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import java.util.Collection;

import org.joda.beans.Bean;

import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;

/**
 * Provider of constructor and property accessor/mutator functions for Joda {@link Bean} classes.
 */
public class BeanFunctionProvider extends AbstractFunctionProvider {

  @Override
  protected void loadDefinitions(final Collection<MetaFunction> definitions) {
    definitions.add(ObjectFunction.INSTANCE.getMetaFunction());
    definitions.add(GetObjectPropertiesFunction.INSTANCE.getMetaFunction());
    definitions.add(GetObjectPropertyFunction.INSTANCE.getMetaFunction());
    definitions.add(SetObjectPropertiesFunction.INSTANCE.getMetaFunction());
    definitions.add(SetObjectPropertyFunction.INSTANCE.getMetaFunction());
    definitions.add(InvokeObjectMethodFunction.INSTANCE.getMetaFunction());
  }
}
