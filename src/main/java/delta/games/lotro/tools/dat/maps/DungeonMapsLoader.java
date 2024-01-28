package delta.games.lotro.tools.dat.maps;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.maps.data.GeoBox;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.GeoReference;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemap;
import delta.games.lotro.maps.data.basemaps.GeoreferencedBasemapsManager;

/**
 * Loader for dungeons.
 * @author DAM
 */
public class DungeonMapsLoader
{
  private static final Logger LOGGER=Logger.getLogger(DungeonMapsLoader.class);

  private DataFacade _facade;
  private GeoreferencedBasemapsManager _basemapsManager;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param basemapsManager Georeferenced basemaps manager.
   */
  public DungeonMapsLoader(DataFacade facade, GeoreferencedBasemapsManager basemapsManager)
  {
    _facade=facade;
    _basemapsManager=basemapsManager;
  }

  /**
   * Load dungeon maps.
   */
  public void doIt()
  {
    for(Dungeon dungeon : DungeonsManager.getInstance().getDungeons())
    {
      handleDungeon(dungeon);
    }
  }

  private void handleDungeon(Dungeon dungeon)
  {
    int dungeonId=dungeon.getIdentifier();
    PropertiesSet dungeonProps=_facade.loadProperties(dungeonId+DATConstants.DBPROPERTIES_OFFSET);
    if (dungeonProps==null)
    {
      LOGGER.warn("Cannot find dungeon properties: ID="+dungeonId);
      return;
    }
    String name=dungeon.getName();
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
    // Image
    int imagePropsId=((Integer)dungeonProps.getProperty("Dungeon_MapData")).intValue();
    PropertiesSet imageProps=_facade.loadProperties(imagePropsId);
    PropertiesSet mapUiProps=(PropertiesSet)imageProps.getProperty("UI_Map_GameMap");
    /*
  UI_Map_GameMap=UI_Map_BlockOffsetX: 254
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
  UI_Map_Scale: 3.0000002
     */
    int mapId=dungeonId;
    int basemapId=dungeon.getBasemapId();
    File basemapImageFile=_basemapsManager.getBasemapImageFile(mapId);
    if (!basemapImageFile.exists())
    {
      DatIconsUtils.buildImageFile(_facade,basemapId,basemapImageFile);
    }
    float scale=((Float)mapUiProps.getProperty("UI_Map_Scale")).floatValue();
    GeoPoint origin=MapUtils.getOrigin(name,scale,mapUiProps);
    float geo2pixel=scale*200;
    GeoReference geoReference=new GeoReference(origin,geo2pixel);
    GeoreferencedBasemap basemap=new GeoreferencedBasemap(mapId,name,geoReference);
    // Bounding box
    GeoBox boundingBox=MapUtils.computeBoundingBox(geoReference,basemapImageFile);
    basemap.setBoundingBox(boundingBox);
    // Image ID
    basemap.setImageId(basemapId);
    // Register basemap
    _basemapsManager.addBasemap(basemap);
  }
}
