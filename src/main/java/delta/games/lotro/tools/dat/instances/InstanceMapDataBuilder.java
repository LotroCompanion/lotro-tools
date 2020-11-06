package delta.games.lotro.tools.dat.instances;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.geo.BlockReferenceComparator;
import delta.games.lotro.lore.instances.InstanceMapDescription;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.MarkersFinder;
import delta.games.lotro.tools.dat.maps.MapUtils;
import delta.games.lotro.tools.dat.maps.MarkerUtils;
import delta.games.lotro.tools.dat.maps.landblocks.Landblock;
import delta.games.lotro.tools.dat.maps.landblocks.LandblocksManager;

/**
 * Build map data for instances.
 * @author DAM
 */
public class InstanceMapDataBuilder
{
  private MarkersFinder _finder;
  private LandblocksManager _landblocksManager;

  /**
   * Constructor.
   */
  public InstanceMapDataBuilder()
  {
    _landblocksManager=LandblocksManager.getInstance();
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    _finder=mapsManager.getMarkersFinder();
  }

  /**
   * Load map data for a private encounter.
   * @param privateEncounter Private encounter to use.
   * @param blocks Blocks for this encounter.
   */
  public void handlePrivateEncounter(PrivateEncounter privateEncounter, List<BlockReference> blocks)
  {
    int contentLayerId=privateEncounter.getContentLayerId();
    List<Marker> markers=_finder.findMarkersForContentLayer(contentLayerId);
    /*
    System.out.println("PE: "+privateEncounter.getName());
    System.out.println("\tFound "+markers.size()+" markers.");
    System.out.println("Blocks: "+blocks);
    for(Marker marker : markers)
    {
      System.out.println(marker+" => "+getBlock(marker));
    }
    */
    List<Integer> additionalContentLayers=privateEncounter.getAdditionalContentLayers();
    for(Integer contentLayer : additionalContentLayers)
    {
      List<Marker> markersInBlocks=findMarkersForBlocks(blocks,contentLayer);
      markers.addAll(markersInBlocks);
    }

    markers=filterMarkers(markers,blocks);

    Map<Integer,List<Marker>> sortedMarkers=sortMarkersByZone(markers);
    //System.out.println("\tFound "+sortedMarkers.size()+" zones.");
    //Identifiable map=MapUtils.findMapForZone(parentZoneID.intValue());
    List<Integer> dungeonsIds=findDungeons(sortedMarkers.keySet());
    for(Integer dungeonId : dungeonsIds)
    {
      sortedMarkers.remove(dungeonId);
    }
    List<Marker> landscapeMarkers=new ArrayList<Marker>();
    for(List<Marker> markersForZone : sortedMarkers.values())
    {
      landscapeMarkers.addAll(markersForZone);
    }
    List<BlockReference> newBlocks=getBlocks(landscapeMarkers);
    BlockGroupsBuilder builder=new BlockGroupsBuilder();
    List<List<BlockReference>> groups=builder.buildGroups(newBlocks);

    // Dungeons
    Collections.sort(dungeonsIds);
    for(Integer dungeonId : dungeonsIds)
    {
      InstanceMapDescription mapDescription=new InstanceMapDescription(dungeonId);
      privateEncounter.addMapDescription(mapDescription);
      mapDescription.addZoneId(dungeonId.intValue());
    }
    // Landscape
    for(List<BlockReference> group : groups)
    {
      Integer mapId=null;
      List<Integer> areaIds=getAreasForBlocks(group);
      if (areaIds.size()>0)
      {
        // Assume same map for all the blocks of a group
        int areaId=areaIds.get(0).intValue();
        Identifiable map=MapUtils.findMapForZone(areaId);
        if (map!=null)
        {
          mapId=Integer.valueOf(map.getIdentifier());
        }
      }
      InstanceMapDescription mapDescription=new InstanceMapDescription(mapId);
      for(Integer areaId : areaIds)
      {
        mapDescription.addZoneId(areaId.intValue());
      }
      for(BlockReference block : group)
      {
        mapDescription.addBlock(block);
      }
      privateEncounter.addMapDescription(mapDescription);
    }
    // Fixes
    fixes(privateEncounter);
    int nbMaps=privateEncounter.getMapDescriptions().size();
    System.out.println("Found "+nbMaps+" map(s) for "+privateEncounter);
  }

