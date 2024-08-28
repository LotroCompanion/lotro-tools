package delta.games.lotro.tools.extraction.geo.maps;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Identifiable;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.maps.resources.ResourcesMapDescriptor;
import delta.games.lotro.lore.maps.resources.ResourcesMapsManager;
import delta.games.lotro.lore.maps.resources.io.xml.ResourcesMapsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Builder for resources map descriptors.
 * @author DAM
 */
public class ResourcesMapsBuilder
{
  private static final Logger LOGGER=Logger.getLogger(ResourcesMapsBuilder.class);

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
   * @param itemName Item name.
   * @param level Crafting level.
   * @param parentZoneId Parent zone ID.
   */
  public void registerResource(int itemId, String itemName, CraftingLevel level, int parentZoneId)
  {
    ResourcesMapDescriptor mapDescriptor=_mapsManager.getMapForLevel(level);
    if (mapDescriptor==null)
    {
      mapDescriptor=new ResourcesMapDescriptor(level);
      _mapsManager.addResourcesMap(mapDescriptor);
    }
    if (!mapDescriptor.hasItem(itemId))
    {
      Item item=new Item();
      item.setIdentifier(itemId);
      item.setName(itemName);
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
      LOGGER.info("Wrote resources maps file: "+GeneratedFiles.RESOURCES_MAPS);
    }
  }
}
