package delta.games.lotro.tools.lore.recipes.lua;

import java.io.File;
import java.util.List;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.lore.crafting.recipes.CraftingResult;
import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeUtils;
import delta.games.lotro.lore.crafting.recipes.RecipeVersion;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.crafting.recipes.io.xml.RecipeXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemProxy;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.finder.ItemsFinder;
import delta.games.lotro.tools.lore.recipes.ResolutionStats;

/**
 * Resolve items referenced in LUA recipes.
 * @author DAM
 */
public class LuaRecipesItemLinksResolver
{
  private ResolutionStats _stats;

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LuaRecipesItemLinksResolver().doIt();
  }

  private void doIt()
  {
    _stats=new ResolutionStats();
    RecipesManager manager=new RecipesManager();
    File inputFile=new File("data/recipes/in/luaRecipes.xml");
    manager.loadRecipesFromFile(inputFile);
    List<Recipe> recipes=manager.getAll();
    RecipeUtils.sort(recipes);
    System.out.println("Nb recipes: "+recipes.size());
    updateRecipes(recipes);
    RecipeXMLWriter writer=new RecipeXMLWriter();
    File toFile=new File("data/recipes/resolvedLuaRecipes.xml");
    writer.write(toFile,recipes,EncodingNames.UTF_8);
  }

  private void updateRecipes(List<Recipe> recipes)
  {
    ItemsFinder finder=ItemsManager.getInstance().getFinder();

    for(Recipe recipe : recipes)
    {
      _stats.startItem();
      List<Ingredient> ingredients=recipe.getIngredients();
      for(Ingredient ingredient : ingredients)
      {
        ItemProxy itemRef=ingredient.getItem();
        handleItemRef(finder,itemRef);
      }
      ItemProxy scroll=recipe.getRecipeScroll();
      if (scroll!=null)
      {
        //handleItemRef(finder,scroll);
      }
      List<RecipeVersion> versions=recipe.getVersions();
      for(RecipeVersion version : versions)
      {
        CraftingResult regular=version.getRegular();
        if (regular!=null)
        {
          ItemProxy ref=regular.getItem();
          handleItemRef(finder,ref);
        }
        CraftingResult critical=version.getCritical();
        if (critical!=null)
        {
          ItemProxy ref=critical.getItem();
          handleItemRef(finder,ref);
        }
      }
      _stats.endItem();
    }
    _stats.show();
  }

  private void handleItemRef(ItemsFinder finder, ItemProxy itemRef)
  {
    String name=itemRef.getName();
    boolean ok=false;
    Item item=finder.resolveByName(name);
    if (item!=null)
    {
      itemRef.setId(item.getIdentifier());
      itemRef.setIcon(item.getIcon());
      itemRef.setItem(item);
      ok=true;
    }
    _stats.registerResolution(ok,name);
  }
}
