package delta.games.lotro.tools.extraction.geo.areas;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.Region;
import delta.games.lotro.lore.maps.Territory;
import delta.games.lotro.lore.maps.io.xml.GeoAreasXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for geographic areas.
 * @author DAM
 */
public class GeoAreasLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(GeoAreasLoader.class);

  private static final String AREA_NAME="Area_Name";

  private DataFacade _facade;
  private GeoAreasManager _geoMgr;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public GeoAreasLoader(DataFacade facade)
  {
    _facade=facade;
    _geoMgr=new GeoAreasManager();
    _i18n=new I18nUtils("geoAreas",facade.getGlobalStringsManager());
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
    // Name
    String areaName=_i18n.getNameStringProperty(areaProps,AREA_NAME,areaId,0);
    if (areaName==null)
    {
      return null;
    }
    // Icon
    Integer imageId=(Integer)areaProps.getProperty("Area_Icon");
    if ((imageId!=null) && (imageId.intValue()>0))
    {
      File to=new File(GeneratedFiles.AREA_ICONS,imageId+".png");
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

    // Scenes: use Area_SceneArray (not interesting => not used)

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
    String areaName=_i18n.getNameStringProperty(territoryProps,AREA_NAME,territoryId,0);
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
    String areaName=_i18n.getNameStringProperty(regionProps,AREA_NAME,regionId,0);
    // Region code
    int regionCode=((Integer)regionProps.getProperty("Area_RegionID")).intValue();
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
        String regionName=_i18n.getNameStringProperty(regionProps,"Region_Name",regionCode,0);
        Region newRegion=new Region(regionCode,regionCode,regionName);
        _geoMgr.addRegion(newRegion);
      }
    }
  }

  /**
   * Save loader data.
   */
  public void save()
  {
    // Data
    boolean ok=GeoAreasXMLWriter.writeGeoAreasFile(GeneratedFiles.GEO_AREAS,_geoMgr);
    if (ok)
    {
      LOGGER.info("Wrote geographic areas file: {}",GeneratedFiles.GEO_AREAS);
    }
    // Labels
    _i18n.save();
  }
}
