package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.common.Identifiable;
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

  private static final boolean VERBOSE=false;
  private static final int MAP_NOTES_DID=0x0E000006;

  private DataFacade _facade;
  private EnumMapper _mapLevel;
  private Map<String,IntegerHolder> _typesCount=new HashMap<String,IntegerHolder>();
  private MarkersLoadingUtils _markersUtils;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param markersUtils Markers utils.
   */
  public MapNotesLoader(DataFacade facade, MarkersLoadingUtils markersUtils)
  {
    _facade=facade;
    _mapLevel=facade.getEnumsManager().getEnumMapper(587202774);
    _markersUtils=markersUtils;
  }

  private void loadMapNote(ByteArrayInputStream bis)
  {
    // Position
    DatPosition position=GeoLoader.readPosition(bis);
    // Area ID
    int areaDID=BufferUtils.readUInt32(bis);
    // Dungeon ID
    int dungeonDID=BufferUtils.readUInt32(bis);

    // Associated data ID
    int noteDID=BufferUtils.readUInt32(bis);

    // Properties
    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);

    // Get content layers properties (Object[] with Integers)
    Object[] contentLayersArray=null;
    PropertyValue contentLayersProperties=propsLoader.decodeProperty(bis,false);
    if (contentLayersProperties!=null)
    {
      contentLayersArray=(Object[])contentLayersProperties.getValue();
    }
    // Displayed text
    Object stringinfo=PropertyUtils.readStringInfoProperty(bis);
    int[] key=(int[])stringinfo;
    String[] labelArray=_facade.getStringsManager().resolveStringInfo(key[0],key[1]);
    String text=StringUtils.stringArrayToString(labelArray);
    if (text!=null)
    {
      text=text.replace("\\n","\n");
      text=delta.games.lotro.utils.StringUtils.fixName(text);
    }
    // Icon
    int iconId=BufferUtils.readUInt32(bis);
    // Level
    int levelCode=BufferUtils.readUInt32(bis);
    // Type
    long type=BufferUtils.readLong64(bis);
    // (padding)
    LoaderUtils.readAssert8(bis,0);

    if (VERBOSE)
    {
      System.out.println("****** Map note:");
      _markersUtils.log(position,areaDID,dungeonDID,noteDID,contentLayersArray,text,type);
      if (iconId!=0)
      {
        System.out.println("IconID: "+iconId);
      }
      if (levelCode!=0)
      {
        BitSet levelsSet=BitSetUtils.getBitSetFromFlags(levelCode);
        System.out.println("Levels: "+levelCode+" => "+BitSetUtils.getStringFromBitSet(levelsSet,_mapLevel," / "));
      }
    }

    if (type==1)
    {
      // Link
      DatPosition destPosition=GeoLoader.readPosition(bis);
      // Dest zone: is a Dungeon (most of the time) or an Area
      int destZoneId=BufferUtils.readUInt32(bis);
      Identifiable destArea=_markersUtils.getAreaOrDungeon(destZoneId);
      // Dest dungeon
      int destDungeonId=BufferUtils.readUInt32(bis);
      // Dest note
      int destNoteId=BufferUtils.readUInt32(bis);
      BufferUtils.skip(bis,6); // Always 0

      // Checks
      if (destArea==null)
      {
        LOGGER.warn("Destination area not found: "+destZoneId);
        return;
      }
      if (destDungeonId!=0) // Always 0
      {
        LOGGER.warn("Dest dungeon ID: "+destDungeonId);
        return;
      }
      if (destNoteId!=0) // Always 0
      {
        LOGGER.warn("Dest note ID: "+destNoteId);
        return;
      }
      if (VERBOSE)
      {
        System.out.println("Link! Target position: "+destPosition);
        System.out.println("Dest area: "+destArea);
      }
      _markersUtils.addLink(position,areaDID,dungeonDID,noteDID,destArea,contentLayersArray,text);
    }
    else
    {
      float minRange=BufferUtils.readFloat(bis);
      float maxRange=BufferUtils.readFloat(bis);
      int discoverableMapNoteIndex=BufferUtils.readUInt32(bis);
      PropertiesSet gameSpecificProps=new PropertiesSet();
      propsLoader.decodeProperties(bis,gameSpecificProps);

      if (VERBOSE)
      {
        if ((minRange>0) || (maxRange>0))
        {
          System.out.println("Range: min="+minRange+", max="+maxRange);
        }
        if (discoverableMapNoteIndex!=0)
        {
          System.out.println("discoverableMapNoteIndex="+discoverableMapNoteIndex);
        }
        if (gameSpecificProps.getPropertyNames().size()>0)
        {
          System.out.println("Game specific props: "+gameSpecificProps.dump());
        }
      }
      _markersUtils.buildMarker(position,areaDID,dungeonDID,noteDID,contentLayersArray,text,type);
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
    System.out.println("Number of map notes: "+count);
    for(int i=0;i<count;i++)
    {
      loadMapNote(bis);
    }
    int available=bis.available();
    if (available>0)
    {
      LOGGER.warn("Available bytes: "+available);
    }
    _markersUtils.registerLinks();
  }

  /**
   * Do load map notes.
   */
  public void doIt()
  {
    loadMapNotes();
    if (VERBOSE)
    {
      System.out.println(_typesCount);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapsDataManager mapsDataManager=new MapsDataManager(facade);
    MarkersLoadingUtils markersUtils=new MarkersLoadingUtils(facade,mapsDataManager);
    MapNotesLoader loader=new MapNotesLoader(facade,markersUtils);
    loader.doIt();
    mapsDataManager.write();
  }
}
