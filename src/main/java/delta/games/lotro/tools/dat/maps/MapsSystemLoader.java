package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for the maps system.
 * @author DAM
 */
public class MapsSystemLoader
{
  private static final Logger LOGGER=Logger.getLogger(MapsSystemLoader.class);

  private DataFacade _facade;
  private EnumMapper _uiElementId;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapsSystemLoader(DataFacade facade)
  {
    _facade=facade;
    _uiElementId=facade.getEnumsManager().getEnumMapper(587202769);
  }

  /**
   * Load the properties for maps system.
   * @return the loaded properties, or <code>null</code> if a problem occurred.
   */
  public PropertiesSet loadMapsSystemProperties()
  {
    PropertiesSet ret=null;
    try
    {
      byte[] data=_facade.loadData(0x22000041);
      ByteArrayInputStream bis=new ByteArrayInputStream(data);
      BufferUtils.skip(bis,27499);
      PropertiesSet properties=new PropertiesSet();
      new DBPropertiesLoader(_facade).decodeProperties(bis,properties);
      ret=properties;
    }
    catch(Exception e)
    {
      LOGGER.error("Could not load maps system properties!", e);
      ret=null;
    }
    return ret;
  }

  private void handleArea(int id)
  {
    PropertiesSet areaProps=_facade.loadProperties(id+0x9000000);
    //System.out.println(areaProps.dump());
    // ID
    System.out.println("\tArea ID: "+id);
    // Name
    String areaName=DatUtils.getStringProperty(areaProps,"Area_Name");
    System.out.println("\tArea name: "+areaName);
    // Icon
    Integer imageId=(Integer)areaProps.getProperty("Area_Icon");
    if ((imageId!=null) && (imageId.intValue()>0))
    {
      File to=new File(imageId+".png");
      DatIconsUtils.buildImageFile(_facade,imageId.intValue(),to);
    }

    // Territory
    Integer territoryId=(Integer)areaProps.getProperty("Area_Territory");
    if (territoryId!=null)
    {
      handleTerritory(territoryId.intValue());
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
        PropertiesSet sceneProps=_facade.loadProperties(sceneId+0x9000000);
        System.out.println(sceneProps.dump());
      }
    }
    */
  }

  private void handleTerritory(int territoryId)
  {
    PropertiesSet territoryProps=_facade.loadProperties(territoryId+0x9000000);
    // ID
    System.out.println("\t\tTerritory ID: "+territoryId);
    // Name
    String areaName=DatUtils.getStringProperty(territoryProps,"Area_Name");
    System.out.println("\t\t\tName: "+areaName);
    // Region ID
    int regionId=((Integer)territoryProps.getProperty("Area_Region")).intValue();
    handleRegion(regionId);
  }

  private void handleRegion(int regionId)
  {
    PropertiesSet regionProps=_facade.loadProperties(regionId+0x9000000);
    // ID
    System.out.println("\t\tRegion ID: "+regionId);
    // Name
    String areaName=DatUtils.getStringProperty(regionProps,"Area_Name");
    System.out.println("\t\t\tName: "+areaName);
    // Region code
    Integer regionCode=(Integer)regionProps.getProperty("Area_RegionID");
    System.out.println("\t\t\tCode: "+regionCode);
  }

  private void handleMapProps(PropertiesSet props)
  {
    // Map name
    String mapName=DatUtils.getStringProperty(props,"UI_Map_MenuName");
    System.out.println("Map name: "+mapName);
    // Map image
    Integer imageId=(Integer)props.getProperty("UI_Map_MapImage");
    if (imageId!=null)
    {
      File to=new File(mapName+".png");
      if (!to.exists())
      {
        DatIconsUtils.buildImageFile(_facade,imageId.intValue(),to);
      }
    }

    // ActiveElement
    int activeElementId=((Integer)props.getProperty("UI_Map_ActiveElement")).intValue();
    String activeElementName=_uiElementId.getString(activeElementId);
    System.out.println("\tActive element: "+activeElementName+" ("+activeElementId+")");
    // Region ID
    Integer regionId=(Integer)props.getProperty("UI_Map_RegionID");
    System.out.println("\tRegion: "+regionId);
    // Block
    Integer blockX=(Integer)props.getProperty("UI_Map_BlockOffsetX");
    Integer blockY=(Integer)props.getProperty("UI_Map_BlockOffsetY");
    if ((blockX!=null) && (blockY!=null))
    {
      System.out.println("\tBlock X/Y: "+blockX+"/"+blockY);
      float[] pos=PositionDecoder.decodePosition(blockX.intValue(),blockY.intValue(),0,0);
      float longitude=pos[0];
      float latitude=pos[1];
      System.out.println("Lat: "+latitude+", Lon: "+longitude);
    }
    // Pixel
    Integer pixelOffsetX=(Integer)props.getProperty("UI_Map_PixelOffsetX");
    Integer pixelOffsetY=(Integer)props.getProperty("UI_Map_PixelOffsetY");
    if ((pixelOffsetX!=null) && (pixelOffsetY!=null))
    {
      // Pixel offset from the top/left of the map
      // Matches the position blockX/blockY/ox=0/oy=0
      // OK for Bree, region 1
      // OK for East Rohan: the wold, region 2
      // OK for South Ithilien, region 3 (Y pixel offset is out of map!)
      // OK for Argarnaith, region 4 (for Lhingris, both pixel offsets are out of map...)
      System.out.println("\tPixel offset X/Y: "+pixelOffsetX+"/"+pixelOffsetY);
    }
    // Scale
    float scale=((Float)props.getProperty("UI_Map_Scale")).floatValue();
    System.out.println("\tScale: "+scale);
    // Scale decrease with high level maps:
    // - Mordor/Agarnaith: 0.2375
    // - Mordor: 0.03644
    // - Middle-earth: 0.0173184

    // Areas
    Object[] areas=(Object[])props.getProperty("UI_Map_AreaDIDs_Array");
    if (areas!=null)
    {
      for(Object areaIdObj : areas)
      {
        int areaId=((Integer)areaIdObj).intValue();
        handleArea(areaId);
      }
    }
    // Sub maps
    Object[] subMaps=(Object[])props.getProperty("UI_Map_AreaData_Array");
    if (subMaps!=null)
    {
      for(Object subMapObj : subMaps)
      {
        PropertiesSet subMapProps=(PropertiesSet)subMapObj;
        handleMapProps(subMapProps);
      }
    }
  }

  private void doIt()
  {
    PropertiesSet props=loadMapsSystemProperties();
    if (props!=null)
    {
      PropertiesSet areaProps=(PropertiesSet)props.getProperty("UI_Map_AreaData");
      handleMapProps(areaProps);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapsSystemLoader loader=new MapsSystemLoader(facade);
    loader.doIt();
  }
}
