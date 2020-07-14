package delta.games.lotro.tools.dat.maps.data;

import java.util.Set;

import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Weenie.
 * @author DAM
 */
public class Weenie
{
  private long _iid;
  private PropertiesSet _props;
  private Set<Integer> _generatorDids;

  /**
   * Constructor.
   */
  public Weenie()
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

  /**
   * @return the generatorDids
   */
  public Set<Integer> getGeneratorDids()
  {
    return _generatorDids;
  }

  /**
   * @param generatorDids the generatorDids to set
   */
  public void setGeneratorDids(Set<Integer> generatorDids)
  {
    _generatorDids=generatorDids;
  }

  @Override
  public String toString()
  {
    StringBuilder sb=new StringBuilder("Weenie: ");
    if (_iid!=0)
    {
      sb.append("IID=").append(String.format("%08X",Long.valueOf(_iid)));
    }
    if (_generatorDids!=null)
    {
      sb.append(" DIDs=").append(_generatorDids);
    }
    if (_props!=null)
    {
      sb.append(" Properties=").append(_props.dump());
    }
    return sb.toString().trim();
  }
}

