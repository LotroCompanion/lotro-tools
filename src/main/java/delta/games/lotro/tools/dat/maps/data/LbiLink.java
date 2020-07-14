package delta.games.lotro.tools.dat.maps.data;

import delta.games.lotro.dat.data.PropertiesSet;

/**
 * @author dm
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
    
  }

  /**
   * @return the iid
   */
  public long getIid()
  {
    return _iid;
  }

  /**
   * @param iid the iid to set
   */
  public void setIid(long iid)
  {
    _iid=iid;
  }

  /**
   * @return the name
   */
  public String getName()
  {
    return _name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name)
  {
    _name=name;
  }

  /**
   * @return the fromIid
   */
  public long getFromIid()
  {
    return _fromIid;
  }

  /**
   * @param fromIid the fromIid to set
   */
  public void setFromIid(long fromIid)
  {
    _fromIid=fromIid;
  }

  /**
   * @return the toIid
   */
  public long getToIid()
  {
    return _toIid;
  }

  /**
   * @param toIid the toIid to set
   */
  public void setToIid(long toIid)
  {
    _toIid=toIid;
  }

  /**
   * @return the type
   */
  public String getType()
  {
    return _type;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type)
  {
    _type=type;
  }

  /**
   * @return the props
   */
  public PropertiesSet getProps()
  {
    return _props;
  }

  /**
   * @param props the props to set
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
      sb.append(" name=").append(_name);
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
