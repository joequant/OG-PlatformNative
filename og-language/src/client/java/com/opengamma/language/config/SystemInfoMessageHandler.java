/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.Collection;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.Main;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.SessionContext;

/**
 * Responds to the SystemInfo messages to allow clients to query/update configuration.
 */
public class SystemInfoMessageHandler {

  private static final Logger s_logger = LoggerFactory.getLogger(SystemInfoMessageHandler.class);

  private abstract static class SystemInfoField<T> {

    private final Integer _ordinal;

    public SystemInfoField(final Integer ordinal) {
      _ordinal = ordinal;
    }

    public Integer getOrdinal() {
      return _ordinal;
    }

    public abstract void set(SystemInfo message, T value);

    public abstract T get(SystemInfo message);

    public abstract void update(SessionContext context, T value);

    public abstract T query(SessionContext context);

  }

  private abstract static class ServerMetadataField<T> extends SystemInfoField<T> {

    public ServerMetadataField(final int ordinal) {
      super(ordinal);
    }

    protected abstract void set(ServerMetadata metadata, T value);

    protected abstract T get(ServerMetadata metadata);

    @Override
    public final void update(final SessionContext context, final T value) {
      set(context.getGlobalContext().getServerMetadata(), value);
    }

    @Override
    public final T query(final SessionContext context) {
      return get(context.getGlobalContext().getServerMetadata());
    }

  }

  private static final SystemInfoField<String> CONFIGURATION_URL = new ServerMetadataField<String>(SystemInfo.CONFIGURATION_URL_ORDINAL) {

    @Override
    public void set(final SystemInfo message, final String value) {
      message.setConfigurationURL(value);
    }

    @Override
    public String get(final SystemInfo message) {
      return message.getConfigurationURL();
    }

    @Override
    public void set(final ServerMetadata metadata, final String value) {
      metadata.setConfigurationURL(value);
    }

    @Override
    public String get(final ServerMetadata metadata) {
      return metadata.getConfigurationURL();
    }

  };

  private static final SystemInfoField<String> LSID = new ServerMetadataField<String>(SystemInfo.LSID_ORDINAL) {

    @Override
    public void set(final SystemInfo message, final String value) {
      message.setLsid(value);
    }

    @Override
    public String get(final SystemInfo message) {
      return message.getLsid();
    }

    @Override
    public void set(final ServerMetadata metadata, final String value) {
      metadata.setLogicalServerId(value);
    }

    @Override
    public String get(final ServerMetadata metadata) {
      return metadata.getLogicalServerId();
    }

  };

