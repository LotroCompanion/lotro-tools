package delta.games.lotro.tools.lore.cosmetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.cosmetics.ItemCosmetics;
import delta.games.lotro.lore.items.cosmetics.ItemCosmeticsManager;

/**
 * Tool to check cosmetic groups.
 * @author DAM
 */
public class MainCheckCosmetics
{
  private DataFacade _facade=new DataFacade();

  private List<Item> findItems()
  {
    List<Item> ret=new ArrayList<Item>();
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      if ("Tough Shoulder Pads".equals(item.getName()))
      {
        ret.add(item);
      }
    }
    return ret;
  }

  private Map<Integer,CosmeticGroup> groupWithCosmetics(List<Item> items)
  {
    Map<Integer,CosmeticGroup> ret=new HashMap<Integer,CosmeticGroup>();
    ItemCosmetics cosmetics=ItemCosmeticsManager.getInstance().getData();
    for(Item item : items)
    {
      Integer cosmeticsID=cosmetics.findCosmeticID(item.getIdentifier());
      if (cosmeticsID==null)
      {
        continue;
      }
      CosmeticGroup group=ret.get(cosmeticsID);
      if (group==null)
      {
        group=new CosmeticGroup(cosmeticsID.intValue());
        ret.put(cosmeticsID,group);
      }
      group.addItem(item);
    }
    return ret;
  }

  private void showResults(Map<Integer,CosmeticGroup> groups)
  {
    List<Integer> cosmeticIDs=new ArrayList<Integer>(groups.keySet());
    Collections.sort(cosmeticIDs);
    for(Integer cosmeticID : cosmeticIDs)
    {
      System.out.println("ID="+cosmeticID);
      CosmeticGroup group=groups.get(cosmeticID);
      List<Item> items=group.getItems();
      Collections.sort(items,new IdentifiableComparator<Item>());
      boolean first=true;
      for(Item item : items)
      {
        if (first)
        {
          showProps(item);
          first=false;
        }
        System.out.println("\t"+item.getIdentifier()+"\t"+item.getQuality()+"\t"+item.getItemLevel()+"\t"+item.getUsageRequirements());
      }
    }
  }

  private void showProps(Item item)
  {
    PropertiesSet props=_facade.loadProperties(item.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
    Integer physObj=(Integer)props.getProperty("PhysObj");
    Object[] entryArray=(Object[])props.getProperty("Item_WornAppearanceMapList");
    if ((physObj==null) && (entryArray==null))
    {
      return;
    }
    if (entryArray!=null)
    {
      int entryIndex=0;
      for(Object entryObj : entryArray)
      {
        System.out.println("Entry #"+entryIndex);
        PropertiesSet entryProps=(PropertiesSet)entryObj;
        Integer key=(Integer)entryProps.getProperty("Item_AppearanceKey");
        int sex=((Integer)entryProps.getProperty("Item_SexOfWearer")).intValue();
        int species=((Integer)entryProps.getProperty("Item_SpeciesOfWearer")).intValue();
        int wornAppearance=((Integer)entryProps.getProperty("Item_WornAppearance")).intValue();
        System.out.println("Index="+entryIndex+": appearance key: "+key+", sex="+sex+", species="+species+", wornAppearance="+wornAppearance);
        entryIndex++;
      }
    }
    //System.out.println(props.dump());
  }

  private void doIt()
  {
    List<Item> items=findItems();
    Map<Integer,CosmeticGroup> groups=groupWithCosmetics(items);
    showResults(groups);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCheckCosmetics().doIt();
  }
}
