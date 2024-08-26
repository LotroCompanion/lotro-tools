package delta.games.lotro.tools.extraction.geo.landblocks;

import delta.games.lotro.dat.data.PropertiesSet;

/**
 * A link as found in LandBlockInfos.
 * @author DAM
 */
public class LbiLink
{
  private long _iid;
  private String _name;
  private long _fromIid;
  private long _toIid;
  private String _type;
  private PropertiesSet _props;

  /**
   * Constructor.
   */
  public LbiLink()
  {
    // Nothing!
  }

  /**
   * Get the IID.
   * @return a IID.
   */
  public long getIid()
  {
    return _iid;
  }

  /**
   * Set the IID.
   * @param iid the IID to set.
   */
  public void setIid(long iid)
  {
    _iid=iid;
  }

  /**
   * Get the name.
   * @return a name or <code>null</code> if not set.
   */
  public String getName()
  {
    return _name;
  }

  /**
   * Set the name.
   * @param name the name to set.
   */
  public void setName(String name)
  {
    _name=name;
  }

  /**
   * Get the 'from' IID.
   * @return the from IID.
   */
  public long getFromIid()
  {
    return _fromIid;
  }

  /**
   * Set the 'from' IID.
   * @param fromIid the IID to set.
   */
  public void setFromIid(long fromIid)
  {
    _fromIid=fromIid;
  }

  /**
   * Get the 'to' IID.
   * @return the 'to' IID.
   */
  public long getToIid()
  {
    return _toIid;
  }

  /**
   * Set the 'to' IID.
   * @param toIid the IID to set.
   */
  public void setToIid(long toIid)
  {
    _toIid=toIid;
  }

  /**
   * Get the link type.
   * @return a link type.
   */
  public String getType()
  {
    return _type;
  }

  /**
   * Set the link type.
   * @param type the type to set.
   */
  public void setType(String type)
  {
    _type=type;
  }

  /**
   * Get the properties.
   * @return some properties or <code>null</code> if not set.
   */
  public PropertiesSet getProps()
  {
    return _props;
  }

  /**
   * Set the properties.
   * @param props the properties to set.
   */
  public void setProps(PropertiesSet props)
  {
    _props=props;
  }

  @Override
  public String toString()
  {
    StringBuilder sb=new StringBuilder("Link: ");
    if (_iid!=0)
    {
      sb.append("IID=").append(String.format("%08X",Long.valueOf(_iid)));
    }
    if (_name!=null)
    {
      sb.append(" name='").append(_name).append('\'');
    }
    if (_fromIid!=0)
    {
      sb.append(" From=").append(String.format("%08X",Long.valueOf(_fromIid)));
    }
    if (_toIid!=0)
    {
      sb.append(" To=").append(String.format("%08X",Long.valueOf(_toIid)));
    }
    if (_type!=null)
    {
      sb.append (" Type=").append(_type);
    }
    if ((_props!=null) && (_props.getPropertiesCount()>0))
    {
      sb.append(" Properties=").append(_props.dump());
    }
    return sb.toString().trim();
  }
}
