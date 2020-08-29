package delta.games.lotro.tools.dat.maps;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.common.Identifiable;
import delta.games.lotro.dat.data.DatPosition;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.DataIdentification;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneLandblockData;
import delta.games.lotro.tools.dat.maps.indexs.ParentZonesLoader;

/**
 * Marker loading utilities.
 * @author DAM
 */
public class MarkersLoadingUtils
{
  private static final Logger LOGGER=Logger.getLogger(MarkersLoadingUtils.class);

  private DataFacade _facade;
  private EnumMapper _mapNoteType;
  private DungeonLoader _dungeonLoader;
  private GeoAreasLoader _geoAreasLoader;
  private MapsDataManager _mapsDataManager;
  private ParentZoneIndex _parentZonesIndex;
  private Map<String,IntegerHolder> _typesCount=new HashMap<String,IntegerHolder>();

  /**
   * Constructor.
   * @param facade Data facade.
   * @param mapsDataManager Maps data manager.
   * @param dungeonLoader Loader for dungeons.
   * @param geoAreasLoader Loader for geographic areas.
   */
  public MarkersLoadingUtils(DataFacade facade, MapsDataManager mapsDataManager, DungeonLoader dungeonLoader, GeoAreasLoader geoAreasLoader)
  {
    _facade=facade;
    _mapNoteType=facade.getEnumsManager().getEnumMapper(587202775);
    _mapsDataManager=mapsDataManager;
    _dungeonLoader=dungeonLoader;
    _geoAreasLoader=geoAreasLoader;
    ParentZonesLoader parentZoneLoader=new ParentZonesLoader(facade);
    _parentZonesIndex=new ParentZoneIndex(parentZoneLoader);
  }

  /**
   * Marker data logging.
   * @param position Position.
   * @param areaDID Parent area.
   * @param dungeonDID Parent dungeon.
   * @param noteDID Associated DID.
   * @param contentLayersArray Content layers.
   * @param text Text override.
   * @param type Type override.
   */
  public void log(DatPosition position, int areaDID, int dungeonDID, int noteDID,
      Object[] contentLayersArray, String text, long type)
  {
    System.out.println("Position: "+position);
    DataIdentification dataId=null;
    if (noteDID!=0)
    {
      // {Milestone=327, Landmark=2459, Waypoint=1308, DoorTemplate=874, IItem=857, Hotspot=267, NPCTemplate=4273}
      dataId=DataIdentificationTools.identify(_facade,noteDID);
      IntegerHolder counter=_typesCount.get(dataId.getWClassName());
      if (counter==null)
      {
        counter=new IntegerHolder();
        _typesCount.put(dataId.getWClassName(),counter);
      }
      counter.increment();
    }
    System.out.println("Data ID: "+dataId);
    if ((contentLayersArray!=null) && (contentLayersArray.length>0))
    {
      System.out.println("Content layers: "+Arrays.toString(contentLayersArray));
    }
    if ((text!=null) && (text.length()>0))
    {
      System.out.println("Text: "+text);
    }
    BitSet typeSet=BitSetUtils.getBitSetFromFlags(type);
    String typeStr=BitSetUtils.getStringFromBitSet(typeSet,_mapNoteType," / ");
    System.out.println("Type: "+type+" => "+typeStr);
  }

  /**
   * Build a marker.
   * @param position Position.
   * @param dataId Data identifier.
   * @param layerId Content layer identifier.
   */
  public void buildMarker(DatPosition position, DataIdentification dataId, int layerId)
  {
    Marker marker=MarkerUtils.buildMarker(position,dataId);
    if (marker==null)
    {
      return;
    }
    int region=position.getRegion();
    if ((region<1) || (region>4))
    {
      LOGGER.warn("Found unsupported region: "+region);
      return;
    }
    ParentZoneLandblockData parentData=_parentZonesIndex.getLandblockData(position.getRegion(),position.getBlockX(),position.getBlockY());
    if (parentData==null)
    {
      LOGGER.warn("No parent data for: "+position);
      return;
    }
    _mapsDataManager.registerMarker(marker,region,position.getBlockX(),position.getBlockY());
    // Indexs
    // - parent zone
    int cell=position.getCell();
    Integer parentArea=(parentData!=null)?parentData.getParentData(cell):null;
    if (parentArea!=null)
    {
      _mapsDataManager.registerDidMarker(parentArea.intValue(),marker);
    }
    // - content layer
    _mapsDataManager.registerContentLayerMarker(layerId,marker);
  }

