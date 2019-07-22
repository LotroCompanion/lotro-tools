package delta.games.lotro.tools.dat.maps;

import java.io.ByteArrayInputStream;
import java.util.BitSet;

import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.DBPropertiesLoader;
import delta.games.lotro.dat.loaders.GeoLoader;
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
  private static final int MAP_NOTES_DID=0x0E000006;

  private DataFacade _facade;
  private EnumMapper _mapNoteType;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MapNotesLoader(DataFacade facade)
  {
    _facade=facade;
    _mapNoteType=facade.getEnumsManager().getEnumMapper(587202775);
  }

  private void loadMapNote(ByteArrayInputStream bis)
  {
    DatPosition position=GeoLoader.readPosition(bis);
    System.out.println("Position: "+position+" = "+position.asLatLon());
    // ID: Area or Dungeon
    int areaWStateID=BufferUtils.readUInt32(bis);
    if (areaWStateID!=0)
    {
      //PropertiesSet areaProps=_facade.loadProperties(areaWStateID+0x9000000);
      System.out.println("AreaID="+areaWStateID/*+" => "+areaProps.dump()*/);
    }
    int dungeonWStateID=BufferUtils.readUInt32(bis);
    if (dungeonWStateID!=0)
    {
      //PropertiesSet dungeonProps=_facade.loadProperties(dungeonWStateID+0x9000000);
      System.out.println("DungeonID="+dungeonWStateID/*+" => "+dungeonProps.dump()*/);
    }
    //System.out.println("dungeonWStateID="+dungeonWStateID);

    // Various depending on what the MapNote represents
    int noteWState=BufferUtils.readUInt32(bis);
    if (noteWState!=0)
    {
      //PropertiesSet noteProps=_facade.loadProperties(noteWState+0x9000000);
      System.out.println("NoteId="+noteWState/*+" => "+noteProps.dump()*/); // Found a Door...
    }

    DBPropertiesLoader propsLoader=new DBPropertiesLoader(_facade);

    Object stringinfo=null;
    // Either the PID for Entity_ContentLayers or 0:
    bis.mark(4);
    int peek=BufferUtils.readUInt32(bis);
    bis.reset();
    if (peek!=0)
    {
      // Get content layers properties (Object[] with Integers)
      /*Object content_layers_properties=*/propsLoader.decodeProperty(null,bis,false);
      // The display text for the MapNote:
      stringinfo=PropertyUtils.readStringInfoProperty(bis,8);
      if (stringinfo instanceof String)
      {
        System.out.println((String)stringinfo);
      }
      else if (stringinfo instanceof int[])
      {
        int[] key=(int[])stringinfo;
        String[] labelArray=_facade.getStringsManager().resolveStringInfo(key[0],key[1]);
        String label=StringUtils.stringArrayToString(labelArray);
        System.out.println(label);
      }
      // MapNoteVisFlags 1 2 3 5 - NOTE these might be indices formed into bitfield: Distance, Overlapping, Filter, World
      // This is almost always 0, but can be 41005e72 a few times, which
      // is a horse's head, the stable icon. When that occurs, it's always
      //followed by 00 00 0f
      int iconId=BufferUtils.readUInt32(bis);
      System.out.println("IconID: "+iconId);
      int nextInt=BufferUtils.readUInt16(bis);
      if (nextInt!=0)
      {
        throw new IllegalArgumentException("Expected 0 here. Got "+nextInt);
      }
      // If we have nonzero floats in the larger section, this is always 14 or 15:
      // So few have 2 or 14 that it seems ignorable; just stuff in Rivendell and around Isengard
      //self.nextbyte = ord(ins.read(1))  # seen: 0 1 2 e f
      int nextByte=BufferUtils.readUInt8(bis);
      if (nextByte==0)
      {
        long type=BufferUtils.readLong64(bis);
        // This doesn't match anything in EMAPPER/MapNoteType
        if (type!=0x100)
        {
          throw new IllegalArgumentException("Expected 0x100 here. Got "+type);
        }
      }
      else
      {
        int pad=BufferUtils.readUInt8(bis);
        if (pad!=0)
        {
          throw new IllegalArgumentException("Expected 0 here. Got "+pad);
        }
        long type=BufferUtils.readLong64(bis);
        BitSet typeSet=BitSetUtils.getBitSetFromFlags(type);
        System.out.println("type: "+type+" => "+BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType," / "));
        pad=BufferUtils.readUInt8(bis);
        if (pad!=0)
        {
          throw new IllegalArgumentException("Expected 0 here. Got "+pad);
        }
        // 0 20 40 80 200 300
        // 0 200 240 400 640 800
        //self.min_range, self.max_range = struct.unpack('<2f', ins.read(8))
        float minRange=BufferUtils.readFloat(bis);
        float maxRange=BufferUtils.readFloat(bis);
        if ((minRange!=0.0) || (maxRange!=0.0))
        {
          if ((nextByte!=14) && (nextByte!=15))
          {
            throw new IllegalArgumentException("Expected 14 or 15 here. Got "+nextByte);
          }
        }
        int discoverable_map_note_index=BufferUtils.readUInt32(bis);
        System.out.println("discoverable_map_note_index="+discoverable_map_note_index);
      }
    }
    else
    {
      //self.content_layers_properties = None
      BufferUtils.skip(bis,4);
    }
    /*
    # If present, length = 1, scheme (not confirmed that all have identical):
    # MapNote_GameSpecific (Struct)
    #   WorldEvent_MapNoteStateArray (Array)
    #     WorldEvent_MapNoteState (Struct) -- may be more than 1
    #       MapNote_StringInfo (StringInfo)
    #       MapNote_OffScreenIcon (DataFile)
    #       WorldEvent_AllConditionList (Array) -- may be more than 1
    #         WorldEvent_Condition (Struct)
    #           WorldEvent_Operator (EnumMapper1) : EqualTo
    #           WorldEvent_ConditionValue (Int) : 0
    #           WorldEvent_WorldEvent(DataFile) : (wstate)
    #       MapNote_MouseOverIcon (DataFile)
    #       MapNote_SelectIcon (DataFile)
    #       MapNote_Icon (DataFile)
    */
    PropertiesSet gameSpecificProps=new PropertiesSet();
    propsLoader.decodeProperties(bis,gameSpecificProps);
    //System.out.println(gameSpecificProps.dump());
    if (stringinfo instanceof int[])
    {
      int[] raw=(int[])stringinfo;
      if (raw[0]==0)
      {
        // Child not
        loadMapNote(bis);
      }
    }
  }

  /**
   * Load map notes.
   */
  private void loadMapNotes()
  {
    byte[] data=_facade.loadData(MAP_NOTES_DID);
    ByteArrayInputStream bis=new ByteArrayInputStream(data);
    int did=BufferUtils.readUInt32(bis);
    if (did!=MAP_NOTES_DID)
    {
      throw new IllegalArgumentException("Expected DID for map notes: "+MAP_NOTES_DID);
    }
    int count=BufferUtils.readUInt32(bis);
    for(int i=0;i<count;i++)
    {
      loadMapNote(bis);
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
