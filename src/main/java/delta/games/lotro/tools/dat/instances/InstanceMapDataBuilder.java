package delta.games.lotro.tools.dat.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.instances.InstanceMapDescription;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.tools.dat.maps.MapUtils;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneLandblockData;

/**
 * Build map data for instances.
 * @author DAM
 */
public class InstanceMapDataBuilder
{
  private static final Logger LOGGER=Logger.getLogger(InstanceMapDataBuilder.class);

  private ParentZoneIndex _parentZoneIndex;

  /**
   * Constructor.
   * @param parentZoneIndex
   */
  public InstanceMapDataBuilder(ParentZoneIndex parentZoneIndex)
  {
    _parentZoneIndex=parentZoneIndex;
  }

  /**
   * Load map data for a private encounter.
   * @param privateEncounter Private encounter to use.
   * @param blocks Blocks for this encounter.
   */
  public void handlePrivateEncounter(PrivateEncounter privateEncounter, List<BlockReference> blocks)
  {
    Map<Integer,List<BlockReference>> blocksByZone=new HashMap<Integer,List<BlockReference>>();
    List<Integer> parentZoneIds=new ArrayList<Integer>();

    // Find parent zones for each block
    for(BlockReference block : blocks)
    {
      int region=block.getRegion();
      if ((region<1) || (region>4))
      {
        continue;
      }
      int blockX=block.getBlockX();
      int blockY=block.getBlockY();
      ParentZoneLandblockData lbData=_parentZoneIndex.getLandblockData(region,blockX,blockY);
      List<Integer> parentZoneIdsForBlock=null;
      if (lbData!=null)
      {
        parentZoneIdsForBlock=getParentZones(lbData);
        if (parentZoneIdsForBlock.size()==0)
        {
          LOGGER.warn("No parent zone for block: "+block);
        }
      }
      else
      {
        parentZoneIdsForBlock=new ArrayList<Integer>();
        LOGGER.warn("Landblock data not found for: "+block);
      }

      for(Integer parentZoneId : parentZoneIdsForBlock)
      {
        // Add block for zone
        List<BlockReference> blocksForZone=blocksByZone.get(parentZoneId);
        if (blocksForZone==null)
        {
          blocksForZone=new ArrayList<BlockReference>();
          blocksByZone.put(parentZoneId,blocksForZone);
        }
        blocksForZone.add(block);
        // Add zone in zones list
        if (!parentZoneIds.contains(parentZoneId))
        {
          parentZoneIds.add(parentZoneId);
        }
      }
    }
    //int id=privateEncounter.getIdentifier();
    //String name=privateEncounter.getName();
    //System.out.println("Zones for ID="+id+" ("+name+"): "+parentZoneIds);
    //List<Identifiable> maps=new ArrayList<Identifiable>();
    Map<Integer,InstanceMapDescription> foundMaps=new HashMap<Integer,InstanceMapDescription>();
    for(Integer parentZoneID : parentZoneIds)
    {
      Identifiable map=MapUtils.findMapForZone(parentZoneID.intValue());
      Integer mapId=(map!=null)?Integer.valueOf(map.getIdentifier()):null;
      InstanceMapDescription mapDescription=null;
      if (mapId!=null)
      {
        mapDescription=foundMaps.get(mapId);
        if (mapDescription==null)
        {
          mapDescription=new InstanceMapDescription(mapId);
          foundMaps.put(mapId,mapDescription);
          privateEncounter.addMapDescription(mapDescription);
        }
      }
      else
      {
        mapDescription=new InstanceMapDescription(null);
        privateEncounter.addMapDescription(mapDescription);
      }
      mapDescription.addZoneId(parentZoneID.intValue());
      for(BlockReference block : blocksByZone.get(parentZoneID))
      {
        mapDescription.addBlock(block);
      }
    }
    /*
    if (maps.size()>1)
    {
      System.out.println("More than one map: "+maps.size());
    }
    for(Identifiable map : maps)
    {
      System.out.println("\t=> Map: "+map);
    }
    */
  }

  private List<Integer> getParentZones(ParentZoneLandblockData lbData)
  {
    List<Integer> ret=new ArrayList<Integer>();
    List<Integer> dungeonsFromCells=lbData.getDungeonsFromCells();
    ret.addAll(dungeonsFromCells);
    Integer dungeonId=lbData.getParentDungeon();
    if ((dungeonId!=null) && (!ret.contains(dungeonId)))
    {
      ret.add(dungeonId);
    }
    if (ret.size()==0)
    {
      Integer areaId=lbData.getParentArea();
      if (areaId!=null)
      {
        ret.add(areaId);
      }
    }
    return ret;
  }
}
