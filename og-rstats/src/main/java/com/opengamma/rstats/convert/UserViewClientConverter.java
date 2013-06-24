/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.rstats.convert;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opengamma.id.UniqueId;
import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.invoke.AbstractTypeConverter;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.language.view.AttachedViewClientHandle;
import com.opengamma.language.view.DetachedViewClientHandle;
import com.opengamma.language.view.ViewClientHandle;
import com.opengamma.rstats.data.RDataInfo;

/**
 * Converts a view client handle to an R-object representing the detached handle. When the R-object is discarded, the handle
 * will be released. 
 */
public class UserViewClientConverter extends AbstractTypeConverter {

  private static final String R_CLASS = "ViewClient";

  /**
   * Destructor function called when the R-object is discarded.
   */
  public static class Destructor implements PublishedProcedure {

    @Override
    public MetaProcedure getMetaProcedure() {
      final MetaParameter arg = new MetaParameter("identifier", JavaTypeInfo.builder(UniqueId.class).get());
      final List<MetaParameter> args = Arrays.asList(arg);
      return new MetaProcedure(Categories.VIEW, "destroy." + R_CLASS, args, new AbstractProcedureInvoker.NoResult(args) {
        @Override
        protected void invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
          final UniqueId identifier = (UniqueId) parameters[0];
          final DetachedViewClientHandle handle = sessionContext.getViewClients().lockViewClient(identifier);
          if (handle != null) {
            handle.attachAndUnlock();
          }
        }
      });
    }
  }

  private static final JavaTypeInfo<UniqueId> UNIQUE_ID = JavaTypeInfo.builder(UniqueId.class).get();
  private static final JavaTypeInfo<ViewClientHandle> VIEW_CLIENT_HANDLE = JavaTypeInfo.builder(ViewClientHandle.class).get();

  private static final TypeMap TO_DATA = TypeMap.of(ZERO_LOSS, VIEW_CLIENT_HANDLE);
  private static final TypeMap TO_VIEW_CLIENT_HANDLE = TypeMap.of(ZERO_LOSS, UNIQUE_ID);

  @Override
  public String getTypeConverterKey() {
    return com.opengamma.language.view.UserViewClientConverter.getTypeConverterKeyImpl();
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return (targetType.getRawClass() == Data.class) || (targetType.getRawClass() == ViewClientHandle.class);
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    if (type.getRawClass() == Data.class) {
      final UniqueId detached;
      if (value instanceof AttachedViewClientHandle) {
        final AttachedViewClientHandle viewClient = (AttachedViewClientHandle) value;
        detached = viewClient.detachAndUnlock(conversionContext.getSessionContext());
      } else if (value instanceof DetachedViewClientHandle) {
        final DetachedViewClientHandle viewClient = (DetachedViewClientHandle) value;
        detached = viewClient.detachAndUnlock();
      } else {
        conversionContext.setFail();
        return;
      }
      conversionContext.setResult(RDataInfo.create().wrapperClass(R_CLASS).applyTo(DataUtils.of(detached.toString())));
    } else {
      final UniqueId identifier = (UniqueId) value;
      conversionContext.setResult(conversionContext.getSessionContext().getViewClients().lockViewClient(identifier));
    }
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    if (targetType.getRawClass() == Data.class) {
      return TO_DATA;
    } else {
      return TO_VIEW_CLIENT_HANDLE;
    }
  }

}
