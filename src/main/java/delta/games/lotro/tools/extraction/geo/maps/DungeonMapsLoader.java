package delta.games.lotro.tools.extraction.geo.maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for dungeons.
 * @author DAM
 */
public class DungeonMapsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DungeonMapsLoader.class);

  private DataFacade _facade;
  private GeoreferencedBasemapsLoader _basemapsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param basemapsLoader Georeferenced basemaps loader.
   */
  public DungeonMapsLoader(DataFacade facade, GeoreferencedBasemapsLoader basemapsLoader)
  {
    _facade=facade;
    _basemapsLoader=basemapsLoader;
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
      LOGGER.warn("Cannot find dungeon properties: ID={}",Integer.valueOf(dungeonId));
      return;
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
    // Name
    I18nUtils i18n=_basemapsLoader.getI18nUtils();
    String name=i18n.getNameStringProperty(dungeonProps,"Dungeon_Name",dungeonId,0);
    // Image
    int imagePropsId=((Integer)dungeonProps.getProperty("Dungeon_MapData")).intValue();
    PropertiesSet imageProps=_facade.loadProperties(imagePropsId);
    PropertiesSet mapUiProps=(PropertiesSet)imageProps.getProperty("UI_Map_GameMap");
    int imageId=dungeon.getBasemapId();
    _basemapsLoader.handleBasemap(mapUiProps,dungeonId,imageId,name);
  }
}
