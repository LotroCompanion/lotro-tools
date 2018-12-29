package delta.games.lotro.tools.dat.utils;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;

/**
 * Places loader.
 * @author DAM
 */
public class PlaceLoader
{
  //private static final Logger LOGGER=Logger.getLogger(PlaceLoader.class);

  private static Map<Integer,String> _names=new HashMap<Integer,String>();

  /**
   * Load a place.
   * @param facade Data facade.
   * @param placeId Place identifier.
   * @return the loaded place name.
   */
  public static String loadPlace(DataFacade facade, int placeId)
  {
    //TraitDescription ret=null;
    String ret=null;
    PropertiesSet placeProperties=facade.loadProperties(0x9000000+placeId);
    if (placeProperties!=null)
    {
      ret=_names.get(Integer.valueOf(placeId));
      if (ret==null)
      {
        //System.out.println("*********** Place: "+placeId+" ****************");
        //System.out.println(placeProperties.dump());
        // Name
        String areaName=DatUtils.getStringProperty(placeProperties,"Area_Name");
        if (areaName!=null)
        {
          _names.put(Integer.valueOf(placeId),areaName);
        }
        ret=areaName;
        // Region ID
        Integer parentRegionId=(Integer)placeProperties.getProperty("Area_Region");
        if (parentRegionId!=null)
        {
          loadPlace(facade,parentRegionId.intValue());
        }
        //Integer regionId=(Integer)placeProperties.getProperty("Area_RegionID");
        // 1 for Eriador, 2 for Rhovanion
      }
    }
    return ret;
  }

  /**
   * Load a place.
   * @param facade Data facade.
   * @param landmarkId Place identifier.
   * @return the loaded place name.
   */
  public static String loadLandmark(DataFacade facade, int landmarkId)
  {
    //TraitDescription ret=null;
    String ret=null;
    PropertiesSet landmarkProperties=facade.loadProperties(0x9000000+landmarkId);
    if (landmarkProperties!=null)
    {
      ret=_names.get(Integer.valueOf(landmarkId));
      if (ret==null)
      {
        //System.out.println("*********** Place: "+landmarkId+" ****************");
        //System.out.println(landmarkProperties.dump());
        // Name
        ret=DatUtils.getStringProperty(landmarkProperties,"Name");
        if (ret!=null)
        {
          _names.put(Integer.valueOf(landmarkId),ret);
        }
      }
    }
    return ret;
  }
}
