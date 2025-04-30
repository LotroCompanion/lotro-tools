package delta.games.lotro.tools.extraction.items;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.cosmetics.ItemCosmetics;
import delta.games.lotro.lore.items.cosmetics.io.xml.ItemCosmeticsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;

/**
 * Loader for cosmetic details for items.
 * @author DAM
 */
public class CosmeticLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(CosmeticLoader.class);

  private Map<String,List<Item>> _map=new HashMap<String,List<Item>>();

  /**
   * Handle an item.
   * @param item Item to use.
   * @param props Properties to use.
   * @param type Item type.
   */
  public void handleItem(Item item, PropertiesSet props, int type)
  {
    // Use only IWeapon, IShield and IClothing
    if ((type!=799)&&(type!=797)&&(type!=795))
    {
      return;
    }
    Integer physObj=(Integer)props.getProperty("PhysObj");
    Object[] entryArray=(Object[])props.getProperty("Item_WornAppearanceMapList");
    if ((physObj==null) && (entryArray==null))
    {
      return;
    }
    String appearanceHash=buildCosmeticKey(physObj,entryArray);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(physObj+" => "+appearanceHash+" => "+item);
    }
    List<Item> list=_map.get(appearanceHash);
    if (list==null)
    {
      list=new ArrayList<Item>();
      _map.put(appearanceHash,list);
    }
    list.add(item);
  }

  private String buildCosmeticKey(Integer physObj, Object[] entryArray)
  {
    StringBuilder sb=new StringBuilder();
    if (physObj!=null)
    {
      sb.append(physObj).append("#");
    }
    if (entryArray!=null)
    {
      List<String> keys=new ArrayList<String>();
      for(Object entryObj : entryArray)
      {
        PropertiesSet entryProps=(PropertiesSet)entryObj;
        String entry=buildCosmeticEntry(entryProps);
        if (entry!=null)
        {
          keys.add(entry);
        }
      }
      Collections.sort(keys);
      sb.append(keys);
    }
    return sb.toString();
  }

  private String buildCosmeticEntry(PropertiesSet entryProps)
  {
    int species=((Integer)entryProps.getProperty("Item_SpeciesOfWearer")).intValue();
    int sex=((Integer)entryProps.getProperty("Item_SexOfWearer")).intValue();
    Integer key=(Integer)entryProps.getProperty("Item_AppearanceKey");
    int wornAppearance=((Integer)entryProps.getProperty("Item_WornAppearance")).intValue();
    if ((sex==8192) && (species==73))
    {
      // Ignore female dwarf
      return null;
    }
    StringBuilder sb=new StringBuilder();
    sb.append(species).append('/');
    sb.append(sex).append('/');
    sb.append(key).append('/');
    sb.append(wornAppearance);
    return sb.toString();
  }

  /**
   * Save data to file.
   */
  public void save()
  {
    ItemCosmetics cosmetics=new ItemCosmetics();
    List<String> stringIDs=new ArrayList<String>(_map.keySet());
    Collections.sort(stringIDs);
    for(String stringID : stringIDs)
    {
      List<Item> items=_map.get(stringID);
      int[] itemIDs=new int[items.size()];
      for(int i=0;i<items.size();i++)
      {
        itemIDs[i]=items.get(i).getIdentifier();
      }
      cosmetics.addEntry(itemIDs);
    }
    ItemCosmeticsXMLWriter.write(GeneratedFiles.ITEM_COSMETICS,cosmetics);
  }

  /**
   * Dump the loaded data.
   * @param out Output stream.
   */
  public void dump(PrintStream out)
  {
    List<String> keys=new ArrayList<String>(_map.keySet());
    out.println(_map.size());
    Collections.sort(keys);
    for(String key : keys)
    {
      out.println("Key: "+key);
      for(Item item : _map.get(key))
      {
        out.println("\t"+item);
      }
    }
  }
}
