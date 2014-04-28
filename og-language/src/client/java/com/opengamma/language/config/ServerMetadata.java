/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to server metadata.
 */
public class ServerMetadata {

  private static final Logger s_logger = LoggerFactory.getLogger(ServerMetadata.class);

  private static final String LSID = "lsid";
  private static final String DESCRIPTION = "description";
  private static final String COMPONENT_HOST_CONFIGURATION = "/jax/components";
  private static final String VERSION_NUMBER = "version";
  private static final String BUILD_NUMBER = "build";

  private final ConfigurationSourceBean _configuration;

  public ServerMetadata(final ConfigurationSourceBean configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  protected ConfigurationSourceBean getConfigurationSource() {
    return _configuration;
  }

  protected FudgeMsg getConfigurationMessage() {
    return getConfigurationSource().fetchConfigurationMessage(true);
  }

  public String getConfigurationURL() {
    return getConfigurationSource().getConfigurationURI();
  }

  public void setConfigurationURL(final String configurationURL) {
    // TODO: This might make sense, but will affect all connected clients so some form of ACL is sensible and/or fork the GC
    throw new UnsupportedOperationException("Can't set configuration URL");
  }

  public String getLogicalServerId() {
    return getConfigurationMessage().getString(LSID);
  }

  public void setLogicalServerId(final String lsid) {
    throw new UnsupportedOperationException("Can't set logical server identifier");
  }

  public FudgeMsg getPublishedConfiguration() {
    return getConfigurationMessage();
  }

  public void setPublishedConfiguration(final FudgeMsg configuration) {
    throw new UnsupportedOperationException("Can't set published configuration");
  }

  public String getServerDescription() {
    return getConfigurationMessage().getString(DESCRIPTION);
  }

  public void setServerDescription(final String description) {
    throw new UnsupportedOperationException("Can't set server description");
  }

  /**
   * Returns the version number of the named component, if available. Most components are published by a host which advertises all of its components, and version number, at /jax/components.
   * <p>
   * When all components are hosted by a single process, the choice of component is trivial. In the case of a multiple process deployment then the version number of the "viewProcessor" is usually the
   * best choice.
   * 
   * @param component configuration item name of a component with one or more URI REST end-points
   * @return the version string, or null if none can be found
   */
  public String getServerVersion(final String component) {
    final FudgeMsg configuration = getConfigurationMessage().getMessage(component);
    if (configuration == null) {
      s_logger.warn("No {} component in server configuration for version lookup", component);
      return null;
    }
    if (!UriEndPointDescriptionProvider.TYPE_VALUE.equals(configuration.getString(UriEndPointDescriptionProvider.TYPE_KEY))) {
      s_logger.warn("Component {} is not a URI end-point for version lookup", component);
      return null;
    }
    s_logger.debug("Checking version of {}", component);
    for (FudgeField uriField : configuration.getAllByName(UriEndPointDescriptionProvider.URI_KEY)) {
      if (uriField.getValue() instanceof String) {
        final String uriString = (String) uriField.getValue();
        try {
          URI uri = getConfigurationSource().getConfigurationURIAsURI().resolve(uriString);
          s_logger.debug("Resolved component to {}", uri);
          uri = uri.resolve(COMPONENT_HOST_CONFIGURATION);
          s_logger.debug("Resolved component host to {}", uri);
          final RemoteConfiguration configClient = new RemoteConfiguration(uri);
          final FudgeMsg configMsg = configClient.getConfigurationMsg();
          final String version = configMsg.getString(VERSION_NUMBER);
          if (version != null) {
            final String build = configMsg.getString(BUILD_NUMBER);
            if (s_logger.isInfoEnabled()) {
              s_logger.info("Component {} hosted by build {}, version {}", new Object[] {component, build, version });
            }
            return version;
          }
        } catch (Throwable t) {
          s_logger.warn("Couldn't query {} - {}", uriString, t);
          s_logger.debug("Caught exception", t);
        }
      }
    }
    s_logger.warn("No version available from host of {}", component);
    return null;
  }

  public void setServerVersion(final String component, final String version) {
    throw new UnsupportedOperationException("Can't set server version");
  }

}