  /**
   * Load a marker.
   * @param position Position.
   * @param areaDID Parent area.
   * @param dungeonDID Parent dungeon.
   * @param noteDID Associated DID.
   * @param contentLayersArray Content layers.
   * @param text Text override.
   * @param type Type override.
   */
  public void loadMarker(DatPosition position, int areaDID, int dungeonDID, int noteDID, Object[] contentLayersArray, String text, long type)
  {
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
    Identifiable where=null;
    Identifiable area=null;
    if (areaDID!=0)
    {
      where=getAreaOrDungeon(areaDID);
      area=where;
    }
    if ((area==null) && (areaDID!=0))
    {
      LOGGER.warn("Area is null: ID="+areaDID);
    }
    if (dungeonDID!=0)
    {
      where=_dungeonLoader.getDungeon(dungeonDID);
    }
    //System.out.println("Area: "+area);
    /*
    if (dungeon!=null)
    {
      System.out.println("Dungeon: "+dungeon);
    }
    */
    if (where==null)
    {
      LOGGER.warn("Unidentified geo entity! AreaID="+areaDID+", DungeonID="+dungeonDID);
      return;
    }
    /*
    ParentZoneLandblockData parentData=_parentZonesIndex.getLandblockData(position.getRegion(),position.getBlockX(),position.getBlockY());
    if (parentData==null)
    {
      LOGGER.warn("No parent data for: "+position);
    }
    int cell=position.getCell();
    if ((cell!=0) && (!(where instanceof Dungeon)))
    {
      // It happens: once in Trum Dreng, and about 10 times in the "Eyes and Guard Tavern"
      //LOGGER.warn("Cell="+cell+" while where="+where+" for position: "+position);
    }
    */
    /*
    Integer parentArea=(parentData!=null)?parentData.getParentData(cell):null;
    Integer whereId=(where!=null)?Integer.valueOf(where.getIdentifier()):null;
    if (!Objects.equals(whereId,parentArea))
    {
      LOGGER.warn("Parent mismatch: got="+whereId+", expected="+parentArea);
    }
    */
    DataIdentification dataId=DataIdentificationTools.identify(_facade,noteDID);
    if (dataId==null)
    {
      return;
    }
    // Build marker
    Marker marker=MarkerUtils.buildMarker(position,dataId);
    if (marker==null)
    {
      return;
    }
    // Patch marker
    if (text!=null)
    {
      marker.setLabel(text);
    }
    if (type>0)
    {
      BitSet typeSet=BitSetUtils.getBitSetFromFlags(type);
      int code=typeSet.nextSetBit(0)+1;
      marker.setCategoryCode(code);
    }
    // Register this marker
    _mapsDataManager.registerMarker(marker,region,position.getBlockX(),position.getBlockY());
    // Indexs
    // - parent zone
    _mapsDataManager.registerDidMarker(where.getIdentifier(),marker);
    // - content layer
    if ((contentLayersArray!=null) && (contentLayersArray.length>0))
    {
      for(Object contentLayerObj : contentLayersArray)
      {
        int layerId=((Integer)contentLayerObj).intValue();
        if (layerId==0)
        {
          LOGGER.warn("Found CL 0!");
        }
        _mapsDataManager.registerContentLayerMarker(layerId,marker);
      }
    }
    else
    {
      // World
      _mapsDataManager.registerContentLayerMarker(0,marker);
    }
  }

  /**
   * Get an area of dungeon from a zone identifier.
   * @param id Identifier to use.
   * @return the found zone or <code>null</code> if not found.
   */
  public Identifiable getAreaOrDungeon(int id)
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
}
