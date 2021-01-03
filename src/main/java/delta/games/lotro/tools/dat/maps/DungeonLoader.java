package delta.games.lotro.tools.dat.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.io.xml.DungeonXMLWriter;
import delta.games.lotro.maps.data.GeoBox;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for dungeons.
 * @author DAM
 */
public class DungeonLoader
{
  private static final Logger LOGGER=Logger.getLogger(DungeonLoader.class);

  private DataFacade _facade;
  private GeoreferencedBasemapsManager _basemapsManager;
  private Map<Integer,Dungeon> _data;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param basemapsManager Georeferenced basemaps manager.
   */
  public DungeonLoader(DataFacade facade, GeoreferencedBasemapsManager basemapsManager)
  {
    _facade=facade;
    _basemapsManager=basemapsManager;
    _data=new HashMap<Integer,Dungeon>();
  }

  /**
   * Get the loaded dungeons.
   * @return the loaded dungeons.
   */
  public List<Dungeon> getDungeons()
  {
    List<Dungeon> ret=new ArrayList<Dungeon>(_data.values());
    Collections.sort(ret,new IdentifiableComparator<Dungeon>());
    return ret;
  }

  /**
   * Get a dungeon using its identifier.
   * @param dungeonId Dungeon identifier.
   * @return A dungeon (<code>null</code> if an error occurs).
   */
  public Dungeon getDungeon(int dungeonId)
  {
    Integer key=Integer.valueOf(dungeonId);
    Dungeon dungeon=_data.get(key);
    if (dungeon==null)
    {
      dungeon=handleDungeon(dungeonId);
      if (dungeon!=null)
      {
        _data.put(key,dungeon);
      }
    }
    return dungeon;
  }

  private Dungeon handleDungeon(int dungeonId)
  {
    PropertiesSet dungeonProps=_facade.loadProperties(dungeonId+DATConstants.DBPROPERTIES_OFFSET);
    if (dungeonProps==null)
    {
      return null;
    }
    /*
******** Properties: 1879149378
Area_AggroPing_BelowAllowedLevel_Radius: 40.0
Area_AggroPing_MinLevelAllowed: 50
Area_AllowSummoning: 0
Area_Allow_Outfits: 1
Area_PermittedBilling_PropertyName: 268450548 (World_XP1AccessAccountTokens_Legacy)
Area_RequiredCharacteristicDID: 1879141627
Dungeon_Allowed_Mount_Types: 0
Dungeon_MapData: 2013266712
Dungeon_MiniMapData: 2013266712
Dungeon_Music: 268435872 (Ghar_bayur)
Dungeon_Name: 
  #1: Crafting Bunker
Dungeon_ParentDungeon: 0
     */
    //System.out.println(areaProps.dump());
    // Name
    String name=DatUtils.getStringProperty(dungeonProps,"Dungeon_Name");
    if (name==null)
    {
      return null;
    }

    // Image
    int imagePropsId=((Integer)dungeonProps.getProperty("Dungeon_MapData")).intValue();
    PropertiesSet imageProps=_facade.loadProperties(imagePropsId);
    PropertiesSet mapUiProps=(PropertiesSet)imageProps.getProperty("UI_Map_GameMap");
    /*
{UI_Map_GameMap=UI_Map_BlockOffsetX: 254
  UI_Map_BlockOffsetY: 174
  UI_Map_FogOfWar: 0
  UI_Map_FogOfWar_Color: 
    #1: 0
    #2: 0
    #3: 0
    #4: 255
  UI_Map_MapImage: 1091530932
  UI_Map_PixelOffsetX: 372
  UI_Map_PixelOffsetY: 254
  UI_Map_Scale: 3.0000002} 
     */
    int mapId=dungeonId;
    int imageId=((Integer)mapUiProps.getProperty("UI_Map_MapImage")).intValue();
    File basemapImageFile=_basemapsManager.getBasemapImageFile(mapId);
    if (!basemapImageFile.exists())
    {
      DatIconsUtils.buildImageFile(_facade,imageId,basemapImageFile);
    }
    Dungeon dungeon=new Dungeon(dungeonId,name,imageId);
    // Parent
    Integer parentDungeonId=(Integer)dungeonProps.getProperty("Dungeon_ParentDungeon");
    if ((parentDungeonId!=null) && (parentDungeonId.intValue()!=0))
    {
      LOGGER.warn("Dungeon "+dungeonId+" HAS a parent dungeon: "+parentDungeonId+"!");
    }

    float scale=((Float)mapUiProps.getProperty("UI_Map_Scale")).floatValue();
    //System.out.println("\tScale: "+scale);
    GeoPoint origin=MapUtils.getOrigin(name,scale,mapUiProps);
    float geo2pixel=scale*200;
    GeoReference geoReference=new GeoReference(origin,geo2pixel);
    GeoreferencedBasemap basemap=new GeoreferencedBasemap(mapId,name,geoReference);
    // Bounding box
    GeoBox boundingBox=MapUtils.computeBoundingBox(geoReference,basemapImageFile);
    basemap.setBoundingBox(boundingBox);
    // Image ID
    basemap.setImageId(imageId);
    // Register basemap
    _basemapsManager.addBasemap(basemap);
    return dungeon;
  }

  /**
   * Show loaded dungeons on the console.
   */
  public void showDungeons()
  {
    List<Dungeon> dungeons=getDungeons();
    System.out.println("Found "+dungeons.size()+" dungeons");
    for(Dungeon dungeon : dungeons)
    {
      System.out.println(dungeon);
    }
  }

  /**
   * Write dungeons file.
   */
  public void save()
  {
    // Save dungeons
    List<Dungeon> dungeons=new ArrayList<Dungeon>(_data.values());
    Collections.sort(dungeons,new IdentifiableComparator<Dungeon>());
    boolean ok=DungeonXMLWriter.writeDungeonsFile(GeneratedFiles.DUNGEONS,dungeons);
    if (ok)
    {
      System.out.println("Wrote dungeons file: "+GeneratedFiles.DUNGEONS);
    }
  }
}
