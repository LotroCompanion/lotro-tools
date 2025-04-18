package delta.games.lotro.tools.extraction.geo.dungeons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.io.Console;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.geo.ExtendedPosition;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.loaders.wstate.WStateDataSet;
import delta.games.lotro.dat.wlib.ClassInstance;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.io.xml.DungeonXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.geo.GeoUtils;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;

/**
 * Loader for dungeons.
 * @author DAM
 */
public class DungeonLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(DungeonLoader.class);

  private DataFacade _facade;
  private Map<Integer,Dungeon> _data;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DungeonLoader(DataFacade facade)
  {
    _facade=facade;
    _data=new HashMap<Integer,Dungeon>();
    _i18n=new I18nUtils("dungeons",facade.getGlobalStringsManager());
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
   * Load a dungeon.
   * @param dungeonId Dungeon identifier.
   * @return A dungeon (<code>null</code> if an error occurs).
   */
  public Dungeon loadDungeon(int dungeonId)
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
    // Name
    String name=_i18n.getNameStringProperty(dungeonProps,"Dungeon_Name",dungeonId,0);
    if (name==null)
    {
      return null;
    }

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
    int basemapId=((Integer)mapUiProps.getProperty("UI_Map_MapImage")).intValue();
    Dungeon dungeon=new Dungeon(dungeonId,name,basemapId);
    // Parent
    Integer parentDungeonId=(Integer)dungeonProps.getProperty("Dungeon_ParentDungeon");
    if ((parentDungeonId!=null) && (parentDungeonId.intValue()!=0))
    {
      LOGGER.warn("Dungeon {} HAS a parent dungeon: {}!",Integer.valueOf(dungeonId),parentDungeonId);
    }
    Integer key=Integer.valueOf(dungeonId);
    _data.put(key,dungeon);
    return dungeon;
  }

  private void loadPosition(Dungeon dungeon)
  {
    int dungeonID=dungeon.getIdentifier();
    WStateDataSet wstate=_facade.loadWState(dungeonID);
    ClassInstance dungeonData=(ClassInstance)wstate.getValue(wstate.getOrphanReferences().get(0).intValue());
    DatPosition position=(DatPosition)dungeonData.getAttributeValue("11794990");
    if (position!=null)
    {
      ExtendedPosition extendedPosition=GeoUtils.buildPosition(position);
      dungeon.setMapPosition(extendedPosition);
    }
  }

  /**
   * Show loaded dungeons on the console.
   */
  public void showDungeons()
  {
    List<Dungeon> dungeons=getDungeons();
    Console.println("Found "+dungeons.size()+" dungeons");
    for(Dungeon dungeon : dungeons)
    {
      Console.println(dungeon);
    }
  }

  /**
   * Save dungeons data.
   */
  public void save()
  {
    // Data
    List<Dungeon> dungeons=new ArrayList<Dungeon>(_data.values());
    Collections.sort(dungeons,new IdentifiableComparator<Dungeon>());
    boolean ok=DungeonXMLWriter.writeDungeonsFile(GeneratedFiles.DUNGEONS,dungeons);
    if (ok)
    {
      LOGGER.info("Wrote dungeons file: {}",GeneratedFiles.DUNGEONS);
    }
    // Labels
    _i18n.save();
  }

  /**
   * Load dungeon positions.
   */
  public void loadPositions()
  {
    List<Dungeon> dungeons=getDungeons();
    LOGGER.info("Found {} dungeons",Integer.valueOf(dungeons.size()));
    for(Dungeon dungeon : dungeons)
    {
      // Handle position
      loadPosition(dungeon);
    }
  }
}
