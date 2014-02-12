/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.object;

import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;

/**
 * Extends the contexts with object manipulation support.
 */
public class Loader extends ContextInitializationBean {

  private PropertyTypeInferer _propertyTypeInferer;

  public void setPropertyTypeInferer(final PropertyTypeInferer propertyTypeInferer) {
    _propertyTypeInferer = propertyTypeInferer;
  }

  public PropertyTypeInferer getPropertyTypeInferer() {
    return _propertyTypeInferer;
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    // First call will set up a default inferer and the functions from this package
    if (SetObjectPropertyFunction.getPropertyTypeInferer(globalContext) == null) {
      globalContext.getFunctionProvider().addProvider(new BeanFunctionProvider());
      globalContext.getFunctionProvider().addProvider(new ObjectFunctionProvider());
      SetObjectPropertyFunction.setPropertyTypeInferer(globalContext, new DefaultPropertyTypeInferer(new NullPropertyTypeInferer()));
    }
    // Later calls can customise the behaviour
    if (getPropertyTypeInferer() != null) {
      SetObjectPropertyFunction.setPropertyTypeInferer(globalContext, getPropertyTypeInferer());
    }
  }

}
