package delta.games.lotro.tools.dat.instances;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.games.lotro.common.comparators.NamedComparator;
import delta.games.lotro.config.DataFiles;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.lore.instances.InstanceMapDescription;
import delta.games.lotro.lore.instances.InstancesTree;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.SkirmishPrivateEncounter;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.ContainersManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.comparators.ItemNameComparator;
import delta.games.lotro.lore.items.containers.ItemsContainer;
import delta.games.lotro.lore.items.containers.LootTables;
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
  private Map<Integer,Set<PrivateEncounter>> _mapByContainer;
  private Map<Integer,Set<Integer>> _mapByPe;

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
    _mapByContainer=new HashMap<Integer,Set<PrivateEncounter>>();
    _mapByPe=new HashMap<Integer,Set<Integer>>();
    //_containers=findContainersWithCustomLootTables();
    _containers=findContainers();
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    for(SkirmishPrivateEncounter skirmishPe : peMgr.getSkirmishPrivateEncounters())
    {
      doIt(skirmishPe);
    }
    showResults();
  }

  private void doIt(SkirmishPrivateEncounter pe)
  {
    Map<Integer,Marker> markers=getMarkersForInstance(pe);
    Set<Integer> items=new HashSet<Integer>();
    _mapByPe.put(Integer.valueOf(pe.getIdentifier()),items);
    for(Integer containerId : _containers.keySet())
    {
      for(Marker marker : markers.values())
      {
        if (marker.getDid()==containerId.intValue())
        {
          // Register in containers map
          Set<PrivateEncounter> pes=_mapByContainer.get(containerId);
          if (pes==null)
          {
            pes=new HashSet<PrivateEncounter>();
            _mapByContainer.put(containerId,pes);
          }
          pes.add(pe);
          // Register in pes map
          items.add(containerId);
          pes.add(pe);
          break;
        }
      }
    }
  }

  private void showResults()
  {
    //showByContainer();
    showByInstance();
  }

  @SuppressWarnings("unused")
  private void showByContainer()
  {
    ItemsManager itemsMgr=ItemsManager.getInstance();
    List<Integer> containerIds=new ArrayList<Integer>(_mapByContainer.keySet());
    Collections.sort(containerIds);
    for(Integer containerId : containerIds)
    {
      Item item=itemsMgr.getItem(containerId.intValue());
      System.out.println("Container: "+item+" was found in:");
      List<PrivateEncounter> pes=new ArrayList<PrivateEncounter>(_mapByContainer.get(containerId));
      Collections.sort(pes,new NamedComparator());
      for(PrivateEncounter pe : pes)
      {
        System.out.println("\t"+pe.getName());
      }
    }
  }

  private void showByInstance()
  {
    ItemsManager itemsMgr=ItemsManager.getInstance();
    List<Integer> peIds=new ArrayList<Integer>(_mapByPe.keySet());
    Collections.sort(peIds);
    for(SkirmishPrivateEncounter pe : InstancesTree.getInstance().getInstances())
    {
      Integer peId=Integer.valueOf(pe.getIdentifier());
      List<Item> items=new ArrayList<Item>();
      for(Integer itemId : _mapByPe.get(peId))
      {
        items.add(itemsMgr.getItem(itemId.intValue()));
      }
      Collections.sort(items,new ItemNameComparator());
      System.out.println("Private encounter "+pe.getName()+" has chests:");
      for(Item item : items)
      {
        System.out.println("\t"+item);
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
        LootTables lootTables=itemsContainer.getLootTables();
        Integer tableId=lootTables.getCustomSkirmishLootTableId();
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
