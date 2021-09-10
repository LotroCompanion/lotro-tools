package delta.games.lotro.tools.lore.crafting.recipes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.ContainersManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsContainer;
import delta.games.lotro.lore.items.ItemsManager;

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
    Set<Integer> foundScrolls=new HashSet<Integer>();
    ContainersManager containersMgr=ContainersManager.getInstance();
    List<Container> containers=containersMgr.getContainers();
    for(Container container : containers)
    {
      if (container instanceof ItemsContainer)
      {
        ItemsContainer itemsContainer=(ItemsContainer)container;
        Set<Integer> containedItems=itemsContainer.getItemIds();
        containedItems.retainAll(recipeScrollsIds);
        foundScrolls.addAll(containedItems);
      }
    }
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Integer scrollId : foundScrolls)
    {
      Item item=itemsMgr.getItem(scrollId.intValue());
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