  private static final SystemInfoField<String> OG_LANGUAGE_VERSION = new SystemInfoField<String>(SystemInfo.OG_LANGUAGE_VERSION_ORDINAL) {

    @Override
    public void set(final SystemInfo message, final String value) {
      message.setOgLanguageVersion(value);
    }

    @Override
    public String get(final SystemInfo message) {
      return message.getOgLanguageVersion();
    }

    @Override
    public void update(final SessionContext context, final String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String query(final SessionContext context) {
      return Main.version();
    }

  };

  private static final SystemInfoField<String> OG_PLATFORM_VERSION = new ServerMetadataField<String>(SystemInfo.OG_PLATFORM_VERSION_ORDINAL) {

    @Override
    public void set(final SystemInfo message, final String value) {
      message.setOgPlatformVersion(value);
    }

    @Override
    public String get(final SystemInfo message) {
      return message.getOgPlatformVersion();
    }

    @Override
    public void set(final ServerMetadata metadata, final String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String get(final ServerMetadata metadata) {
      final String version = Main.version();
      s_logger.error("TODO: PLAT-4766 Server version requested; returning OG-Language version string - {}", version);
      return version;
    }

  };

  private static final SystemInfoField<FudgeMsg> PUBLISHED_CONFIGURATION = new ServerMetadataField<FudgeMsg>(SystemInfo.PUBLISHED_CONFIGURATION_ORDINAL) {

    @Override
    public void set(final SystemInfo message, final FudgeMsg value) {
      message.setPublishedConfiguration(value);
    }

    @Override
    public FudgeMsg get(final SystemInfo message) {
      return message.getPublishedConfiguration();
    }

    @Override
    public void set(final ServerMetadata metadata, final FudgeMsg value) {
      metadata.setPublishedConfiguration(value);
    }

    @Override
    public FudgeMsg get(final ServerMetadata metadata) {
      return metadata.getPublishedConfiguration();
    }

  };

  private static final SystemInfoField<String> SERVER_DESCRIPTION = new ServerMetadataField<String>(SystemInfo.SERVER_DESCRIPTION_ORDINAL) {

    @Override
    public void set(final SystemInfo message, final String value) {
      message.setServerDescription(value);
    }

    @Override
    public String get(final SystemInfo message) {
      return message.getServerDescription();
    }

    @Override
    public void set(final ServerMetadata metadata, final String value) {
      metadata.setServerDescription(value);
    }

    @Override
    public String get(final ServerMetadata metadata) {
      return metadata.getServerDescription();
    }

  };

  private static SystemInfoField<?> getField(final Integer field) {
    switch (field.intValue()) {
      case SystemInfo.CONFIGURATION_URL_ORDINAL:
        return CONFIGURATION_URL;
      case SystemInfo.LSID_ORDINAL:
        return LSID;
      case SystemInfo.OG_LANGUAGE_VERSION_ORDINAL:
        return OG_LANGUAGE_VERSION;
      case SystemInfo.OG_PLATFORM_VERSION_ORDINAL:
        return OG_PLATFORM_VERSION;
      case SystemInfo.PUBLISHED_CONFIGURATION_ORDINAL:
        return PUBLISHED_CONFIGURATION;
      case SystemInfo.SERVER_DESCRIPTION_ORDINAL:
        return SERVER_DESCRIPTION;
      default:
        s_logger.warn("Unrecognised field {}", field);
        return null;
    }
  }

  private static <T> void setSystemInfo(final SystemInfoField<T> field, final SystemInfo message, final SessionContext context) {
    field.update(context, field.get(message));
    field.set(message, null);
  }

  private static <T> void getSystemInfo(final SystemInfoField<T> field, final SystemInfo message, final SessionContext context) {
    field.set(message, field.query(context));
    message.addSet(field.getOrdinal());
  }

  /**
   * Acts upon the update requests, modifying the original message to remove the fields which have been processed.
   * 
   * @param fields the fields to update, not null
   * @param message the message containing the update information, to be updated, not null
   * @param context the client's session context, not null
   */
  private static void setSystemInfo(final Collection<Integer> fields, final SystemInfo message, final SessionContext context) {
    for (Integer field : fields) {
      final SystemInfoField<?> info = getField(field);
      if (info != null) {
        s_logger.debug("Updating field {}", field);
        setSystemInfo(info, message, context);
      } else {
        s_logger.error("Can't update system information for unrecognised field {}", field);
      }
    }
  }

  /**
   * Acts upon the query requests, modifying the original message to include the queried fields.
   * 
   * @param fields the fields to query, not null
   * @param message the message to update, not null
   * @param context the client's session context, not null;
   */
  private static void getSystemInfo(final Collection<Integer> fields, final SystemInfo message, final SessionContext context) {
    for (Integer field : fields) {
      final SystemInfoField<?> info = getField(field);
      if (info != null) {
        s_logger.debug("Querying field {}", field);
        getSystemInfo(info, message, context);
      } else {
        s_logger.error("Can't query system information for unrecognised field {}", field);
      }
    }
  }

  public static UserMessagePayload handle(final SystemInfo message, final SessionContext context) {
    Collection<Integer> fields = message.getSet();
    if (fields != null) {
      message.setSet((Integer) null);
      s_logger.info("Attempting to update {} configuration field(s)", fields.size());
      setSystemInfo(fields, message, context);
    }
    fields = message.getGet();
    if (fields != null) {
      message.setGet((Integer) null);
      s_logger.info("Attempting to fetch {} configuration field(s)", fields.size());
      getSystemInfo(fields, message, context);
    }
    return message;
  }

}
