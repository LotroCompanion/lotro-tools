package delta.games.lotro.tools.dat.maps;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.maps.resources.ResourcesMapDescriptor;
import delta.games.lotro.lore.maps.resources.ResourcesMapsManager;
import delta.games.lotro.lore.maps.resources.io.xml.ResourcesMapsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.ProxyBuilder;
import delta.games.lotro.utils.Proxy;

/**
 * Builder for resources map descriptors.
 * @author DAM
 */
public class ResourcesMapsBuilder
{
  private ResourcesMapsManager _mapsManager;

  /**
   * Constructor.
   */
  public ResourcesMapsBuilder()
  {
    _mapsManager=new ResourcesMapsManager();
  }

  /**
   * Register a resource.
   * @param itemId Item identifier.
   * @param level Crafting level.
   * @param parentZoneId Parent zone ID.
   */
  public void registerResource(int itemId, CraftingLevel level, int parentZoneId)
  {
    ResourcesMapDescriptor mapDescriptor=_mapsManager.getMapForLevel(level);
    if (mapDescriptor==null)
    {
      mapDescriptor=new ResourcesMapDescriptor(level);
      _mapsManager.addResourcesMap(mapDescriptor);
    }
    if (!mapDescriptor.hasItem(itemId))
    {
      Proxy<Item> item=ProxyBuilder.buildItemProxy(itemId);
      mapDescriptor.addItem(item);
    }
    Identifiable map=MapUtils.findMapForZone(parentZoneId);
    if (map!=null)
    {
      int mapId=map.getIdentifier();
      if (useMap(mapId,level))
      {
        mapDescriptor.addMapId(mapId);
      }
    }
  }

  private boolean useMap(int mapId, CraftingLevel level)
  {
    // Skip Eriador map
    if (mapId==268437557) return false;
    if (mapId==268447030)
    {
      // Skip Supreme for map "Dunland"
      if (level.getTier()==6) return false;
    }
    return true;
  }

  /**
   * Write loaded data to disk.
   */
  public void write()
  {
    // Save resources maps
    boolean ok=ResourcesMapsXMLWriter.writeResourcesMapsFile(GeneratedFiles.RESOURCES_MAPS,_mapsManager.getResourcesMaps());
    if (ok)
    {
      System.out.println("Wrote resources maps file: "+GeneratedFiles.RESOURCES_MAPS);
    }
  }
}
