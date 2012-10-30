// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.rstats.msg;
public class LiveDataResult extends com.opengamma.language.livedata.Result implements java.io.Serializable {
  private static final long serialVersionUID = -671428197l;
  private com.opengamma.rstats.msg.DataInfo _info;
  public static final String INFO_KEY = "info";
  public LiveDataResult () {
  }
  protected LiveDataResult (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (INFO_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.rstats.msg.DataInfo fudge1;
        fudge1 = com.opengamma.rstats.msg.DataInfo.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setInfo (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveDataResult - field 'info' is not DataInfo message", e);
      }
    }
  }
  public LiveDataResult (Integer connection, com.opengamma.language.Data result, com.opengamma.rstats.msg.DataInfo info) {
    super (connection, result);
    if (info == null) _info = null;
    else {
      _info = (com.opengamma.rstats.msg.DataInfo)info.clone ();
    }
  }
  protected LiveDataResult (final LiveDataResult source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._info == null) _info = null;
    else {
      _info = (com.opengamma.rstats.msg.DataInfo)source._info.clone ();
    }
  }
  public LiveDataResult clone () {
    return new LiveDataResult (this);
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
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _info.getClass (), com.opengamma.rstats.msg.DataInfo.class);
      _info.toFudgeMsg (serializer, fudge1);
      msg.add (INFO_KEY, null, fudge1);
    }
  }
  public static LiveDataResult fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.rstats.msg.LiveDataResult".equals (className)) break;
      try {
        return (com.opengamma.rstats.msg.LiveDataResult)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new LiveDataResult (deserializer, fudgeMsg);
  }
  public com.opengamma.rstats.msg.DataInfo getInfo () {
    return _info;
  }
  public void setInfo (com.opengamma.rstats.msg.DataInfo info) {
    if (info == null) _info = null;
    else {
      _info = (com.opengamma.rstats.msg.DataInfo)info.clone ();
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof LiveDataResult)) return false;
    LiveDataResult msg = (LiveDataResult)o;
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
///CLOVER:ON
// CSON: Generated File
