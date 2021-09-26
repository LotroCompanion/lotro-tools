package delta.games.lotro.tools.dat.instances;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.lore.instances.InstanceMapDescription;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.ContainersManager;
import delta.games.lotro.lore.items.ItemsContainer;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.markers.MarkersFinder;

/**
 * Finds chests available in the instances.
 * @author DAM
 */
public class InstanceChestsFinder
{
  private MapsManager _mapsManager;
  private Map<Integer,ItemsContainer> _containers;

  /**
   * Constructor.
   */
  public InstanceChestsFinder()
  {
    LotroCoreConfig coreConfig=LotroCoreConfig.getInstance();
    File mapsDir=coreConfig.getFile(DataFiles.MAPS);
    _mapsManager=new MapsManager(mapsDir);
  }

  private void doIt()
  {
    //_containers=findContainersWithCustomLootTables();
    _containers=findContainers();
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    for(SkirmishPrivateEncounter skirmishPe : peMgr.getSkirmishPrivateEncounters())
    {
      doIt(skirmishPe);
    }
  }

  private void doIt(SkirmishPrivateEncounter pe)
  {
    Map<Integer,Marker> markers=getMarkersForInstance(pe);
    for(Integer containerId : _containers.keySet())
    {
      for(Marker marker : markers.values())
      {
        if (marker.getDid()==containerId.intValue())
        {
          System.out.println("Found container "+containerId+" in PE: "+pe.getName());
          break;
        }
      }
    }
  }

  private Map<Integer,Marker> getMarkersForInstance(SkirmishPrivateEncounter pe)
  {
    MarkersFinder markersFinder=_mapsManager.getMarkersFinder();
    int contentLayer=pe.getContentLayerId();

    List<Marker> allMarkers=new ArrayList<Marker>();
    for(InstanceMapDescription instanceMap : pe.getMapDescriptions())
    {
      List<Integer> zones=instanceMap.getZoneIds();
      for(Integer zone : zones)
      {
        List<Marker> markers=markersFinder.findMarkers(zone.intValue(),contentLayer);
        allMarkers.addAll(markers);
        for(Integer contentLayerId : pe.getAdditionalContentLayers())
        {
          List<Marker> clMarkers=markersFinder.findMarkers(zone.intValue(),contentLayerId.intValue());
          allMarkers.addAll(clMarkers);
        }
        List<Marker> markers2=markersFinder.findMarkers(zone.intValue(),2);
        allMarkers.addAll(markers2);
      }
    }
    Map<Integer,Marker> ret=new HashMap<Integer,Marker>();
    for(Marker marker : allMarkers)
    {
      ret.put(Integer.valueOf(marker.getId()),marker);
    }
    return ret;
  }

  Map<Integer,ItemsContainer> findContainersWithCustomLootTables()
  {
    Map<Integer,ItemsContainer> ret=new HashMap<Integer,ItemsContainer>();
    ContainersManager containersMgr=ContainersManager.getInstance();
    for(Container container : containersMgr.getContainers())
    {
      if (container instanceof ItemsContainer)
      {
        ItemsContainer itemsContainer=(ItemsContainer)container;
        Integer tableId=itemsContainer.getCustomSkirmishLootTableId();
        if (tableId!=null)
        {
          Integer key=Integer.valueOf(itemsContainer.getIdentifier());
          ret.put(key,itemsContainer);
        }
      }
    }
    return ret;
  }

  private Map<Integer,ItemsContainer> findContainers()
  {
    Map<Integer,ItemsContainer> ret=new HashMap<Integer,ItemsContainer>();
    ContainersManager containersMgr=ContainersManager.getInstance();
    for(Container container : containersMgr.getContainers())
    {
      if (container instanceof ItemsContainer)
      {
        ItemsContainer itemsContainer=(ItemsContainer)container;
        Integer key=Integer.valueOf(itemsContainer.getIdentifier());
        ret.put(key,itemsContainer);
      }
    }
    return ret;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new InstanceChestsFinder().doIt();
  }
}
