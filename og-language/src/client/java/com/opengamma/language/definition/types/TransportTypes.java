/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import org.fudgemsg.FudgeMsg;

import com.opengamma.language.Data;
import com.opengamma.language.Value;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to the transport types.
 */
public final class TransportTypes {

  /**
   * Prevents instantiation.
   */
  private TransportTypes() {
  }

  public static final JavaTypeInfo<Data> DATA = JavaTypeInfo.builder(Data.class).get();

  public static final JavaTypeInfo<Data> DATA_ALLOW_NULL = JavaTypeInfo.builder(Data.class).allowNull().get();

  public static final JavaTypeInfo<FudgeMsg> FUDGE_MSG = JavaTypeInfo.builder(FudgeMsg.class).get();

  public static final JavaTypeInfo<FudgeMsg> FUDGE_MSG_ALLOW_NULL = JavaTypeInfo.builder(FudgeMsg.class).allowNull().get();

  public static final JavaTypeInfo<Value> VALUE = JavaTypeInfo.builder(Value.class).get();

  public static final JavaTypeInfo<Value> VALUE_ALLOW_NULL = JavaTypeInfo.builder(Value.class).allowNull().get();

}
