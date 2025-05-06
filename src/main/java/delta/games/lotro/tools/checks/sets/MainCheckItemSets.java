package delta.games.lotro.tools.checks.sets;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.math.Range;
import delta.games.lotro.common.stats.StatUtils;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.common.stats.StatsProviderEntry;
import delta.games.lotro.lore.items.EquipmentLocation;
import delta.games.lotro.lore.items.EquipmentLocations;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.scaling.Munging;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSetsManager;
import delta.games.lotro.lore.items.sets.ItemsSetsUtils;
import delta.games.lotro.lore.items.sets.SetBonus;
import delta.games.lotro.lore.items.sets.ItemsSet.SetType;

/**
 * Simple test class to show the bonuses of item sets.
 * @author DAM
 */
public class MainCheckItemSets
{
  private PrintStream _out=System.out; // NOSONAR

  private void doIt()
  {
    displaySets();
    checkItemsThatBelongToSeveralSets();
    // => no item belong to more than 1 set
    checkItemsInSetAreUnique();
    // => lots of non unique set members
  }

  void checkItemsThatBelongToSeveralSets()
  {
    _out.println("Check for items that belong to several sets:");
    // Gather data
    Map<Integer,List<ItemsSet>> mapByItemId=new HashMap<Integer,List<ItemsSet>>();
    ItemsSetsManager mgr=ItemsSetsManager.getInstance();
    List<ItemsSet> itemsSets=mgr.getAll();
    for(ItemsSet itemsSet : itemsSets)
    {
      // Filter
      SetType type=itemsSet.getSetType();
      if (type==SetType.TRACERIES)
      {
        continue;
      }
      mapItemsForSet(itemsSet,mapByItemId);
    }
    // Check
    for(Map.Entry<Integer,List<ItemsSet>> entry : mapByItemId.entrySet())
    {
      List<ItemsSet> sets=entry.getValue();
      if (sets.size()>1)
      {
        int itemId=entry.getKey().intValue();
        Item item=ItemsManager.getInstance().getItem(itemId);
        _out.println("Item: "+item+" is found in severals sets:");
        for(ItemsSet set : sets)
        {
          _out.println("\t"+set.getIdentifier()+" - "+set.getName());
        }
      }
    }
  }

  private void mapItemsForSet(ItemsSet itemsSet, Map<Integer,List<ItemsSet>> mapByItemId)
  {
    List<Item> members=itemsSet.getMembers();
    for(Item member : members)
    {
      Integer key=Integer.valueOf(member.getIdentifier());
      List<ItemsSet> sets=mapByItemId.get(key);
      if (sets==null)
      {
        sets=new ArrayList<ItemsSet>();
        mapByItemId.put(key,sets);
      }
      sets.add(itemsSet);
    }
  }

  void displaySets()
  {
    _out.println("Sets:");
    // Gather data
    ItemsSetsManager mgr=ItemsSetsManager.getInstance();
    List<ItemsSet> itemsSets=mgr.getAll();
    for(ItemsSet itemsSet : itemsSets)
    {
      // Filter
      boolean average=itemsSet.useAverageItemLevelForSetLevel();
      if (!average)
      {
        continue;
      }
      SetType type=itemsSet.getSetType();
      if (type==SetType.TRACERIES)
      {
        continue;
      }
      displayItemLevelSet(itemsSet);
    }
  }

  void displayItemLevelSet(ItemsSet itemsSet)
  {
    _out.println("Items set: "+itemsSet.getName());
    int level=itemsSet.getSetLevel();
    _out.println("\tSet level="+level);
    List<Item> members=itemsSet.getMembers();
    _out.println("\tMembers:");
    for(Item member : members)
    {
      Munging munging=member.getMunging();
      _out.println("\t\t"+member+" => "+munging);
    }
    if (ItemsSetsUtils.hasMultipleItemLevels(itemsSet))
    {
      Range itemLevelRange=ItemsSetsUtils.findItemLevelRange(itemsSet);
      _out.println("Range: "+itemLevelRange);
    }
    for(SetBonus bonusSet : itemsSet.getBonuses())
    {
      // Count
      int count=bonusSet.getPiecesCount();
      _out.println("\tCount="+count);
      // Stats provider
      StatsProvider statsProvider=bonusSet.getStatsProvider();
      int nbEntries=statsProvider.getEntriesCount();
      for(int i=0;i<nbEntries;i++)
      {
        StatsProviderEntry entry=statsProvider.getEntry(i);
        _out.println("\t\t"+entry);
      }
      // Stats
      List<String> lines=StatUtils.getFullStatsForDisplay(statsProvider,level);
      for(String line : lines)
      {
        _out.println("\t\t"+line);
      }
    }
  }

  void checkItemsInSetAreUnique()
  {
    _out.println("Check unicity of items:");
    // Gather data
    ItemsSetsManager mgr=ItemsSetsManager.getInstance();
    List<ItemsSet> itemsSets=mgr.getAll();
    for(ItemsSet itemsSet : itemsSets)
    {
      // Filter
      SetType type=itemsSet.getSetType();
      if (type==SetType.TRACERIES)
      {
        continue;
      }
      for(Item member : itemsSet.getMembers())
      {
        boolean isMultipleSlot=isMultipleSlot(member.getEquipmentLocation());
        if (isMultipleSlot)
        {
          boolean isUnique=member.isUnique();
          if (!isUnique)
          {
            _out.println("Set "+itemsSet.getIdentifier()+" - "+itemsSet.getName()+": "+member+" is not unique");
          }
        }
      }
    }
  }

  private boolean isMultipleSlot(EquipmentLocation location)
  {
    return ((location==EquipmentLocations.FINGER)||(location==EquipmentLocations.EAR)||(location==EquipmentLocations.WRIST));
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainCheckItemSets().doIt();
  }
}
