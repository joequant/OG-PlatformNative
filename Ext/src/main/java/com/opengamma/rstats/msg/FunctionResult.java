// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.rstats.msg;
public class FunctionResult extends com.opengamma.language.function.Result implements java.io.Serializable {
  private static final long serialVersionUID = 24349519l;
  private java.util.List<com.opengamma.rstats.msg.DataInfo> _info;
  public static final String INFO_KEY = "info";
  public FunctionResult (java.util.Collection<? extends com.opengamma.language.Data> result) {
    super (result);
  }
  protected FunctionResult (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (INFO_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.rstats.msg.DataInfo> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.rstats.msg.DataInfo> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.rstats.msg.DataInfo fudge3;
          fudge3 = com.opengamma.rstats.msg.DataInfo.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2));
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a FunctionResult - field 'info' is not DataInfo message", e);
        }
      }
      setInfo (fudge1);
    }
  }
  public FunctionResult (java.util.Collection<? extends com.opengamma.language.Data> result, java.util.Collection<? extends com.opengamma.rstats.msg.DataInfo> info) {
    super (result);
    if (info == null) _info = null;
    else {
      final java.util.List<com.opengamma.rstats.msg.DataInfo> fudge0 = new java.util.ArrayList<com.opengamma.rstats.msg.DataInfo> (info);
      for (java.util.ListIterator<com.opengamma.rstats.msg.DataInfo> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.rstats.msg.DataInfo fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'info' cannot be null");
        fudge1.set ((com.opengamma.rstats.msg.DataInfo)fudge2.clone ());
      }
      _info = fudge0;
    }
  }
  protected FunctionResult (final FunctionResult source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._info == null) _info = null;
    else {
      final java.util.List<com.opengamma.rstats.msg.DataInfo> fudge0 = new java.util.ArrayList<com.opengamma.rstats.msg.DataInfo> (source._info);
      for (java.util.ListIterator<com.opengamma.rstats.msg.DataInfo> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.rstats.msg.DataInfo fudge2 = fudge1.next ();
        fudge1.set ((com.opengamma.rstats.msg.DataInfo)fudge2.clone ());
      }
      _info = fudge0;
    }
  }
  public FunctionResult clone () {
    return new FunctionResult (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_info != null)  {
      for (com.opengamma.rstats.msg.DataInfo fudge1 : _info) {
        final org.fudgemsg.MutableFudgeMsg fudge2 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), fudge1.getClass (), com.opengamma.rstats.msg.DataInfo.class);
        fudge1.toFudgeMsg (serializer, fudge2);
        msg.add (INFO_KEY, null, fudge2);
      }
    }
  }
  public static FunctionResult fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.rstats.msg.FunctionResult".equals (className)) break;
      try {
        return (com.opengamma.rstats.msg.FunctionResult)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FunctionResult (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.rstats.msg.DataInfo> getInfo () {
    if (_info != null) {
      return java.util.Collections.unmodifiableList (_info);
    }
    else return null;
  }
  public void setInfo (com.opengamma.rstats.msg.DataInfo info) {
    if (info == null) _info = null;
    else {
      _info = new java.util.ArrayList<com.opengamma.rstats.msg.DataInfo> (1);
      addInfo (info);
    }
  }
  public void setInfo (java.util.Collection<? extends com.opengamma.rstats.msg.DataInfo> info) {
    if (info == null) _info = null;
    else {
      final java.util.List<com.opengamma.rstats.msg.DataInfo> fudge0 = new java.util.ArrayList<com.opengamma.rstats.msg.DataInfo> (info);
      for (java.util.ListIterator<com.opengamma.rstats.msg.DataInfo> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.rstats.msg.DataInfo fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'info' cannot be null");
        fudge1.set ((com.opengamma.rstats.msg.DataInfo)fudge2.clone ());
      }
      _info = fudge0;
    }
  }
  public void addInfo (com.opengamma.rstats.msg.DataInfo info) {
    if (info == null) throw new NullPointerException ("'info' cannot be null");
    if (_info == null) _info = new java.util.ArrayList<com.opengamma.rstats.msg.DataInfo> ();
    _info.add ((com.opengamma.rstats.msg.DataInfo)info.clone ());
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FunctionResult)) return false;
    FunctionResult msg = (FunctionResult)o;
    if (_info != null) {
      if (msg._info != null) {
        if (!_info.equals (msg._info)) return false;
      }
      else return false;
    }
    else if (msg._info != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_info != null) hc += _info.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
