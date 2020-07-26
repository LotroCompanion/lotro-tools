package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;

/**
 * Loader for quest map notes.
 * @author DAM
 */
public class QuestMapNotesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MapNotesLoader.class);

  private static final int QUEST_MAP_NOTES_DID=0x0E400000;

  private DataFacade _facade;
  private DungeonLoader _dungeonLoader;
  private GeoAreasLoader _geoAreasLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public QuestMapNotesLoader(DataFacade facade)
  {
    _facade=facade;
    _dungeonLoader=new DungeonLoader(facade);
    _geoAreasLoader=new GeoAreasLoader(facade);
  }

  private void loadQuestMapNote(ByteArrayInputStream bis)
  {
    System.out.println("****** Quest map note:");
    DatPosition position=GeoLoader.readPosition(bis);
    System.out.println("Position: "+position);
    // Area ID
    int areaDID=BufferUtils.readUInt32(bis);
    if (areaDID!=0)
    {
      Identifiable where=getAreaOrDungeon(areaDID); // Area or dungeon
      if (where==null)
      {
        LOGGER.warn("Area/dungeon not found: "+areaDID);
      }
      System.out.println("AreaID="+areaDID+" => "+where);
    }
    // Dungeon ID
    int dungeonDID=BufferUtils.readUInt32(bis);
    if (dungeonDID!=0)
    {
      Dungeon dungeon=_dungeonLoader.getDungeon(dungeonDID);
      if (dungeon==null)
      {
        LOGGER.warn("Dungeon not found: "+dungeonDID);
      }
      //PropertiesSet dungeonProps=_facade.loadProperties(dungeonWStateID+DATConstants.DBPROPERTIES_OFFSET);
      System.out.println("DungeonID="+dungeonDID+" => "+dungeon.getName());
    }

    // Various depending on what the MapNote represents
    int noteDID=BufferUtils.readUInt32(bis);
    if (noteDID!=0)
    {
      DataIdentification dataId=DataIdentificationTools.identify(_facade,noteDID);
      System.out.println("Note: "+dataId);
    }
    else
    {
      LOGGER.warn("No note DID for a quest map note!");
    }

    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);

    // Get content layers properties (Object[] with Integers)
    PropertyValue contentLayersProperties=propsLoader.decodeProperty(bis,false);
    if (contentLayersProperties!=null)
    {
      Object[] contentLayersArray=(Object[])contentLayersProperties.getValue();
      if (contentLayersArray.length>0)
      {
        System.out.println("Content layers properties: "+contentLayersProperties);
      }
    }
    PropertiesSet questDispenserInfo=new PropertiesSet();
    propsLoader.decodeProperties(bis,questDispenserInfo);
    if (questDispenserInfo.getPropertyNames().size()>0)
    {
      System.out.println("Quest dispenser info: "+questDispenserInfo.dump());
    }
  }

  private Identifiable getAreaOrDungeon(int id)
  {
    Dungeon dungeon=_dungeonLoader.getDungeon(id);
    if (dungeon!=null)
    {
      return dungeon;
    }
    Area area=_geoAreasLoader.getArea(id);
    if (area!=null)
    {
      return area;
    }
    LOGGER.warn("Unidentified geo entity: "+id);
    return null;
  }

  /**
   * Load quest map notes.
   */
  private void loadQuestMapNotes()
  {
    byte[] data=_facade.loadData(QUEST_MAP_NOTES_DID);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=QUEST_MAP_NOTES_DID)
    {
      throw new IllegalArgumentException("Expected DID for quest map notes: "+QUEST_MAP_NOTES_DID);
    }
    int count=BufferUtils.readUInt32(bis);
    System.out.println("Number of quest map notes: "+count);
    for(int i=0;i<count;i++)
    {
      loadQuestMapNote(bis);
    }
  }

  private void doIt()
  {
    loadQuestMapNotes();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    QuestMapNotesLoader loader=new QuestMapNotesLoader(facade);
    loader.doIt();
  }
}
