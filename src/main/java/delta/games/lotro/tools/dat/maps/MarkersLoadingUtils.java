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
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
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
  private MapsDataManager _mapsDataManager;
  private ParentZoneIndex _parentZonesIndex;
  private Map<String,IntegerHolder> _typesCount=new HashMap<String,IntegerHolder>();
  private LinksStorage _links;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param mapsDataManager Maps data manager.
   */
  public MarkersLoadingUtils(DataFacade facade, MapsDataManager mapsDataManager)
  {
    _facade=facade;
    _mapNoteType=facade.getEnumsManager().getEnumMapper(587202775);
    _mapsDataManager=mapsDataManager;
    ParentZonesLoader parentZoneLoader=new ParentZonesLoader(facade);
    _parentZonesIndex=new ParentZoneIndex(parentZoneLoader);
    _links=new LinksStorage();
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
   * @return the generated marker or <code>null</code>.
   */
  public Marker buildMarker(DatPosition position, DataIdentification dataId, int layerId)
  {
    Marker marker=MarkerUtils.buildMarker(position,dataId);
    if (marker==null)
    {
      return null;
    }
    int region=position.getRegion();
    if ((region<1) || (region>4))
    {
      LOGGER.warn("Found unsupported region: "+region);
      return null;
    }

    Integer parentArea=_parentZonesIndex.getParentZone(position);
    if (parentArea==null)
    {
      LOGGER.warn("No parent area for marker!");
      return null;
    }
    int parentZoneId=parentArea.intValue();
    _mapsDataManager.registerMarker(marker,region,position.getBlockX(),position.getBlockY(),parentZoneId);
    // Indexs
    // - content layer
    _mapsDataManager.registerContentLayerMarker(layerId,marker);
    return marker;
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
   * @return the generated marker or <code>null</code>.
   */
  public Marker loadMarker(DatPosition position, int areaDID, int dungeonDID, int noteDID, Object[] contentLayersArray, String text, long type)
  {
    // Checks
    int region=position.getRegion();
    if ((region<1) || (region>4))
    {
      LOGGER.warn("Weird region value: "+region);
      return null;
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
      DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
      where=dungeonsMgr.getDungeonById(dungeonDID);
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
      return null;
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
      return null;
    }
    // Build marker
    Marker marker=MarkerUtils.buildMarker(position,dataId);
    if (marker==null)
    {
      return null;
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
    _mapsDataManager.registerMarker(marker,region,position.getBlockX(),position.getBlockY(),where.getIdentifier());
    // Indexs
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
    return marker;
  }

  /**
   * Register links.
   */
  public void registerLinks()
  {
    LinksManager linksMgr=_mapsDataManager.getMapsManager().getLinksManager();
    for(MapLink link : _links.getLinks())
    {
      linksMgr.addLink(link);
    }
  }

  /**
   * Add a link.
   * @param position Link position.
   * @param areaDID Source area.
   * @param dungeonDID Source dungeon.
   * @param noteDID Source DID.
   * @param destArea Destination area/dungeon.
   * @param contentLayersArray Content layers.
   * @param text Link text.
   */
  public void addLink(DatPosition position, int areaDID, int dungeonDID, int noteDID, Identifiable destArea, Object[] contentLayersArray, String text)
  {
    DataIdentification dataId=DataIdentificationTools.identify(_facade,noteDID);
    if (dataId==null)
    {
      return;
    }
    int classIndex=dataId.getWClass().getClassIndex();
    if ((classIndex!=815) && (classIndex!=1722))
    {
      LOGGER.warn("Link DID is not a Waypoint or a DoorTemplate");
      return;
    }
    if ((text!=null) && (text.length()>0))
    {
      LOGGER.warn("Link has text: ["+text+"]");
    }
    Identifiable where=null;
    if (dungeonDID!=0)
    {
      where=getAreaOrDungeon(dungeonDID);
    }
    if (where==null)
    {
      where=getAreaOrDungeon(areaDID);
    }
    String[] targetMapName=new String[1];
    int targetMapKey=getTargetMap(destArea,targetMapName);
    if (targetMapKey!=0)
    {
      text="To: "+targetMapName[0];
    }
    float[] lonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    GeoPoint geoPoint=new GeoPoint(lonLat[0],lonLat[1]);
    //System.out.println("Data ID: "+dataId);
    if ((contentLayersArray!=null) && (contentLayersArray.length>0))
    {
      for(Object contentLayerObj : contentLayersArray)
      {
        int contentLayer=((Integer)contentLayerObj).intValue();
        if (contentLayer==0)
        {
          LOGGER.warn("Found CL 0!");
        }
        if (contentLayer==1)
        {
          // Merge layer 1 "InstanceZero" with world
          contentLayer=0;
        }
        MapLink link=new MapLink(where.getIdentifier(),contentLayer,targetMapKey,geoPoint);
        link.setLabel(text);
        _links.addLink(link);
      }
    }
    else
    {
      MapLink link=new MapLink(where.getIdentifier(),0,targetMapKey,geoPoint);
      _links.addLink(link);
      link.setLabel(text);
    }
  }

  private int getTargetMap(Identifiable destArea, String[] name)
  {
    int targetMapKey=0;
    if (destArea instanceof Dungeon)
    {
      Dungeon dungeon=(Dungeon)destArea;
      targetMapKey=dungeon.getIdentifier();
      name[0]=dungeon.getName();
    }
    else if (destArea instanceof Area)
    {
      ParchmentMapsManager parchmentMapsManager=ParchmentMapsManager.getInstance();
      ParchmentMap map=parchmentMapsManager.getParchmentMapForArea(destArea.getIdentifier());
      if (map!=null)
      {
        targetMapKey=map.getIdentifier();
        name[0]=map.getName();
      }
    }
    if (targetMapKey==0)
    {
      LOGGER.warn("Target map not found for target: "+destArea);
    }
    return targetMapKey;
  }

  /**
   * Get an area of dungeon from a zone identifier.
   * @param id Identifier to use.
   * @return the found zone or <code>null</code> if not found.
   */
  public Identifiable getAreaOrDungeon(int id)
  {
    DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
    Dungeon dungeon=dungeonsMgr.getDungeonById(id);
    if (dungeon!=null)
    {
      return dungeon;
    }
    GeoAreasManager geoAreasMgr=GeoAreasManager.getInstance();
    Area area=geoAreasMgr.getAreaById(id);
    if (area!=null)
    {
      return area;
    }
    return null;
  }
}
