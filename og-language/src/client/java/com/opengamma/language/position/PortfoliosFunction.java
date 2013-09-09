/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.position;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;

import com.opengamma.language.client.CombiningMaster;
import com.opengamma.language.client.MasterID;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.CombinedPortfolioMaster;

/**
 * Returns a list of matching portfolios, searched by name.
 */
public class PortfoliosFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final PortfoliosFunction INSTANCE = new PortfoliosFunction();

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    final MetaParameter name = new MetaParameter("name", JavaTypeInfo.builder(String.class).allowNull().get());
    return Arrays.asList(name);
  }

  private PortfoliosFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.POSITION, "Portfolios", getParameters(), this));
  }

  protected PortfoliosFunction() {
    this(new DefinitionAnnotater(PortfoliosFunction.class));
  }

  // TODO: Returning a matrix like this is bad; what is the fundamental data structure represented? Return that and
  // use a type converter to reduce it to a matrix.

  public static Object[][] invoke(final SessionContext context, final String name) {
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(name);
    request.setDepth(0);
    request.setVisibility(DocumentVisibility.HIDDEN);
    final List<Object[]> rows = new LinkedList<Object[]>();
    
    final IdentityHashMap<PortfolioMaster, String> masterMap = buildMasterMap(context);
    
    CombiningMaster.PORTFOLIO.get(context).search(request, new CombinedPortfolioMaster.SearchCallback() {

      @Override
      public boolean include(PortfolioDocument document) {
        final String name = document.getPortfolio().getName();
        return name.charAt(0) != '.';
      }

      @Override
      public void accept(PortfolioDocument document, PortfolioMaster master, boolean masterUnique, boolean clientUnique) {
        final ManageablePortfolio portfolio = document.getPortfolio();
        final Object[] result = new Object[4];
        result[0] = portfolio.getUniqueId();
        String name = portfolio.getName();
        if (!masterUnique) {
          name = name + " " + portfolio.getUniqueId().toString();
        }
        if (!clientUnique) {
          String label = masterMap.get(master);
          if (label == null) {
            label = "UNKNOWN";
          }
          name = name + " (" + label + ")";
        }
        result[1] = name;
        result[2] = portfolio.getRootNode().getUniqueId();
        result[3] = portfolio.getRootNode().getName();
        rows.add(result);
      }

      @Override
      public int compare(PortfolioDocument o1, PortfolioDocument o2) {
        return o1.getPortfolio().getName().compareToIgnoreCase(o2.getPortfolio().getName());
      }

    });
    return rows.toArray(new Object[rows.size()][]);
  }

  private static IdentityHashMap<PortfolioMaster, String> buildMasterMap(final SessionContext context) {
    PortfolioMaster sessionMaster = context.getClient().getPortfolioMaster();
    PortfolioMaster userMaster = context.getUserContext().getClient().getPortfolioMaster();
    PortfolioMaster globalMaster = context.getGlobalContext().getClient().getPortfolioMaster();
    
    final IdentityHashMap<PortfolioMaster, String> masterMap = new IdentityHashMap<>();
    masterMap.put(sessionMaster, MasterID.SESSION.getLabel());
    masterMap.put(userMaster, MasterID.USER.getLabel());
    masterMap.put(globalMaster, MasterID.GLOBAL.getLabel());
    return masterMap;
  }
  
  // AbstractFunctionInvoker

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) {
    return invoke(sessionContext, (String) parameters[0]);
  }

  // PublishedFunction

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
