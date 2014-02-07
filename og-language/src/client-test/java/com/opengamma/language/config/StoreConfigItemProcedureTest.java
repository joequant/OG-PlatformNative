/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import static org.testng.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.id.UniqueId;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.convert.FudgeTypeConverter;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.language.test.TestUtils;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link StoreConfigItemProcedure} class.
 */
@Test(groups = TestGroup.UNIT)
public class StoreConfigItemProcedureTest {

  public static class NamelessItem {
  }

  private ViewDefinition getMockConfigObject(final int i) {
    return new ViewDefinition("Test view " + i, "me");
  }

  private FudgeMsg getMockConfigObject(final SessionContext context, final int i, final boolean headers) {
    final FudgeSerializer serializer = new FudgeSerializer(FudgeTypeConverter.getFudgeContext(context.getGlobalContext()));
    final ViewDefinition object = getMockConfigObject(i);
    final MutableFudgeMsg msg = serializer.objectToFudgeMsg(object);
    if (headers) {
      FudgeSerializer.addClassHeader(msg, object.getClass());
    }
    return msg;
  }

  private RemoteClient createSessionClient() {
    final InMemoryConfigMaster master = new InMemoryConfigMaster();
    master.add(new ConfigDocument(ConfigItem.of(getMockConfigObject(1))));
    return new RemoteClient(null, null, null) {
      @Override
      public ConfigMaster getConfigMaster() {
        return master;
      }
    };
  }

  private SessionContext createSessionContext() {
    final TestUtils testUtils = new TestUtils();
    testUtils.setSessionClient(createSessionClient());
    testUtils.setTypeConverters(new Converters());
    return testUtils.createSessionContext();
  }

  public void testByIdentifier_1() {
    final SessionContext context = createSessionContext();
    final UniqueId id = StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 1, true), null, null, UniqueId.of("MemCfg", "1"), MasterID.SESSION);
    assertEquals(id, UniqueId.of("MemCfg", "1", "2"));
  }

  public void testByIdentifier_2() {
    final SessionContext context = createSessionContext();
    final UniqueId id = StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 1, false), null, null, UniqueId.of("MemCfg", "1"), MasterID.SESSION);
    assertEquals(id, UniqueId.of("MemCfg", "1", "2"));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidType_1() {
    final SessionContext context = createSessionContext();
    StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 1, false), YieldCurveDefinition.class.getName(), null, UniqueId.of("MemCfg", "1"), MasterID.SESSION);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testInvalidType_2() {
    final SessionContext context = createSessionContext();
    StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 1, false), "com.opengamma.does.not.exist.Foo", null, UniqueId.of("MemCfg", "1"), MasterID.SESSION);
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByBadIdentifier() {
    final SessionContext context = createSessionContext();
    StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 1, true), null, null, UniqueId.of("MemCfg", "99"), MasterID.SESSION);
  }

  public void testByIdentifierTypeAndName() {
    final SessionContext context = createSessionContext();
    final UniqueId id = StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 1, false), ViewDefinition.class.getName(), "Test view updated", UniqueId.of("MemCfg", "1"),
        MasterID.SESSION);
    assertEquals(id, UniqueId.of("MemCfg", "1", "2"));
    assertEquals(context.getClient().getConfigMaster().get(id).getName(), "Test view updated");
  }

  public void testByName() {
    final SessionContext context = createSessionContext();
    final UniqueId id = StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 3, true), null, "Test view", null, MasterID.SESSION);
    assertEquals(id, UniqueId.of("MemCfg", "2", "2"));
    assertEquals(context.getClient().getConfigMaster().get(id).getName(), "Test view");
    assertEquals(context.getClient().getConfigMaster().get(id).getConfig().getValue(), getMockConfigObject(3));
  }

  public void testByTypeAndName() {
    final SessionContext context = createSessionContext();
    final UniqueId id = StoreConfigItemProcedure.invoke(context, FudgeContext.EMPTY_MESSAGE, NamelessItem.class.getName(), "Test item", null, MasterID.SESSION);
    assertEquals(id, UniqueId.of("MemCfg", "2", "2"));
    assertEquals(context.getClient().getConfigMaster().get(id).getName(), "Test item");
    assertEquals(context.getClient().getConfigMaster().get(id).getConfig().getValue().getClass(), NamelessItem.class);
  }

  public void testByInferredName() {
    final SessionContext context = createSessionContext();
    final UniqueId id = StoreConfigItemProcedure.invoke(context, getMockConfigObject(context, 3, true), null, null, null, MasterID.SESSION);
    assertEquals(id, UniqueId.of("MemCfg", "2", "2"));
    assertEquals(context.getClient().getConfigMaster().get(id).getName(), "Test view 3");
    assertEquals(context.getClient().getConfigMaster().get(id).getConfig().getValue(), getMockConfigObject(3));
  }

  @Test(expectedExceptions = InvokeInvalidArgumentException.class)
  public void testByInferredName_fail() {
    final SessionContext context = createSessionContext();
    StoreConfigItemProcedure.invoke(context, FudgeContext.EMPTY_MESSAGE, NamelessItem.class.getName(), null, null, MasterID.SESSION);
  }

}
