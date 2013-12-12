/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.debug;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import com.opengamma.language.connector.UserMessage;
import com.opengamma.language.connector.UserMessagePayloadHandler;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextFactoryBean;
import com.opengamma.language.test.Test;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.test.TestGroup;

@org.testng.annotations.Test(groups = TestGroup.UNIT)
public class UnhandledMessageHandlerTest {

  /**
   * Example pattern for a message handler. The message should be checked, and if it is of relevance dealt with somehow. Anything else should be passed to the original underlying so that these can be
   * chained by extensions.
   */
  private static class CustomUnhandledMessageHandler extends UnhandledMessageHandlerAdapter<SessionContext> {

    private final SessionContext _context;
    private final UserMessage _message;
    private int _count;

    public CustomUnhandledMessageHandler(final UnhandledMessageHandler<SessionContext> underlying, final UserMessage message, final SessionContext context) {
      super(underlying);
      _message = message;
      _context = context;
    }

    @Override
    public void unhandledMessage(final UserMessage message, final SessionContext data) throws AsynchronousExecution {
      if (_message.equals(message)) {
        assertSame(data, _context);
        _count++;
      } else {
        super.unhandledMessage(message, data);
      }
    }

  }

  public void tesDefaultHandler() throws AsynchronousExecution {
    final UserMessagePayloadHandler handler = new UserMessagePayloadHandler();
    final SessionContextFactoryBean contextFactory = new SessionContextFactoryBean();
    final SessionContext session = contextFactory.createSessionContext("user", false);
    final UserMessage payload = new UserMessage(new Test(Test.Operation.VOID_REQUEST, 0));
    final Unhandled message = new Unhandled(payload);
    assertNull(message.accept(handler, session));
  }

  public void testCustomHandler() throws AsynchronousExecution {
    final UserMessage payload1 = new UserMessage(new Test(Test.Operation.VOID_REQUEST, 1));
    final UserMessage payload2 = new UserMessage(new Test(Test.Operation.VOID_REQUEST, 2));
    final SessionContextFactoryBean contextFactory = new SessionContextFactoryBean();
    final SessionContext session = contextFactory.createSessionContext("user", false);
    final UserMessagePayloadHandler handler = new UserMessagePayloadHandler();
    final CustomUnhandledMessageHandler custom = new CustomUnhandledMessageHandler(handler.getNonDeliverableHandler(), payload1, session);
    handler.setNonDeliverableHandler(custom);
    assertNull((new Unhandled(payload1)).accept(handler, session));
    assertNull((new Unhandled(payload2)).accept(handler, session));
    assertEquals(custom._count, 1);
  }

}
