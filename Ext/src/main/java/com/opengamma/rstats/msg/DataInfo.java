// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.rstats.msg;
public class DataInfo implements java.io.Serializable {
  private static final long serialVersionUID = 51737349101l;
  private String _wrapperClass;
  public static final String WRAPPER_CLASS_KEY = "wrapperClass";
  public DataInfo () {
  }
  protected DataInfo (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (WRAPPER_CLASS_KEY);
    if (fudgeField != null)  {
      try {
        setWrapperClass ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a DataInfo - field 'wrapperClass' is not string", e);
      }
    }
  }
  public DataInfo (String wrapperClass) {
    _wrapperClass = wrapperClass;
  }
  protected DataInfo (final DataInfo source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _wrapperClass = source._wrapperClass;
  }
  public DataInfo clone () {
    return new DataInfo (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_wrapperClass != null)  {
      msg.add (WRAPPER_CLASS_KEY, null, _wrapperClass);
    }
  }
  public static DataInfo fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.rstats.msg.DataInfo".equals (className)) break;
      try {
        return (com.opengamma.rstats.msg.DataInfo)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DataInfo (deserializer, fudgeMsg);
  }
  public String getWrapperClass () {
    return _wrapperClass;
  }
  public void setWrapperClass (String wrapperClass) {
    _wrapperClass = wrapperClass;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof DataInfo)) return false;
    DataInfo msg = (DataInfo)o;
    if (_wrapperClass != null) {
      if (msg._wrapperClass != null) {
        if (!_wrapperClass.equals (msg._wrapperClass)) return false;
      }
      else return false;
    }
    else if (msg._wrapperClass != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_wrapperClass != null) hc += _wrapperClass.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
