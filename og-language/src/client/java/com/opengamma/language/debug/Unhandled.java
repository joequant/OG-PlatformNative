// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.debug;
public class Unhandled extends com.opengamma.language.connector.UserMessagePayload implements java.io.Serializable {
          public <T1,T2> T1 accept (final com.opengamma.language.connector.UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitUnhandled (this, data); }
  private static final long serialVersionUID = -59925387455l;
  private com.opengamma.language.connector.UserMessage _unhandled;
  public static final String UNHANDLED_KEY = "unhandled";
  public Unhandled (com.opengamma.language.connector.UserMessage unhandled) {
    if (unhandled == null) throw new NullPointerException ("'unhandled' cannot be null");
    else {
      _unhandled = (com.opengamma.language.connector.UserMessage)unhandled.clone ();
    }
  }
  protected Unhandled (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (UNHANDLED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Unhandled - field 'unhandled' is not present");
    try {
      _unhandled = com.opengamma.language.connector.UserMessage.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Unhandled - field 'unhandled' is not UserMessage message", e);
    }
  }
  protected Unhandled (final Unhandled source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._unhandled == null) _unhandled = null;
    else {
      _unhandled = (com.opengamma.language.connector.UserMessage)source._unhandled.clone ();
    }
  }
  public Unhandled clone () {
    return new Unhandled (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_unhandled != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _unhandled.getClass (), com.opengamma.language.connector.UserMessage.class);
      _unhandled.toFudgeMsg (serializer, fudge1);
      msg.add (UNHANDLED_KEY, null, fudge1);
    }
  }
  public static Unhandled fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.debug.Unhandled".equals (className)) break;
      try {
        return (com.opengamma.language.debug.Unhandled)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Unhandled (deserializer, fudgeMsg);
  }
  public com.opengamma.language.connector.UserMessage getUnhandled () {
    return _unhandled;
  }
  public void setUnhandled (com.opengamma.language.connector.UserMessage unhandled) {
    if (unhandled == null) throw new NullPointerException ("'unhandled' cannot be null");
    else {
      _unhandled = (com.opengamma.language.connector.UserMessage)unhandled.clone ();
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Unhandled)) return false;
    Unhandled msg = (Unhandled)o;
    if (_unhandled != null) {
      if (msg._unhandled != null) {
        if (!_unhandled.equals (msg._unhandled)) return false;
      }
      else return false;
    }
    else if (msg._unhandled != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_unhandled != null) hc += _unhandled.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
