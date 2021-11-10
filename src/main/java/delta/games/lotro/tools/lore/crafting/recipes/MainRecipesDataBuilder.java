package delta.games.lotro.tools.lore.crafting.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.ContainersManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.containers.ItemsContainer;
import delta.games.lotro.lore.items.containers.LootTables;
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
    // TODO Quest rewards

    Set<Integer> scrollsNotFound=new HashSet<Integer>(recipeScrollsIds);
    scrollsNotFound.removeAll(foundInContainers);
    scrollsNotFound.removeAll(sold);
    scrollsNotFound.removeAll(bartered);
    System.out.println("Scrolls not found:");
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
        if (containedItems.size()>0)
        {
          scrollContainers.add(Integer.valueOf(container.getIdentifier()));
        }
        foundScrolls.addAll(containedItems);
      }
    }
    System.out.println("Scrolls found in containers:");
    showList(foundScrolls);
    System.out.println("Scroll containers:");
    showList(scrollContainers);
    return foundScrolls;
  }

  private Set<Integer> findInVendors(Set<Integer> recipeScrollsIds)
  {
    Set<Integer> soldItems=findSoldItems();
    soldItems.retainAll(recipeScrollsIds);
    System.out.println("Sold scrolls:");
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
    System.out.println("Bartered scrolls:");
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
          int itemId=itemEntry.getItemProxy().getId();
          itemIds.add(Integer.valueOf(itemId));
        }
      }
    }
    return itemIds;
  }

  private void showList(Set<Integer> ids)
  {
    System.out.println("Nb items: "+ids.size());
    ItemsManager itemsMgr=ItemsManager.getInstance();
    List<Integer> sortedIds=new ArrayList<Integer>(ids);
    Collections.sort(sortedIds);
    for(Integer id : sortedIds)
    {
      Item item=itemsMgr.getItem(id.intValue());
      System.out.println(item);
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
