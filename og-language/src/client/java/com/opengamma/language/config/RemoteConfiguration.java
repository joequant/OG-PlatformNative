/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.transport.jaxrs.FudgeObjectBinaryConsumer;
import com.opengamma.transport.jaxrs.FudgeObjectBinaryProducer;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.rest.FudgeRestClient;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Remote client for accessing remote {@code Configuration}.
 */
public final class RemoteConfiguration extends AbstractRemoteClient {

  private static final class CustomFudgeRestClient extends FudgeRestClient {

    private static Client createClient() {
      final DefaultClientConfig config = getDefaultClientConfig();
      config.getClasses().remove(FudgeObjectBinaryConsumer.class);
      config.getSingletons().add(new FudgeObjectBinaryConsumer(FudgeContext.GLOBAL_DEFAULT));
      config.getClasses().remove(FudgeObjectBinaryProducer.class);
      config.getSingletons().add(new FudgeObjectBinaryProducer(FudgeContext.GLOBAL_DEFAULT));
      final Client client = Client.create(config);
      configureDefaultClient(client);
      return client;
    }

    private CustomFudgeRestClient() {
      super(createClient());
    }

  }

  /**
   * Creates the resource.
   * 
   * @param baseUri the base URI, not null
   */
  public RemoteConfiguration(final URI baseUri) {
    super(baseUri, new CustomFudgeRestClient());
  }

  /**
   * Gets the remote configuration at the URI.
   * 
   * @return the configuration message, null if not found
   */
  public FudgeMsg getConfigurationMsg() {
    try {
      return accessRemote(getBaseUri()).get(FudgeMsg.class);
    } catch (final UniformInterfaceException404NotFound ex) {
      return null;
    }
  }

}
