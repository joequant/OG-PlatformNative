/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.language.context.AbstractGlobalContextEventHandler;
import com.opengamma.language.context.GlobalContextEventHandler;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.test.TestUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SystemInfoMessageHandler} class
 */
@Test(groups = TestGroup.UNIT)
public class SystemInfoMessageHandlerTest {

  private SessionContext createContext(final ServerMetadata metadata) {
    final TestUtils testUtils = new TestUtils() {
      @Override
      protected GlobalContextEventHandler createGlobalContextEventHandler() {
        return new AbstractGlobalContextEventHandler(super.createGlobalContextEventHandler()) {
          @Override
          public void initContextImpl(final MutableGlobalContext globalContext) {
            globalContext.setServerMetadata(metadata);
          }
        };
      }
    };
    return testUtils.createSessionContext();
  }

  public void testGetConfigurationURL() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    Mockito.when(metadata.getConfigurationURL()).thenReturn("config url");
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addGet(SystemInfo.CONFIGURATION_URL_ORDINAL);
    SystemInfoMessageHandler.handle(message, context);
    assertNull(message.getGet());
    assertEquals(message.getSet(), Collections.singleton(SystemInfo.CONFIGURATION_URL_ORDINAL));
    assertEquals(message.getConfigurationURL(), "config url");
  }

  public void testSetConfigurationURL() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addSet(SystemInfo.CONFIGURATION_URL_ORDINAL);
    message.setConfigurationURL("new url");
    SystemInfoMessageHandler.handle(message, context);
    Mockito.verify(metadata).setConfigurationURL("new url");
  }

  public void testGetLSID() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    Mockito.when(metadata.getLogicalServerId()).thenReturn("lsid");
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addGet(SystemInfo.LSID_ORDINAL);
    SystemInfoMessageHandler.handle(message, context);
    assertNull(message.getGet());
    assertEquals(message.getSet(), Collections.singleton(SystemInfo.LSID_ORDINAL));
    assertEquals(message.getLsid(), "lsid");
  }

  public void testSetLSID() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addSet(SystemInfo.LSID_ORDINAL);
    message.setLsid("new lsid");
    SystemInfoMessageHandler.handle(message, context);
    Mockito.verify(metadata).setLogicalServerId("new lsid");
  }

  public void testGetPublishedConfiguration() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    Mockito.when(metadata.getPublishedConfiguration()).thenReturn(FudgeContext.EMPTY_MESSAGE);
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addGet(SystemInfo.PUBLISHED_CONFIGURATION_ORDINAL);
    SystemInfoMessageHandler.handle(message, context);
    assertNull(message.getGet());
    assertEquals(message.getSet(), Collections.singleton(SystemInfo.PUBLISHED_CONFIGURATION_ORDINAL));
    assertEquals(message.getPublishedConfiguration(), FudgeContext.EMPTY_MESSAGE);
  }

  public void testSetPublishedConfiguration() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addSet(SystemInfo.PUBLISHED_CONFIGURATION_ORDINAL);
    message.setPublishedConfiguration(FudgeContext.EMPTY_MESSAGE);
    SystemInfoMessageHandler.handle(message, context);
    Mockito.verify(metadata).setPublishedConfiguration(FudgeContext.EMPTY_MESSAGE);
  }

  public void testGetServerDescription() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    Mockito.when(metadata.getServerDescription()).thenReturn("description");
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addGet(SystemInfo.SERVER_DESCRIPTION_ORDINAL);
    SystemInfoMessageHandler.handle(message, context);
    assertNull(message.getGet());
    assertEquals(message.getSet(), Collections.singleton(SystemInfo.SERVER_DESCRIPTION_ORDINAL));
    assertEquals(message.getServerDescription(), "description");
  }

  public void testSetServerDescription() {
    final ServerMetadata metadata = Mockito.mock(ServerMetadata.class);
    final SessionContext context = createContext(metadata);
    final SystemInfo message = new SystemInfo();
    message.addSet(SystemInfo.SERVER_DESCRIPTION_ORDINAL);
    message.setServerDescription("new description");
    SystemInfoMessageHandler.handle(message, context);
    Mockito.verify(metadata).setServerDescription("new description");
  }

}
