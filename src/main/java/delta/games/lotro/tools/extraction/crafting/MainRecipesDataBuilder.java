package delta.games.lotro.tools.extraction.crafting;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.common.rewards.RewardsExplorer;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.ContainersManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.containers.ItemsContainer;
import delta.games.lotro.lore.items.containers.LootTables;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.trade.barter.BarterEntry;
import delta.games.lotro.lore.trade.barter.BarterEntryElement;
import delta.games.lotro.lore.trade.barter.BarterNpc;
import delta.games.lotro.lore.trade.barter.BarterersManager;
import delta.games.lotro.lore.trade.barter.ItemBarterEntryElement;
import delta.games.lotro.lore.trade.vendor.ValuedItem;
import delta.games.lotro.lore.trade.vendor.VendorNpc;
import delta.games.lotro.lore.trade.vendor.VendorsManager;

/**
 * Builder for recipes related data.
 * @author DAM
 */
public class MainRecipesDataBuilder
{
  private PrintStream _out=System.out;

  private Set<Integer> getRecipeItemIds()
  {
    Set<Integer> items=new HashSet<Integer>();
    RecipesManager recipesMgr=RecipesManager.getInstance();
    for(Recipe recipe : recipesMgr.getAll())
    {
      Item scroll=recipe.getRecipeScroll();
      if (scroll!=null)
      {
        items.add(Integer.valueOf(scroll.getIdentifier()));
      }
    }
    return items;
  }

  private void doIt()
  {
    Set<Integer> recipeScrollsIds=getRecipeItemIds();
    Set<Integer> foundInContainers=findScrollsInContainers(recipeScrollsIds);
    Set<Integer> sold=findInVendors(recipeScrollsIds);
    Set<Integer> bartered=findInBarterers(recipeScrollsIds);
    Set<Integer> questRewards=findInQuestRewards(recipeScrollsIds);
    Set<Integer> deedRewards=findInDeedRewards(recipeScrollsIds);

    Set<Integer> scrollsNotFound=new HashSet<Integer>(recipeScrollsIds);
    scrollsNotFound.removeAll(foundInContainers);
    scrollsNotFound.removeAll(sold);
    scrollsNotFound.removeAll(bartered);
    scrollsNotFound.removeAll(questRewards);
    scrollsNotFound.removeAll(deedRewards);
    _out.println("Scrolls not found:");
    showList(scrollsNotFound);
  }

  private Set<Integer> findScrollsInContainers(Set<Integer> recipeScrollsIds)
  {
    Set<Integer> foundScrolls=new HashSet<Integer>();
    Set<Integer> scrollContainers=new HashSet<Integer>();
    ContainersManager containersMgr=ContainersManager.getInstance();
    List<Container> containers=containersMgr.getContainers();
    for(Container container : containers)
    {
      if (container instanceof ItemsContainer)
      {
        ItemsContainer itemsContainer=(ItemsContainer)container;
        LootTables lootTables=itemsContainer.getLootTables();
        Set<Integer> containedItems=lootTables.getItemIds();
        containedItems.retainAll(recipeScrollsIds);
        if (!containedItems.isEmpty())
        {
          scrollContainers.add(Integer.valueOf(container.getIdentifier()));
        }
        foundScrolls.addAll(containedItems);
      }
    }
    _out.println("Scrolls found in containers:");
    showList(foundScrolls);
    _out.println("Scroll containers:");
    showList(scrollContainers);
    return foundScrolls;
  }

  private Set<Integer> findInVendors(Set<Integer> recipeScrollsIds)
  {
    Set<Integer> soldItems=findSoldItems();
    soldItems.retainAll(recipeScrollsIds);
    _out.println("Sold scrolls:");
    showList(soldItems);
    return soldItems;
  }

  private Set<Integer> findSoldItems()
  {
    Set<Integer> itemIds=new HashSet<Integer>();
    VendorsManager vendorsMgr=VendorsManager.getInstance();
    List<VendorNpc> vendorNpcs=vendorsMgr.getAll();
    for(VendorNpc vendor : vendorNpcs)
    {
      List<ValuedItem> soldItems=vendor.getItemsList();
      for(ValuedItem soldItem : soldItems)
      {
        itemIds.add(Integer.valueOf(soldItem.getId()));
      }
    }
    return itemIds;
  }

  private Set<Integer> findInBarterers(Set<Integer> recipeScrollsIds)
  {
    Set<Integer> barteredItems=findBarteredItems();
    barteredItems.retainAll(recipeScrollsIds);
    _out.println("Bartered scrolls:");
    showList(barteredItems);
    return barteredItems;
  }

  private Set<Integer> findBarteredItems()
  {
    Set<Integer> itemIds=new HashSet<Integer>();
    BarterersManager barterersMgr=BarterersManager.getInstance();
    List<BarterNpc> barterNpcs=barterersMgr.getAll();
    for(BarterNpc barterer : barterNpcs)
    {
      List<BarterEntry> entries=barterer.getEntries();
      for(BarterEntry entry : entries)
      {
        BarterEntryElement toReceive=entry.getElementToReceive();
        if (toReceive instanceof ItemBarterEntryElement)
        {
          ItemBarterEntryElement itemEntry=(ItemBarterEntryElement)toReceive;
          int itemId=itemEntry.getItem().getIdentifier();
          itemIds.add(Integer.valueOf(itemId));
        }
      }
    }
    return itemIds;
  }

  private Set<Integer> findInQuestRewards(Set<Integer> recipeScrollsIds)
  {
    List<QuestDescription> quests=QuestsManager.getInstance().getAll();
    Set<Integer> rewardItemIDs=findRewardItems(quests);
    rewardItemIDs.retainAll(recipeScrollsIds);
    _out.println("Quest rewards:");
    showList(rewardItemIDs);
    return rewardItemIDs;
  }

  private Set<Integer> findInDeedRewards(Set<Integer> recipeScrollsIds)
  {
    List<DeedDescription> deeds=DeedsManager.getInstance().getAll();
    Set<Integer> rewardItemIDs=findRewardItems(deeds);
    rewardItemIDs.retainAll(recipeScrollsIds);
    _out.println("Deed rewards:");
    showList(rewardItemIDs);
    return rewardItemIDs;
  }

  private Set<Integer> findRewardItems(List<? extends Achievable> achievables)
  {
    RewardsExplorer rewardsExplorer=new RewardsExplorer();
    for(Achievable achievable : achievables)
    {
      rewardsExplorer.doIt(achievable.getRewards());
    }
    rewardsExplorer.resolveProxies();
    List<Item> items=rewardsExplorer.getItems();
    Set<Integer> itemIds=new HashSet<Integer>();
    for(Item item : items)
    {
      itemIds.add(Integer.valueOf(item.getIdentifier()));
    }
    return itemIds;
  }

  private void showList(Set<Integer> ids)
  {
    _out.println("Nb items: "+ids.size());
    ItemsManager itemsMgr=ItemsManager.getInstance();
    List<Integer> sortedIds=new ArrayList<Integer>(ids);
    Collections.sort(sortedIds);
    for(Integer id : sortedIds)
    {
      Item item=itemsMgr.getItem(id.intValue());
      _out.println(item);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainRecipesDataBuilder().doIt();
  }
}
