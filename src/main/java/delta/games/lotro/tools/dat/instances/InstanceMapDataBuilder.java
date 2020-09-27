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
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
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
      Integer parentZoneId=null;
      if (lbData!=null)
      {
        Integer dungeonId=lbData.getParentDungeon();
        if (dungeonId!=null)
        {
          parentZoneId=dungeonId;
        }
        else
        {
          Integer areaId=lbData.getParentArea();
          if (areaId!=null)
          {
            parentZoneId=areaId;
          }
        }
        if (parentZoneId==null)
        {
          LOGGER.warn("No parent zone for block: "+block);
        }
      }
      else
      {
        LOGGER.warn("Landblock data not found for: "+block);
      }
      if (parentZoneId!=null)
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
      Identifiable map=findMapForZone(parentZoneID.intValue());
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

  private Identifiable getZone(int zoneId)
  {
    // Dungeon?
    DungeonsManager dungeonsManager=DungeonsManager.getInstance();
    Dungeon dungeon=dungeonsManager.getDungeonById(zoneId);
    if (dungeon!=null)
    {
      return dungeon;
    }
    // Area?
    GeoAreasManager geoAreasManager=GeoAreasManager.getInstance();
    Area area=geoAreasManager.getAreaById(zoneId);
    return area;
  }

  private Identifiable findMapForZone(int zoneId)
  {
    Identifiable zone=getZone(zoneId);
    if (zone instanceof Dungeon)
    {
      return zone;
    }
    if (zone instanceof Area)
    {
      // Find parent map...
      ParchmentMapsManager parchmentMapsManager=ParchmentMapsManager.getInstance();
      ParchmentMap parchmentMap=parchmentMapsManager.getParchmentMapForArea(zoneId);
      if (parchmentMap!=null)
      {
        return parchmentMap;
      }
    }
    return null;
  }
}
