package delta.games.lotro.tools.dat.maps.instances;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.lore.geo.BlockReference;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.ZoneAndMap;
import delta.games.lotro.lore.instances.io.xml.PrivateEncountersXMLWriter;
import delta.games.lotro.lore.maps.Area;
import delta.games.lotro.lore.maps.Dungeon;
import delta.games.lotro.lore.maps.DungeonsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.ParchmentMap;
import delta.games.lotro.lore.maps.ParchmentMapsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneIndex;
import delta.games.lotro.tools.dat.maps.indexs.ParentZoneLandblockData;
import delta.games.lotro.tools.dat.maps.indexs.ParentZonesLoader;

/**
 * A tool to find the maps that fits for each instance.
 * @author DAM
 */
public class MainFindMapsForInstances
{
  private static final Logger LOGGER=Logger.getLogger(MainFindMapsForInstances.class);

  private ParentZoneIndex _parentZoneIndex;

  /**
   * Constructor.
   * @param parentZoneIndex
   */
  public MainFindMapsForInstances(ParentZoneIndex parentZoneIndex)
  {
    _parentZoneIndex=parentZoneIndex;
  }

  private void doIt()
  {
    PrivateEncountersManager peManager=PrivateEncountersManager.getInstance();
    List<PrivateEncounter> privateEncounters=peManager.getPrivateEncounters();
    for(PrivateEncounter privateEncounter : privateEncounters)
    {
      handlePrivateEncounter(privateEncounter);
    }
    boolean ok=PrivateEncountersXMLWriter.writePrivateEncountersFile(GeneratedFiles.PRIVATE_ENCOUNTERS,peManager.getPrivateEncounters());
    if (ok)
    {
      System.out.println("Wrote private encounters file: "+GeneratedFiles.PRIVATE_ENCOUNTERS);
    }
  }

  private void handlePrivateEncounter(PrivateEncounter privateEncounter)
  {
    List<Integer> parentZoneIds=new ArrayList<Integer>();
    List<BlockReference> blocks=privateEncounter.getBlocks();
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
      if (lbData!=null)
      {
        Integer dungeonId=lbData.getParentDungeon();
        if (dungeonId!=null)
        {
          if (!parentZoneIds.contains(dungeonId))
          {
            parentZoneIds.add(dungeonId);
          }
        }
        else
        {
          Integer areaId=lbData.getParentArea();
          if (areaId!=null)
          {
            if (!parentZoneIds.contains(areaId))
            {
              parentZoneIds.add(areaId);
            }
          }
        }
      }
      else
      {
        LOGGER.warn("Landblock data not found for: "+block);
      }
    }
    //int id=privateEncounter.getIdentifier();
    //String name=privateEncounter.getName();
    //System.out.println("Zones for ID="+id+" ("+name+"): "+parentZoneIds);
    //List<Identifiable> maps=new ArrayList<Identifiable>();
    for(Integer parentZoneID : parentZoneIds)
    {
      Identifiable map=findMapForZone(parentZoneID.intValue());
      Integer mapId=(map!=null)?Integer.valueOf(map.getIdentifier()):null;
      ZoneAndMap zoneAndMap=new ZoneAndMap(parentZoneID.intValue(),mapId);
      privateEncounter.addZoneAndMap(zoneAndMap);
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

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    ParentZonesLoader parentZoneLoader=new ParentZonesLoader(facade);
    ParentZoneIndex parentZonesIndex=new ParentZoneIndex(parentZoneLoader);
    MainFindMapsForInstances main=new MainFindMapsForInstances(parentZonesIndex);
    main.doIt();
  }
}
