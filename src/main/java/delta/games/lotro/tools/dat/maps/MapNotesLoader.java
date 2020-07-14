package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;
import java.util.BitSet;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.loaders.LoaderUtils;
import delta.games.lotro.dat.loaders.PropertyUtils;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.StringUtils;

/**
 * Loader for map notes.
 * @author DAM
 */
public class MapNotesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MapNotesLoader.class);

  private static final int MAP_NOTES_DID=0x0E000006;

  private DataFacade _facade;
  private EnumMapper _mapNoteType;
  private EnumMapper _mapLevel;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapNotesLoader(DataFacade facade)
  {
    _facade=facade;
    _mapNoteType=facade.getEnumsManager().getEnumMapper(587202775);
    _mapLevel=facade.getEnumsManager().getEnumMapper(587202774);
  }

  private void loadMapNote(ByteArrayInputStream bis)
  {
    System.out.println("****** Map note:");
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
    int noteDID=BufferUtils.readUInt32(bis); // Vendor NPC, Item (Invisible Collision Waypoint), Landmark...
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
    // The display text for the MapNote:
    Object stringinfo=PropertyUtils.readStringInfoProperty(bis);
    int[] key=(int[])stringinfo;
    String[] labelArray=_facade.getStringsManager().resolveStringInfo(key[0],key[1]);
    String text=StringUtils.stringArrayToString(labelArray);
    if ((text!=null) && (text.length()>0))
    {
      System.out.println("Text: "+text);
    }
    // Icon
    int iconId=BufferUtils.readUInt32(bis);
    if (iconId!=0)
    {
      System.out.println("IconID: "+iconId);
    }
    // Level
    int levelCode=BufferUtils.readUInt32(bis);
    BitSet levelsSet=BitSetUtils.getBitSetFromFlags(levelCode);
    System.out.println("Levels: "+levelCode+" => "+BitSetUtils.getStringFromBitSet(levelsSet,_mapLevel," / "));
    // Type
    long type=BufferUtils.readLong64(bis);
    // (padding)
    LoaderUtils.readAssert8(bis,0);
    if (type==1)
    {
      // Link
      DatPosition destPosition=GeoLoader.readPosition(bis);
      System.out.println("Link! Target position: "+destPosition);
      // Dest area
      int destAreaId=BufferUtils.readUInt32(bis);
      System.out.println("Dest area ID: "+destAreaId);
      // Dest dungeon
      int destDungeonId=BufferUtils.readUInt32(bis);
      System.out.println("Dest dungeon ID: "+destDungeonId);
      // Dest note
      int destNoteId=BufferUtils.readUInt32(bis);
      System.out.println("Dest note ID: "+destNoteId);
      BufferUtils.skip(bis,6); // Always 0
    }
    else
    {
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(type);
      System.out.println("Type: "+type+" => "+BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType," / "));
      float minRange=BufferUtils.readFloat(bis);
      float maxRange=BufferUtils.readFloat(bis);
      if ((minRange>0) || (maxRange>0))
      {
        System.out.println("Range: min="+minRange+", max="+maxRange);
      }
      int discoverableMapNoteIndex=BufferUtils.readUInt32(bis);
      if (discoverableMapNoteIndex!=0)
      {
        System.out.println("discoverableMapNoteIndex="+discoverableMapNoteIndex);
      }
      PropertiesSet gameSpecificProps=new PropertiesSet();
      propsLoader.decodeProperties(bis,gameSpecificProps);
      if (gameSpecificProps.getPropertyNames().size()>0)
      {
        System.out.println("Game specific props: "+gameSpecificProps.dump());
      }
    }
  }

  /**
   * Load map notes.
   */
  private void loadMapNotes()
  {
    byte[] data=_facade.loadData(MAP_NOTES_DID); // > 1Mb
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=MAP_NOTES_DID)
    {
      throw new IllegalArgumentException("Expected DID for map notes: "+MAP_NOTES_DID);
    }
    int count=BufferUtils.readUInt32(bis); // > 10k
    for(int i=0;i<count;i++)
    {
      loadMapNote(bis);
    }
    int available=bis.available();
    if (available>0)
    {
      LOGGER.warn("Available bytes: "+available);
    }
  }

  private void doIt()
  {
    loadMapNotes();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapNotesLoader loader=new MapNotesLoader(facade);
    loader.doIt();
  }
}
