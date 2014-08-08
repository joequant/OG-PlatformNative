/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.definition.types.PrimitiveTypes;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.function.BiFunction;

/**
 * Expands a Fudge representation of a {@link List} of {@link ComputedValue} objects into a 2D structure.
 */
public class ExpandComputedValuesFunction extends AbstractFunctionInvoker implements PublishedFunction {

  private static final Logger s_logger = LoggerFactory.getLogger(ExpandComputedValuesFunction.class);

  /**
   * Default instance.
   */
  public static final ExpandComputedValuesFunction INSTANCE = new ExpandComputedValuesFunction();

  private static final ComputationTargetTypeMap<BiFunction<SessionContext, ComputationTargetSpecification, String>> s_getName = getName();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return ImmutableList.of(new MetaParameter("values", JavaTypeInfo.builder(List.class).parameter(ComputedValue.class).get()), new MetaParameter("includeIdentifier",
        PrimitiveTypes.BOOLEAN_DEFAULT_FALSE), new MetaParameter("includeName", PrimitiveTypes.BOOLEAN_DEFAULT_TRUE), new MetaParameter("includeValue", PrimitiveTypes.BOOLEAN_DEFAULT_TRUE),
        new MetaParameter("includeType", PrimitiveTypes.BOOLEAN_DEFAULT_FALSE));
  }

  private ExpandComputedValuesFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VALUE, "ExpandComputedValues", getParameters(), this));
  }

  public ExpandComputedValuesFunction() {
    this(new DefinitionAnnotater(ExpandComputedValuesFunction.class));
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

  private static ComputationTargetTypeMap<BiFunction<SessionContext, ComputationTargetSpecification, String>> getName() {
    final ComputationTargetTypeMap<BiFunction<SessionContext, ComputationTargetSpecification, String>> map = new ComputationTargetTypeMap<>();
    map.put(ComputationTargetType.PORTFOLIO_NODE, new BiFunction<SessionContext, ComputationTargetSpecification, String>() {
      @Override
      public String apply(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
        final PositionSource positions = sessionContext.getGlobalContext().getPositionSource();
        if (positions != null) {
          try {
            return positions.getPortfolioNode(targetSpec.getUniqueId(), VersionCorrection.LATEST).getName();
          } catch (DataNotFoundException ex) {
            s_logger.warn("Node {} not found in position source", targetSpec);
          }
        }
        return "Node " + targetSpec.getUniqueId();
      }
    });
    map.put(ComputationTargetType.POSITION, new BiFunction<SessionContext, ComputationTargetSpecification, String>() {
      @Override
      public String apply(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
        final PositionSource positions = sessionContext.getGlobalContext().getPositionSource();
        final SecuritySource securities = sessionContext.getGlobalContext().getSecuritySource();
        if ((positions != null) && (securities != null)) {
          try {
            final Position position = positions.getPosition(targetSpec.getUniqueId());
            try {
              final Security security = position.getSecurityLink().resolve(securities);
              return position.getQuantity() + " x " + security.getName();

            } catch (DataNotFoundException ex) {
              s_logger.warn("Security {} not found in security source for position {}", position.getSecurityLink(), targetSpec);
            }
          } catch (DataNotFoundException ex) {
            s_logger.warn("Position {} not found in position source", targetSpec);
          }
        }
        return "Position " + targetSpec.getUniqueId();
      }
    });
    map.put(ComputationTargetType.TRADE, new BiFunction<SessionContext, ComputationTargetSpecification, String>() {
      @Override
      public String apply(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
        final PositionSource positions = sessionContext.getGlobalContext().getPositionSource();
        final SecuritySource securities = sessionContext.getGlobalContext().getSecuritySource();
        if ((positions != null) && (securities != null)) {
          try {
            final Trade trade = positions.getTrade(targetSpec.getUniqueId());
            try {
              final Security security = trade.getSecurityLink().resolve(securities);
              return trade.getQuantity() + " x " + security.getName() + " (" + trade.getTradeDate() + ")";

            } catch (DataNotFoundException ex) {
              s_logger.warn("Security {} not found in security source for trade {}", trade.getSecurityLink(), targetSpec);
            }
          } catch (DataNotFoundException ex) {
            s_logger.warn("Trade {} not found in position source", targetSpec);
          }
        }
        return "Trade " + targetSpec.getUniqueId();
      }
    });
    map.put(ComputationTargetType.SECURITY, new BiFunction<SessionContext, ComputationTargetSpecification, String>() {
      @Override
      public String apply(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
        final SecuritySource securities = sessionContext.getGlobalContext().getSecuritySource();
        if (securities != null) {
          final Security security = securities.get(targetSpec.getUniqueId());
          if (security != null) {
            return security.getName();
          } else {
            s_logger.warn("Security {} not found in security source", targetSpec);
          }
        }
        return "Security " + targetSpec.getUniqueId();
      }
    });
    map.put(ComputationTargetType.NULL, new BiFunction<SessionContext, ComputationTargetSpecification, String>() {
      @Override
      public String apply(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
        return "NULL";
      }
    });
    map.put(ComputationTargetType.ANYTHING, new BiFunction<SessionContext, ComputationTargetSpecification, String>() {
      @Override
      public String apply(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
        return targetSpec.getType().getName() + " " + targetSpec.getUniqueId();
      }
    });
    return map;
  }

  private static String getName(final SessionContext sessionContext, final ComputationTargetSpecification targetSpec) {
    final BiFunction<SessionContext, ComputationTargetSpecification, String> operation = s_getName.get(targetSpec.getType());
    if (operation != null) {
      return operation.apply(sessionContext, targetSpec);
    } else {
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    final List<ComputedValue> values = (List<ComputedValue>) parameters[0];
    final boolean includeIdentifier = (Boolean) parameters[1];
    final boolean includeName = (Boolean) parameters[2];
    final boolean includeValue = (Boolean) parameters[3];
    final boolean includeType = (Boolean) parameters[4];
    int columns = 0;
    if (includeIdentifier) {
      columns++;
    }
    if (includeName) {
      columns++;
    }
    if (includeValue) {
      columns++;
    }
    if (includeType) {
      columns++;
    }
    final Object[][] result = new Object[values.size()][columns];
    int row = 0;
    for (ComputedValue value : values) {
      final Object[] resultRow = result[row++];
      columns = 0;
      if (includeIdentifier) {
        final UniqueId uid = value.getSpecification().getTargetSpecification().getUniqueId();
        resultRow[columns++] = uid;
      }
      if (includeName) {
        resultRow[columns++] = getName(sessionContext, value.getSpecification().getTargetSpecification());
      }
      if (includeValue) {
        resultRow[columns++] = value.getValue();
      }
      if (includeType) {
        resultRow[columns++] = value.getSpecification().getTargetSpecification().getType();
      }
    }
    return result;
  }

}
