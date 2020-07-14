package delta.games.lotro.tools.dat.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Utility methods to get names for DIDs.
 * @author DAM
 */
public class DidNamesUtils
{
  /**
   * Get a name for a DID.
   * @param facade Facade to use.
   * @param id DID.
   * @return A name.
   */
  private static String getNameForId(DataFacade facade, int id)
  {
    String ret=null;
    PropertiesSet props=facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (props!=null)
    {
      ret=DatStringUtils.getStringProperty(props,"Name");
    }
    if (ret!=null)
    {
      ret=StringUtils.fixName(ret);
    }
    else
    {
      ret="?";
    }
    return ret;
  }

  /**
   * Get a sorted list of names for the given DIDs.
   * @param facade Facade to use.
   * @param ids DIDs to use.
   * @return A list of DID names.
   */
  public static List<String> getNamesForIds(DataFacade facade, Set<Integer> ids)
  {
    List<String> ret=new ArrayList<String>();
    for(Integer id : ids)
    {
      String name=getNameForId(facade,id.intValue());
      ret.add(name);
    }
    Collections.sort(ret);
    return ret;
  }
}
