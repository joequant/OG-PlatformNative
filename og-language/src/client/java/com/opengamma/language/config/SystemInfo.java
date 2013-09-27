// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.config;

import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.connector.UserMessagePayloadVisitor;

public class SystemInfo extends com.opengamma.language.connector.UserMessagePayload implements java.io.Serializable {
  public <T1,T2> T1 accept (final UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitSystemInfo (this, data); }
  private static final long serialVersionUID = -7473512778568440080l;
  private java.util.List<Integer> _get;
  public static final int GET_ORDINAL = 1;
  private java.util.List<Integer> _set;
  public static final int SET_ORDINAL = 2;
  private String _lsid;
  public static final int LSID_ORDINAL = 3;
  private String _configurationURL;
  public static final int CONFIGURATION_URL_ORDINAL = 4;
  private String _serverDescription;
  public static final int SERVER_DESCRIPTION_ORDINAL = 5;
  private org.fudgemsg.FudgeMsg _publishedConfiguration;
  public static final int PUBLISHED_CONFIGURATION_ORDINAL = 6;
  public SystemInfo () {
  }
  protected SystemInfo (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    org.fudgemsg.FudgeField fudgeField;
    fudgeFields = fudgeMsg.getAllByOrdinal (GET_ORDINAL);
    if (fudgeFields.size () > 0)  {
      final java.util.List<Integer> fudge1;
      fudge1 = new java.util.ArrayList<Integer> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudgeMsg.getFieldValue (Integer.class, fudge2));
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a SystemInfo - field 'get' is not integer", e);
        }
      }
      setGet (fudge1);
    }
    fudgeFields = fudgeMsg.getAllByOrdinal (SET_ORDINAL);
    if (fudgeFields.size () > 0)  {
      final java.util.List<Integer> fudge1;
      fudge1 = new java.util.ArrayList<Integer> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudgeMsg.getFieldValue (Integer.class, fudge2));
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a SystemInfo - field 'set' is not integer", e);
        }
      }
      setSet (fudge1);
    }
    fudgeField = fudgeMsg.getByOrdinal (LSID_ORDINAL);
    if (fudgeField != null)  {
      try {
        setLsid ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SystemInfo - field 'lsid' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (CONFIGURATION_URL_ORDINAL);
    if (fudgeField != null)  {
      try {
        setConfigurationURL ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SystemInfo - field 'configurationURL' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (SERVER_DESCRIPTION_ORDINAL);
    if (fudgeField != null)  {
      try {
        setServerDescription ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SystemInfo - field 'serverDescription' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (PUBLISHED_CONFIGURATION_ORDINAL);
    if (fudgeField != null)  {
      try {
        final org.fudgemsg.FudgeMsg fudge1;
        fudge1 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField);
        setPublishedConfiguration (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SystemInfo - field 'publishedConfiguration' is not anonymous/unknown message", e);
      }
    }
  }
  public SystemInfo (java.util.Collection<? extends Integer> get, java.util.Collection<? extends Integer> set, String lsid, String configurationURL, String serverDescription, org.fudgemsg.FudgeMsg publishedConfiguration) {
    if (get == null) _get = null;
    else {
      final java.util.List<Integer> fudge0 = new java.util.ArrayList<Integer> (get);
      for (java.util.ListIterator<Integer> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Integer fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'get' cannot be null");
      }
      _get = fudge0;
    }
    if (set == null) _set = null;
    else {
      final java.util.List<Integer> fudge0 = new java.util.ArrayList<Integer> (set);
      for (java.util.ListIterator<Integer> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Integer fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'set' cannot be null");
      }
      _set = fudge0;
    }
    _lsid = lsid;
    _configurationURL = configurationURL;
    _serverDescription = serverDescription;
    _publishedConfiguration = publishedConfiguration;
  }
  protected SystemInfo (final SystemInfo source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._get == null) _get = null;
    else {
      _get = new java.util.ArrayList<Integer> (source._get);
    }
    if (source._set == null) _set = null;
    else {
      _set = new java.util.ArrayList<Integer> (source._set);
    }
    _lsid = source._lsid;
    _configurationURL = source._configurationURL;
    _serverDescription = source._serverDescription;
    _publishedConfiguration = source._publishedConfiguration;
  }
  public SystemInfo clone () {
    return new SystemInfo (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_get != null)  {
      for (Integer fudge1 : _get) {
        msg.add (null, GET_ORDINAL, fudge1);
      }
    }
    if (_set != null)  {
      for (Integer fudge1 : _set) {
        msg.add (null, SET_ORDINAL, fudge1);
      }
    }
    if (_lsid != null)  {
      msg.add (null, LSID_ORDINAL, _lsid);
    }
    if (_configurationURL != null)  {
      msg.add (null, CONFIGURATION_URL_ORDINAL, _configurationURL);
    }
    if (_serverDescription != null)  {
      msg.add (null, SERVER_DESCRIPTION_ORDINAL, _serverDescription);
    }
    if (_publishedConfiguration != null)  {
      msg.add (null, PUBLISHED_CONFIGURATION_ORDINAL, (_publishedConfiguration instanceof org.fudgemsg.MutableFudgeMsg) ? serializer.newMessage (_publishedConfiguration) : _publishedConfiguration);
    }
  }
  public static SystemInfo fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.SystemInfo".equals (className)) break;
      try {
        return (com.opengamma.language.config.SystemInfo)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SystemInfo (deserializer, fudgeMsg);
  }
  public java.util.List<Integer> getGet () {
    if (_get != null) {
      return java.util.Collections.unmodifiableList (_get);
    }
    else return null;
  }
  public void setGet (Integer get) {
    if (get == null) _get = null;
    else {
      _get = new java.util.ArrayList<Integer> (1);
      addGet (get);
    }
  }
  public void setGet (java.util.Collection<? extends Integer> get) {
    if (get == null) _get = null;
    else {
      final java.util.List<Integer> fudge0 = new java.util.ArrayList<Integer> (get);
      for (java.util.ListIterator<Integer> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Integer fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'get' cannot be null");
      }
      _get = fudge0;
    }
  }
  public void addGet (Integer get) {
    if (get == null) throw new NullPointerException ("'get' cannot be null");
    if (_get == null) _get = new java.util.ArrayList<Integer> ();
    _get.add (get);
  }
  public java.util.List<Integer> getSet () {
    if (_set != null) {
      return java.util.Collections.unmodifiableList (_set);
    }
    else return null;
  }
  public void setSet (Integer set) {
    if (set == null) _set = null;
    else {
      _set = new java.util.ArrayList<Integer> (1);
      addSet (set);
    }
  }
  public void setSet (java.util.Collection<? extends Integer> set) {
    if (set == null) _set = null;
    else {
      final java.util.List<Integer> fudge0 = new java.util.ArrayList<Integer> (set);
      for (java.util.ListIterator<Integer> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Integer fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'set' cannot be null");
      }
      _set = fudge0;
    }
  }
  public void addSet (Integer set) {
    if (set == null) throw new NullPointerException ("'set' cannot be null");
    if (_set == null) _set = new java.util.ArrayList<Integer> ();
    _set.add (set);
  }
  public String getLsid () {
    return _lsid;
  }
  public void setLsid (String lsid) {
    _lsid = lsid;
  }
  public String getConfigurationURL () {
    return _configurationURL;
  }
  public void setConfigurationURL (String configurationURL) {
    _configurationURL = configurationURL;
  }
  public String getServerDescription () {
    return _serverDescription;
  }
  public void setServerDescription (String serverDescription) {
    _serverDescription = serverDescription;
  }
  public org.fudgemsg.FudgeMsg getPublishedConfiguration () {
    return _publishedConfiguration;
  }
  public void setPublishedConfiguration (org.fudgemsg.FudgeMsg publishedConfiguration) {
    _publishedConfiguration = publishedConfiguration;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SystemInfo)) return false;
    SystemInfo msg = (SystemInfo)o;
    if (_get != null) {
      if (msg._get != null) {
        if (!_get.equals (msg._get)) return false;
      }
      else return false;
    }
    else if (msg._get != null) return false;
    if (_set != null) {
      if (msg._set != null) {
        if (!_set.equals (msg._set)) return false;
      }
      else return false;
    }
    else if (msg._set != null) return false;
    if (_lsid != null) {
      if (msg._lsid != null) {
        if (!_lsid.equals (msg._lsid)) return false;
      }
      else return false;
    }
    else if (msg._lsid != null) return false;
    if (_configurationURL != null) {
      if (msg._configurationURL != null) {
        if (!_configurationURL.equals (msg._configurationURL)) return false;
      }
      else return false;
    }
    else if (msg._configurationURL != null) return false;
    if (_serverDescription != null) {
      if (msg._serverDescription != null) {
        if (!_serverDescription.equals (msg._serverDescription)) return false;
      }
      else return false;
    }
    else if (msg._serverDescription != null) return false;
    if (_publishedConfiguration != null) {
      if (msg._publishedConfiguration != null) {
        if (!_publishedConfiguration.equals (msg._publishedConfiguration)) return false;
      }
      else return false;
    }
    else if (msg._publishedConfiguration != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_get != null) hc += _get.hashCode ();
    hc *= 31;
    if (_set != null) hc += _set.hashCode ();
    hc *= 31;
    if (_lsid != null) hc += _lsid.hashCode ();
    hc *= 31;
    if (_configurationURL != null) hc += _configurationURL.hashCode ();
    hc *= 31;
    if (_serverDescription != null) hc += _serverDescription.hashCode ();
    hc *= 31;
    if (_publishedConfiguration != null) hc += _publishedConfiguration.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
