package delta.games.lotro.tools.dat.maps;

import java.util.Arrays;
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
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.loaders.PositionDecoder;
import delta.games.lotro.dat.utils.BitSetUtils;
import delta.games.lotro.dat.utils.DataIdentificationTools;
import delta.games.lotro.lore.maps.AbstractMap;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.lore.maps.Zone;
import delta.games.lotro.lore.maps.ZoneUtils;
import delta.games.lotro.lore.maps.landblocks.Landblock;
import delta.games.lotro.lore.maps.landblocks.LandblocksManager;
import delta.games.lotro.maps.data.GeoPoint;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.links.LinksManager;
import delta.games.lotro.maps.data.links.MapLink;
import delta.games.lotro.maps.data.links.MapLinkType;

/**
 * Marker loading utilities.
 * @author DAM
 */
public class MarkersLoadingUtils
{
  private static final Logger LOGGER=Logger.getLogger(MarkersLoadingUtils.class);
  private static final boolean DO_CHECK=false;

  private DataFacade _facade;
  private EnumMapper _mapNoteType;
  private MapsDataManager _mapsDataManager;
  private Map<Integer,IntegerHolder> _typesCount=new HashMap<Integer,IntegerHolder>();
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
      Integer key=Integer.valueOf(dataId.getClassIndex());
      IntegerHolder counter=_typesCount.get(key);
      if (counter==null)
      {
        counter=new IntegerHolder();
        _typesCount.put(key,counter);
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
    int region=position.getRegion();
    if (((region<1) || (region>4)) && (region!=14))
    {
      LOGGER.warn("Found unsupported region: "+region);
      return null;
    }
    Integer parentZoneId=MapUtils.getParentZone(position);
    if (parentZoneId==null)
    {
      LOGGER.warn("No parent zone for marker!");
      return null;
    }
    Marker marker=MarkerUtils.buildMarker(position,dataId);
    if (marker==null)
    {
      return null;
    }
    // Register marker
    _mapsDataManager.registerMarker(marker,region,position.getBlockX(),position.getBlockY(),parentZoneId.intValue());
    // Content layer
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
  public Marker buildMarker(DatPosition position, int areaDID, int dungeonDID, int noteDID, Object[] contentLayersArray, String text, long type)
  {
    // Checks
    int region=position.getRegion();
    if (((region<1) || (region>4)) && (region!=14))
    {
      LOGGER.warn("Weird region value: "+region);
      return null;
    }
    int instance=position.getInstance();
    if (instance!=0)
    {
      LOGGER.warn("Instance is not 0 for position: "+position);
    }
    Zone where=null;
    if (areaDID!=0)
    {
      where=ZoneUtils.getZone(areaDID);
      if (where==null)
      {
        LOGGER.warn("Area is null: ID="+areaDID);
      }
    }
    if (dungeonDID!=0)
    {
      DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
      where=dungeonsMgr.getDungeonById(dungeonDID);
    }
    if (where==null)
    {
      LOGGER.warn("Unidentified geo entity! AreaID="+areaDID+", DungeonID="+dungeonDID);
      return null;
    }
    // Checks
    if (DO_CHECK)
    {
      // Verified to work 100%
      Landblock landblock=LandblocksManager.getInstance().getLandblock(position.getRegion(),position.getBlockX(),position.getBlockY());
      if (landblock==null)
      {
        LOGGER.warn("No parent data for: "+position);
      }
      int cell=position.getCell();
      if ((cell!=0) && (!(where instanceof Dungeon)))
      {
        // It happens: once in Trum Dreng, and about 10 times in the "Eyes and Guard Tavern"
        //LOGGER.warn("Cell="+cell+" while where="+where+" for position: "+position);
      }
      Integer parentArea=(landblock!=null)?landblock.getParentData(cell,position.getPosition()):null;
      Integer whereId=(where!=null)?Integer.valueOf(where.getIdentifier()):null;
      if (!Objects.equals(whereId,parentArea))
      {
        LOGGER.warn("Parent mismatch: got="+whereId+", expected="+parentArea);
      }
    }
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
    // Content layer
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
   * @param destPosition Destination position.
   * @param destArea Destination area/dungeon.
   * @param contentLayersArray Content layers.
   * @param text Link text.
   */
  public void addLink(DatPosition position, int areaDID, int dungeonDID, int noteDID, DatPosition destPosition, Identifiable destArea, Object[] contentLayersArray, String text)
  {
    DataIdentification dataId=DataIdentificationTools.identify(_facade,noteDID);
    if (dataId==null)
    {
      return;
    }
    int classIndex=dataId.getClassIndex();
    if ((classIndex!=815) && (classIndex!=1722))
    {
      LOGGER.warn("Link DID is not a Waypoint or a DoorTemplate");
      return;
    }
    if ((text!=null) && (text.length()>0))
    {
      LOGGER.warn("Link has text: ["+text+"]");
    }
    Zone where=null;
    if (dungeonDID!=0)
    {
      where=ZoneUtils.getZone(dungeonDID);
    }
    if (where==null)
    {
      where=ZoneUtils.getZone(areaDID);
    }
    AbstractMap targetMap=getTargetMap(destArea,destPosition);
    if (targetMap==null)
    {
      return;
    }
    if (where==null)
    {
      return;
    }
    text="To: "+targetMap.getName();
    float[] fromLonLat=PositionDecoder.decodePosition(position.getBlockX(),position.getBlockY(),position.getPosition().getX(),position.getPosition().getY());
    GeoPoint fromPoint=new GeoPoint(fromLonLat[0],fromLonLat[1]);
    float[] toLonLat=PositionDecoder.decodePosition(destPosition.getBlockX(),destPosition.getBlockY(),destPosition.getPosition().getX(),destPosition.getPosition().getY());
    GeoPoint toPoint=new GeoPoint(toLonLat[0],toLonLat[1]);
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
        MapLink link=new MapLink(where.getIdentifier(),contentLayer,targetMap.getIdentifier(),fromPoint,toPoint);
        if (targetMap instanceof Dungeon)
        {
          link.setType(MapLinkType.TO_DUNGEON);
        }
        link.setLabel(text);
        _links.addLink(link);
      }
    }
    else
    {
      MapLink link=new MapLink(where.getIdentifier(),0,targetMap.getIdentifier(),fromPoint,toPoint);
      if (targetMap instanceof Dungeon)
      {
        link.setType(MapLinkType.TO_DUNGEON);
      }
      link.setLabel(text);
      _links.addLink(link);
    }
  }

  private AbstractMap getTargetMap(Identifiable destArea, DatPosition destPosition)
  {
    AbstractMap targetMap=null;
    if (destArea instanceof Dungeon)
    {
      Dungeon dungeon=(Dungeon)destArea;
      targetMap=dungeon;
    }
    else if (destArea instanceof Area)
    {
      ParchmentMapsManager parchmentMapsManager=ParchmentMapsManager.getInstance();
      ParchmentMap map=parchmentMapsManager.getParchmentMapForArea(destArea.getIdentifier());
      if (map!=null)
      {
        targetMap=map;
      }
    }
    Integer parentZone=MapUtils.getParentZone(destPosition);
    Integer destId=(destArea!=null)?Integer.valueOf(destArea.getIdentifier()):null;
    if (!Objects.equals(destId,parentZone))
    {
      LOGGER.warn("Parent mismatch: got="+parentZone+", expected="+destId);
    }

    if (parentZone!=null)
    {
      AbstractMap map=MapUtils.findMapForZone(parentZone.intValue());
      if (map!=null)
      {
        targetMap=map;
      }
    }
    if (targetMap==null)
    {
      LOGGER.warn("Target map not found for target: "+destArea);
    }
    return targetMap;
  }
}
