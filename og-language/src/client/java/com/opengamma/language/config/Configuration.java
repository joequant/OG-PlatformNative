/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.opengamma.language.connector.AsyncSupplier;
import com.opengamma.language.connector.ConnectorStartupError;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.function.Function;

/**
 * Configuration document describing how to connect to or otherwise work with components from an OpenGamma installation. Configuration information is typically published at a URL as a Fudge message
 * containing further URLs and relevant configuration strings.
 */
public final class Configuration {

  private static final Logger s_logger = LoggerFactory.getLogger(Configuration.class);

  private final Supplier<FudgeContext> _fudgeContext;
  private final AsyncSupplier<FudgeMsg> _configuration;
  private final UriEndPointDescriptionProvider.Validater _uriValidater;
  private boolean _failOnInvalidURI;
  private boolean _failOnMissingConfiguration;

  protected Configuration(final Supplier<FudgeContext> fudgeContext, final AsyncSupplier<FudgeMsg> configuration, final UriEndPointDescriptionProvider.Validater uriValidater) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(configuration, "configuration");
    ArgumentChecker.notNull(uriValidater, "uriValidater");
    _fudgeContext = fudgeContext;
    _configuration = configuration;
    _uriValidater = uriValidater;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext.get();
  }

  protected AsyncSupplier<FudgeMsg> getConfiguration() {
    return _configuration;
  }

  protected void setFailOnInvalidURI(final boolean failOnInvalidURI) {
    _failOnInvalidURI = failOnInvalidURI;
  }

  protected boolean isFailOnInvalidURI() {
    return _failOnInvalidURI;
  }

  protected void setFailOnMissingConfiguration(final boolean failOnMissingConfiguration) {
    _failOnMissingConfiguration = failOnMissingConfiguration;
  }

  protected boolean isFailOnMissingConfiguration() {
    return _failOnMissingConfiguration;
  }

  protected UriEndPointDescriptionProvider.Validater getURIValidater() {
    return _uriValidater;
  }

  protected <T> T missingConfiguration(final String entry) {
    if (isFailOnMissingConfiguration()) {
      throw new ConnectorStartupError("No configuration data for " + entry + " was published by the server",
          "The \"fail on missing configuration\" flag is set to TRUE which has prevented the system from starting. " +
              "Either set the flag to FALSE or correct the server configuration to include " + entry);
    } else {
      s_logger.debug("Ignoring missing configuration {}", entry);
    }
    return null;
  }

  protected <T> T invalidUrl(final String entry, final Collection<String> addresses) {
    if (isFailOnInvalidURI()) {
      throw new ConnectorStartupError("The published address(es) of " + entry + " did not respond",
          "The \"fail on invalid URI\" flag is set to TRUE which has prevented the system from starting. " +
              "Either set the flag to FALSE, correct the server configuration to use a different address for " + entry + ", or " + "try again in a few moments.",
          "The addresses tried were: " + StringUtils.join(addresses, ", "));
    } else {
      s_logger.debug("Ignoring invalid URI for {}", entry);
    }
    return null;
  }

  /**
   * Returns a sub-configuration document.
   * 
   * @param entry configuration item name
   * @return the configuration document, or null if there is none (and passive failure is allowed)
   */
  public Configuration getSubConfiguration(final String entry) {
    return new Configuration(_fudgeContext, new AsyncSupplier.Filter<FudgeMsg, FudgeMsg>(getConfiguration(), new Function<FudgeMsg, FudgeMsg>() {
      @Override
      public FudgeMsg apply(final FudgeMsg instance) {
        final FudgeMsg submsg = instance.getMessage(entry);
        if (submsg != null) {
          return submsg;
        } else {
          s_logger.warn("No sub-configuration {}", entry);
          return missingConfiguration(entry);
        }
      }
    }), getURIValidater());
  }

  /**
   * Returns a REST end point as a {@link URI}
   * 
   * @param entry configuration item name
   * @return the URI supplier
   */
  public AsyncSupplier<URI> getURIConfiguration(final String entry) {
    return new AsyncSupplier.Filter<FudgeMsg, URI>(getConfiguration(), new Function<FudgeMsg, URI>() {
      @Override
      public URI apply(final FudgeMsg instance) {
        s_logger.info("Searching for accessible URI for {}", entry);
        final FudgeMsg submsg = instance.getMessage(entry);
        if (submsg == null) {
          s_logger.warn("No URI for {}", entry);
          return missingConfiguration(entry);
        }
        final URI uri = getURIValidater().getAccessibleURI(submsg);
        if (uri != null) {
          return uri;
        } else {
          s_logger.warn("No accessible URI for {}", entry);
          s_logger.debug("Tried {}", submsg);
          return invalidUrl(entry, getURIValidater().getAllURIStrings(submsg));
        }
      }
    });
  }

  /**
   * Returns an arbitrary string value, for example a JMS broker string.
   * 
   * @param entry configuration item name
   * @return the string value supplier
   */
  public AsyncSupplier<String> getStringConfiguration(final String entry) {
    return new AsyncSupplier.Filter<FudgeMsg, String>(getConfiguration(), new Function<FudgeMsg, String>() {
      @Override
      public String apply(final FudgeMsg instance) {
        final String value = instance.getString(entry);
        if (value != null) {
          return value;
        } else {
          s_logger.warn("No string for {}", entry);
          return missingConfiguration(entry);
        }
      }
    });
  }

}
