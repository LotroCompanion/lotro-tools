package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.utils.BufferUtils;

/**
 * Loader for quest map notes.
 * @author DAM
 */
public class QuestMapNotesLoader
{
  private static final int QUEST_MAP_NOTES_DID=0x0E400000;

  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public QuestMapNotesLoader(DataFacade facade)
  {
    _facade=facade;
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
      //PropertiesSet areaProps=_facade.loadProperties(areaWStateID+DATConstants.DBPROPERTIES_OFFSET);
      System.out.println("AreaID="+areaDID/*+" => "+areaProps.dump()*/);
    }
    // Dungeon ID
    int dungeonDID=BufferUtils.readUInt32(bis);
    if (dungeonDID!=0)
    {
      //PropertiesSet dungeonProps=_facade.loadProperties(dungeonWStateID+DATConstants.DBPROPERTIES_OFFSET);
      System.out.println("DungeonID="+dungeonDID/*+" => "+dungeonProps.dump()*/);
    }

    // Various depending on what the MapNote represents
    int noteDID=BufferUtils.readUInt32(bis);
    if (noteDID!=0)
    {
      //PropertiesSet noteProps=_facade.loadProperties(noteWState+DATConstants.DBPROPERTIES_OFFSET);
      System.out.println("NoteId="+noteDID/*+" => "+noteProps.dump()*/); // Found a Door...
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
