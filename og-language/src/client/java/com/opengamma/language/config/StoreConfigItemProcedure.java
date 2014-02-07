/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.ContextRemoteClient;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.OpenGammaTypes;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.definition.types.TransportTypes;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;

/**
 * Stores a configuration item back into the {@link ConfigMaster}.
 */
public class StoreConfigItemProcedure extends AbstractProcedureInvoker.SingleResult implements PublishedProcedure {

  /**
   * Default instance.
   */
  public static final StoreConfigItemProcedure INSTANCE = new StoreConfigItemProcedure();

  private final MetaProcedure _meta;

  private static final int ITEM = 0;
  private static final int TYPE = 1;
  private static final int NAME = 2;
  private static final int IDENTIFIER = 3;
  private static final int MASTER = 4;

  private static List<MetaParameter> parameters() {
    final MetaParameter item = new MetaParameter("item", TransportTypes.FUDGE_MSG);
    final MetaParameter type = new MetaParameter("type", PrimitiveTypes.STRING_ALLOW_NULL);
    final MetaParameter name = new MetaParameter("name", PrimitiveTypes.STRING_ALLOW_NULL);
    final MetaParameter identifier = new MetaParameter("identifier", OpenGammaTypes.UNIQUE_ID_ALLOW_NULL);
    final MetaParameter master = new MetaParameter("master", JavaTypeInfo.builder(MasterID.class).defaultValue(MasterID.SESSION).get());
    return ImmutableList.of(item, type, name, identifier, master);
  }

  private StoreConfigItemProcedure(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaProcedure(Categories.CONFIG, "StoreConfigItem", getParameters(), this));
  }

  protected StoreConfigItemProcedure() {
    this(new DefinitionAnnotater(StoreConfigItemProcedure.class));
  }

  private static JavaTypeInfo<?> getType(final String type) {
    try {
      return JavaTypeInfo.parseString(type);
    } catch (RuntimeException e) {
      throw new InvokeInvalidArgumentException(TYPE, e);
    }
  }

  private static Object convert(final SessionContext sessionContext, final FudgeMsg item, final JavaTypeInfo<?> type) {
    try {
      return sessionContext.getGlobalContext().getValueConverter().convertValue(sessionContext, item, type);
    } catch (RuntimeException e) {
      throw new InvokeInvalidArgumentException(ITEM, e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static UniqueId invoke(final SessionContext sessionContext, final FudgeMsg item, final String type, final String name, final UniqueId identifier, final ConfigMaster master) {
    if (identifier != null) {
      ConfigDocument configDocument;
      try {
        configDocument = master.get(identifier);
      } catch (RuntimeException e) {
        throw new InvokeInvalidArgumentException(IDENTIFIER, e);
      }
      final JavaTypeInfo<?> javaType;
      if (type != null) {
        javaType = getType(type);
      } else {
        javaType = JavaTypeInfo.builder(configDocument.getConfig().getType()).get();
      }
      ((ConfigItem) configDocument.getConfig()).setValue(convert(sessionContext, item, javaType));
      if (name != null) {
        configDocument.setName(name);
      }
      try {
        configDocument = master.update(configDocument);
      } catch (RuntimeException e) {
        throw new InvokeInvalidArgumentException(IDENTIFIER, e);
      }
      return configDocument.getUniqueId();
    } else {
      final Class<?> valueType;
      final Object value;
      if (type != null) {
        final JavaTypeInfo<?> javaType = getType(type);
        valueType = javaType.getRawClass();
        value = convert(sessionContext, item, javaType);
      } else {
        if (item.hasField(0)) {
          try {
            value = (new FudgeDeserializer(FudgeTypeConverter.getFudgeContext(sessionContext.getGlobalContext()))).fudgeMsgToObject(item);
            valueType = value.getClass();
          } catch (RuntimeException e) {
            throw new InvokeInvalidArgumentException(ITEM, e);
          }
        } else {
          throw new InvokeInvalidArgumentException(TYPE, "type must be specified");
        }
      }
      final ConfigItem<?> configItem;
      if (name != null) {
        configItem = ConfigItem.of(value, name, valueType);
      } else {
        configItem = ConfigItem.of(value);
        if (configItem.getName() == null) {
          throw new InvokeInvalidArgumentException(NAME, "a name must be specified when adding an item");
        }
        configItem.setType(valueType);
      }
      try {
        return master.add(new ConfigDocument(configItem)).getUniqueId();
      } catch (RuntimeException e) {
        throw new InvokeInvalidArgumentException(ITEM, e);
      }
    }
  }

  protected static UniqueId invoke(final SessionContext sessionContext, final FudgeMsg item, final String type, final String name, final UniqueId identifier, final MasterID master) {
    final RemoteClient client = ContextRemoteClient.get(sessionContext, master);
    final ConfigMaster configMaster;
    try {
      configMaster = client.getConfigMaster();
    } catch (UnsupportedOperationException e) {
      throw new InvokeInvalidArgumentException(MASTER, e);
    }
    return invoke(sessionContext, item, type, name, identifier, configMaster);
  }

  // AbstractProcedureInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (FudgeMsg) parameters[ITEM], (String) parameters[TYPE], (String) parameters[NAME], (UniqueId) parameters[IDENTIFIER], (MasterID) parameters[MASTER]);
  }

  // PublishedProcedure

  @Override
  public MetaProcedure getMetaProcedure() {
    return _meta;
  }

}
