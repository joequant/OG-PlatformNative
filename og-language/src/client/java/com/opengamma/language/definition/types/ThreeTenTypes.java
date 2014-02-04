/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.definition.types;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Global container for {@link JavaTypeInfo} instances corresponding to time library types.
 */
public final class ThreeTenTypes {

  /**
   * Prevents instantiation.
   */
  private ThreeTenTypes() {
  }

  public static final JavaTypeInfo<Instant> INSTANT = JavaTypeInfo.builder(Instant.class).get();

  public static final JavaTypeInfo<Instant> INSTANT_ALLOW_NULL = JavaTypeInfo.builder(Instant.class).allowNull().get();

  public static final JavaTypeInfo<LocalDate> LOCAL_DATE = JavaTypeInfo.builder(LocalDate.class).get();

  public static final JavaTypeInfo<LocalDate> LOCAL_DATE_ALLOW_NULL = JavaTypeInfo.builder(LocalDate.class).allowNull().get();

  public static final JavaTypeInfo<LocalDateTime> LOCAL_DATE_TIME = JavaTypeInfo.builder(LocalDateTime.class).get();

  public static final JavaTypeInfo<LocalDateTime> LOCAL_DATE_TIME_ALLOW_NULL = JavaTypeInfo.builder(LocalDateTime.class).allowNull().get();

  public static final JavaTypeInfo<OffsetTime> OFFSET_TIME = JavaTypeInfo.builder(OffsetTime.class).get();

  public static final JavaTypeInfo<OffsetTime> OFFSET_TIME_ALLOW_NULL = JavaTypeInfo.builder(OffsetTime.class).allowNull().get();

  public static final JavaTypeInfo<ZonedDateTime> ZONED_DATE_TIME = JavaTypeInfo.builder(ZonedDateTime.class).get();

  public static final JavaTypeInfo<ZonedDateTime> ZONED_DATE_TIME_ALLOW_NULL = JavaTypeInfo.builder(ZonedDateTime.class).allowNull().get();

  public static final JavaTypeInfo<ZoneId> ZONE_ID_ALLOW_NULL = JavaTypeInfo.builder(ZoneId.class).allowNull().get();

}
