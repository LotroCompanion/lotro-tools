package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.common.Identifiable;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertiesSet.PropertyValue;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
import delta.games.lotro.dat.loaders.LoaderUtils;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.loaders.PropertyUtils;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.dat.utils.StringUtils;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneLandblockData;
import delta.games.lotro.tools.dat.maps.indexs.ParentZonesLoader;

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
  private EnumMapper _mapNoteType;
  private EnumMapper _mapLevel;
  private DungeonLoader _dungeonLoader;
  private GeoAreasLoader _geoAreasLoader;
  private ParentZoneIndex _parentZonesIndex;
  private MapsDataManager _mapsDataManager;
  private Map<String,IntegerHolder> _typesCount=new HashMap<String,IntegerHolder>();

  /**
   * Constructor.
   * @param facade Data facade.
   * @param mapsDataManager Maps data manager.
   * @param dungeonLoader Loader for dungeons.
   * @param geoAreasLoader Loader for geographic areas.
   */
  public MapNotesLoader(DataFacade facade, MapsDataManager mapsDataManager, DungeonLoader dungeonLoader, GeoAreasLoader geoAreasLoader)
  {
    _facade=facade;
    _mapsDataManager=mapsDataManager;
    _mapNoteType=facade.getEnumsManager().getEnumMapper(587202775);
    _mapLevel=facade.getEnumsManager().getEnumMapper(587202774);
    _dungeonLoader=dungeonLoader;
    _geoAreasLoader=geoAreasLoader;
    ParentZonesLoader parentZoneLoader=new ParentZonesLoader(facade);
    _parentZonesIndex=new ParentZoneIndex(parentZoneLoader);
  }

  private void loadMapNote(ByteArrayInputStream bis)
  {
    DatPosition position=GeoLoader.readPosition(bis);
    Identifiable where=null;
    // Area ID
    int areaDID=BufferUtils.readUInt32(bis);
    Identifiable area=null;
    if (areaDID!=0)
    {
      where=getAreaOrDungeon(areaDID);
      area=where;
    }
    if (area==null)
    {
      LOGGER.warn("Area is null: ID="+areaDID);
    }
    // Dungeon ID
    int dungeonDID=BufferUtils.readUInt32(bis);
    Identifiable dungeon=null;
    if (dungeonDID!=0)
    {
      where=_dungeonLoader.getDungeon(dungeonDID);
      dungeon=where;
    }

    // Associated data ID
    int noteDID=BufferUtils.readUInt32(bis);
    DataIdentification dataId=null;
    if (noteDID!=0)
    {
      // {Milestone=327, Landmark=2459, Waypoint=1308, DoorTemplate=874, IItem=857, Hotspot=267, NPCTemplate=4273}
      dataId=DataIdentificationTools.identify(_facade,noteDID);
      if (VERBOSE)
      {
        IntegerHolder counter=_typesCount.get(dataId.getWClassName());
        if (counter==null)
        {
          counter=new IntegerHolder();
          _typesCount.put(dataId.getWClassName(),counter);
        }
        counter.increment();
      }
    }

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
    text=delta.games.lotro.utils.StringUtils.fixName(text);
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
      System.out.println("Position: "+position);
      System.out.println("Area: "+area);
      if (dungeon!=null)
      {
        System.out.println("Dungeon: "+dungeon);
      }
      System.out.println("Data ID: "+dataId);
      if ((contentLayersArray!=null) && (contentLayersArray.length>0))
      {
        System.out.println("Content layers properties: "+contentLayersProperties);
      }
      if ((text!=null) && (text.length()>0))
      {
        System.out.println("Text: "+text);
      }
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
      // Dest area: is a Dungeon (most of the time) or an Area
      int destAreaId=BufferUtils.readUInt32(bis);
      Identifiable destArea=getAreaOrDungeon(destAreaId);
      if (destArea==null)
      {
        LOGGER.warn("Destination area not found: "+destAreaId);
      }
      // Dest dungeon
      int destDungeonId=BufferUtils.readUInt32(bis);
      if (destDungeonId!=0) // Always 0
      {
        LOGGER.warn("Dest dungeon ID: "+destDungeonId);
      }
      // Dest note
      int destNoteId=BufferUtils.readUInt32(bis);
      if (destNoteId!=0) // Always 0
      {
        LOGGER.warn("Dest note ID: "+destNoteId);
      }
      BufferUtils.skip(bis,6); // Always 0
      if (VERBOSE)
      {
        System.out.println("Link! Target position: "+destPosition);
        System.out.println("Dest area: "+destArea);
      }
    }
    else
    {
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(type);
      float minRange=BufferUtils.readFloat(bis);
      float maxRange=BufferUtils.readFloat(bis);
      int discoverableMapNoteIndex=BufferUtils.readUInt32(bis);
      PropertiesSet gameSpecificProps=new PropertiesSet();
      propsLoader.decodeProperties(bis,gameSpecificProps);

      if (VERBOSE)
      {
        String typeStr=BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType," / ");
        System.out.println("Type: "+type+" => "+typeStr);
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

      // Checks
      int region=position.getRegion();
      if ((region<1) || (region>4))
      {
        LOGGER.warn("Weird region value: "+region);
        return;
      }
      int instance=position.getInstance();
      if (instance!=0)
      {
        LOGGER.warn("Instance is not 0 for position: "+position);
      }
      if (where==null)
      {
        LOGGER.warn("Unidentified geo entity! AreaID="+areaDID+", DungeonID="+dungeonDID);
      }
      int cell=position.getCell();
      ParentZoneLandblockData parentData=_parentZonesIndex.getLandblockData(position.getRegion(),position.getBlockX(),position.getBlockY());
      if (parentData==null)
      {
        LOGGER.warn("No parent data for: "+position);
      }
      if (cell!=0)
      {
        if (!(where instanceof Dungeon))
        {
          //LOGGER.warn("Cell="+cell+" while where="+where+" for position: "+position);
        }
      }
      Integer parentArea=(parentData!=null)?parentData.getParentData(cell):null;
      Integer whereId=(where!=null)?Integer.valueOf(where.getIdentifier()):null;
      if (!Objects.equals(whereId,parentArea))
      {
        LOGGER.warn("Parent mismatch: got="+whereId+", expected="+parentArea);
      }
      // Build marker
      Marker marker=new Marker();
      float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
      GeoPoint geoPoint=new GeoPoint(lonLat[0],lonLat[1]);
      marker.setDid(noteDID);
      marker.setPosition(geoPoint);
      marker.setLabel(text);
      int code=typeSet.nextSetBit(0)+1;
      marker.setCategoryCode(code);
      _mapsDataManager.registerDidMarker(dungeonDID,marker);
      _mapsDataManager.registerWorldMarker(position.getRegion(),marker);
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
    return null;
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
  }

  /**
   * Do load map notes.
   */
  public void doIt()
  {
    loadMapNotes();
    System.out.println(_typesCount);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    MapsDataManager mapsDataManager=new MapsDataManager();
    DungeonLoader dungeonLoader=new DungeonLoader(facade);
    GeoAreasLoader geoAreasLoader=new GeoAreasLoader(facade);
    MapNotesLoader loader=new MapNotesLoader(facade,mapsDataManager,dungeonLoader,geoAreasLoader);
    loader.doIt();
  }
}
