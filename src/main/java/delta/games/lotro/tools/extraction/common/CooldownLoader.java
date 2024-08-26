package delta.games.lotro.tools.extraction.common;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.WeenieContentDirectory;

/**
 * Loader for cooldown data.
 * @author DAM
 */
public class CooldownLoader
{
  /**
   * Load cooldown data.
   * @param facade Data access facade.
   * @return A map of duration keys to duration values (seconds).
   */
  public static Map<Integer,Float> doIt(DataFacade facade)
  {
    Map<Integer,Float> ret=new HashMap<Integer,Float>();
    PropertiesSet props=WeenieContentDirectory.loadWeenieContentProps(facade,"CooldownControl");
    Object[] list=(Object[])props.getProperty("CooldownControl_DurationMapList");
    if (list==null)
    {
      return ret;
    }
    for(Object entry : list)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      int type=((Integer)entryProps.getProperty("CooldownControl_DurationType")).intValue();
      float value=((Float)entryProps.getProperty("CooldownControl_DurationValue")).floatValue();
      ret.put(Integer.valueOf(type),Float.valueOf(value));
    }
    return ret;
  }
}