  private List<Marker> findMarkersForBlocks(List<BlockReference> blocks, Integer contentLayer)
  {
    List<Marker> ret=new ArrayList<Marker>();
    for(BlockReference block : blocks)
    {
      List<Marker> markers=_finder.findMarkersForBlock(block.getRegion(),block.getBlockX(),block.getBlockY(),contentLayer);
      ret.addAll(markers);
    }
    return ret;
  }

  private Map<Integer,List<Marker>> sortMarkersByZone(List<Marker> markers)
  {
    Map<Integer,List<Marker>> ret=new HashMap<Integer,List<Marker>>();
    for(Marker marker : markers)
    {
      Integer key=Integer.valueOf(marker.getParentZoneId());
      List<Marker> sortedList=ret.get(key);
      if (sortedList==null)
      {
        sortedList=new ArrayList<Marker>();
        ret.put(key,sortedList);
      }
      sortedList.add(marker);
    }
    return ret;
  }

  private List<Integer> findDungeons(Set<Integer> zoneIds)
  {
    DungeonsManager dungeonsMgr=DungeonsManager.getInstance();
    List<Integer> ret=new ArrayList<Integer>();
    for(Integer zoneId : zoneIds)
    {
      Dungeon dungeon=dungeonsMgr.getDungeonById(zoneId.intValue());
      if (dungeon!=null)
      {
        ret.add(zoneId);
      }
    }
    return ret;
  }

  private List<BlockReference> getBlocks(List<Marker> markers)
  {
    Map<String,BlockReference> map=new HashMap<String,BlockReference>();
    for(Marker marker : markers)
    {
      BlockReference block=getBlock(marker);
      map.put(block.toString(),block);
    }
    List<BlockReference> ret=new ArrayList<BlockReference>(map.values());
    Collections.sort(ret,new BlockReferenceComparator());
    return ret;
  }

  private List<Marker> filterMarkers(List<Marker> markers, List<BlockReference> blocks)
  {
    List<Marker> ret=new ArrayList<Marker>();
    for(Marker marker : markers)
    {
      BlockReference markerBlock=getBlock(marker);
      if (blocks.contains(markerBlock))
      {
        ret.add(marker);
      }
    }
    return ret;
  }

  private List<Integer> getAreasForBlocks(List<BlockReference> blocks)
  {
    List<Integer> ret=new ArrayList<Integer>();
    for(BlockReference block : blocks)
    {
      Integer areaId=getAreaForBlock(block);
      if (areaId!=null)
      {
        if (!ret.contains(areaId))
        {
          ret.add(areaId);
        }
      }
    }
    return ret;
  }

  private Integer getAreaForBlock(BlockReference block)
  {
    Landblock lb=_landblocksManager.getLandblock(block.getRegion(),block.getBlockX(),block.getBlockY());
    if (lb!=null)
    {
      return lb.getParentArea();
    }
    return null;
  }

  /**
   * Get the block reference for a marker.
   * @param marker Marker to use.
   * @return A block reference.
   */
  private BlockReference getBlock(Marker marker)
  {
    int markerId=marker.getId();
    return MarkerUtils.getBlockForMarker(markerId);
  }

  private void fixes(PrivateEncounter privateEncounter)
  {
    int peId=privateEncounter.getIdentifier();
    // 1879184816 Dragon Wing: remove Spider Wing map
    if (peId==1879184816)
    {
      privateEncounter.removeMapDescription(1879084152);
    }
    // 1879224851 Storm on Methedras: remove zone 1879201776 "Tâl Methedras"
    else if (peId==1879224851)
    {
      privateEncounter.removeZone(1879201776);
    }
  }
}
