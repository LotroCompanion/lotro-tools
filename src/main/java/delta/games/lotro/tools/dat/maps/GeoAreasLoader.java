package delta.games.lotro.tools.dat.maps;

import java.io.File;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.Region;
import delta.games.lotro.lore.maps.Territory;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for geographic areas.
 * @author DAM
 */
public class GeoAreasLoader
{
  /**
   * Directory for area icons.
   */
  public static final File AREA_ICONS_DIR=new File("data\\maps\\areas\\tmp").getAbsoluteFile();

  private DataFacade _facade;
  private GeoAreasManager _geoMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public GeoAreasLoader(DataFacade facade)
  {
    _facade=facade;
    _geoMgr=new GeoAreasManager();
  }

  /**
   * Get the geo manager.
   * @return the geo manager.
   */
  public GeoAreasManager getGeoManager()
  {
    return _geoMgr;
  }

  /**
   * Get an area using its identifier.
   * @param areaId Area identifier.
   * @return An area (<code>null</code> if an error occurs).
   */
  public Area getArea(int areaId)
  {
    Area area=_geoMgr.getAreaById(areaId);
    if (area==null)
    {
      area=handleArea(areaId);
      if (area!=null)
      {
        _geoMgr.addArea(area);
      }
    }
    return area;
  }

  private Area handleArea(int areaId)
  {
    PropertiesSet areaProps=_facade.loadProperties(areaId+DATConstants.DBPROPERTIES_OFFSET);
    if (areaProps==null)
    {
      return null;
    }
    //System.out.println(areaProps.dump());
    // Name
    String areaName=DatUtils.getStringProperty(areaProps,"Area_Name");
    if (areaName==null)
    {
      return null;
    }
    //System.out.println("\tArea name: "+areaName);
    // Icon
    Integer imageId=(Integer)areaProps.getProperty("Area_Icon");
    if ((imageId!=null) && (imageId.intValue()>0))
    {
      File toDir=new File(AREA_ICONS_DIR,"areaIcons");
      File to=new File(toDir,imageId+".png");
      if (!to.exists())
      {
        DatIconsUtils.buildImageFile(_facade,imageId.intValue(),to);
      }
    }

    // Territory
    Territory territory=null;
    Integer territoryId=(Integer)areaProps.getProperty("Area_Territory");
    if (territoryId!=null)
    {
      territory=getTerritory(territoryId.intValue());
    }

    // Scenes
    // Scenes are not interesting
    /*
    Object[] scenes=(Object[])areaProps.getProperty("Area_SceneArray");
    if (scenes!=null)
    {
      for(Object sceneIdObj : scenes)
      {
        int sceneId=((Integer)sceneIdObj).intValue();
        PropertiesSet sceneProps=_facade.loadProperties(sceneId+DATConstants.DBPROPERTIES_OFFSET);
        System.out.println(sceneProps.dump());
      }
    }
    */
    Area area=new Area(areaId,areaName,territory);
    area.setIconId(imageId);
    return area;
  }

  /**
   * Get a territory using its identifier.
   * @param territoryId Territory identifier.
   * @return A territory (<code>null</code> if an error occurs).
   */
  public Territory getTerritory(int territoryId)
  {
    Territory territory=_geoMgr.getTerritoryById(territoryId);
    if (territory==null)
    {
      territory=handleTerritory(territoryId);
      _geoMgr.addTerritory(territory);
    }
    return territory;
  }

  private Territory handleTerritory(int territoryId)
  {
    PropertiesSet territoryProps=_facade.loadProperties(territoryId+DATConstants.DBPROPERTIES_OFFSET);
    // Name
    String areaName=DatUtils.getStringProperty(territoryProps,"Area_Name");
    //System.out.println("\t\t\tName: "+areaName);
    // Region ID
    int regionId=((Integer)territoryProps.getProperty("Area_Region")).intValue();
    Region region=getRegion(regionId);
    Territory territory=new Territory(territoryId,areaName,region);
    return territory;
  }

  /**
   * Get a region using its identifier.
   * @param regionId Region identifier.
   * @return A region (<code>null</code> if an error occurs).
   */
  public Region getRegion(int regionId)
  {
    Region region=_geoMgr.getRegionById(regionId);
    if (region==null)
    {
      region=handleRegion(regionId);
      _geoMgr.addRegion(region);
    }
    return region;
  }

  private Region handleRegion(int regionId)
  {
    PropertiesSet regionProps=_facade.loadProperties(regionId+DATConstants.DBPROPERTIES_OFFSET);
    // Name
    String areaName=DatUtils.getStringProperty(regionProps,"Area_Name");
    //System.out.println("\t\t\tName: "+areaName);
    // Region code
    int regionCode=((Integer)regionProps.getProperty("Area_RegionID")).intValue();
    //System.out.println("\t\t\tCode: "+regionCode);
    Region region=new Region(regionId,regionCode,areaName);
    return region;
  }

  /**
   * Add missing regions.
   */
  public void addMissingRegions()
  {
    PropertiesSet regionControlProps=_facade.loadProperties(1879146908+DATConstants.DBPROPERTIES_OFFSET); // 0x7001819C
    if (regionControlProps==null)
    {
      return;
    }
    Object[] regionArray=(Object[])regionControlProps.getProperty("Region_Map_List");
    for(Object regionObj : regionArray)
    {
      PropertiesSet regionProps=(PropertiesSet)regionObj;
      int regionCode=((Integer)regionProps.getProperty("Region_ID")).intValue(); 
      Region region=_geoMgr.getRegionByCode(regionCode);
      if (region==null)
      {
        String regionName=(String)regionProps.getProperty("Region_Name");
        Region newRegion=new Region(regionCode,regionCode,regionName);
        _geoMgr.addRegion(newRegion);
      }
    }
  }
}
