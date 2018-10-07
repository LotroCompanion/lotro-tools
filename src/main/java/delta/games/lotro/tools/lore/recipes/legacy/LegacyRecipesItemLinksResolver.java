package delta.games.lotro.tools.lore.recipes.legacy;

import java.io.File;
import java.util.List;

import delta.common.utils.text.EncodingNames;
import delta.games.lotro.lore.crafting.recipes.CraftingResult;
import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeVersion;
import delta.games.lotro.lore.crafting.recipes.io.xml.RecipeXMLParser;
import delta.games.lotro.lore.crafting.recipes.io.xml.RecipeXMLWriter;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemProxy;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.finder.ItemsFinder;
import delta.games.lotro.tools.lore.items.LegacyItemsManager;
import delta.games.lotro.tools.lore.recipes.ResolutionStats;

/**
 * Resolve items referenced in legacy recipes.
 * @author DAM
 */
public class LegacyRecipesItemLinksResolver
{
  private ResolutionStats _stats;
  private LegacyItemsManager _legacyItems;

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new LegacyRecipesItemLinksResolver().doIt();
  }

  private void doIt()
  {
    _stats=new ResolutionStats();
    _legacyItems=new LegacyItemsManager();
    List<Recipe> recipes=loadRecipes();
    System.out.println("Nb recipes: "+recipes.size());
    updateRecipes(recipes);
    RecipeXMLWriter writer=new RecipeXMLWriter();
    File toFile=new File("data/recipes/resolvedLegacyRecipes.xml");
    writer.write(toFile,recipes,EncodingNames.UTF_8);
  }

  private List<Recipe> loadRecipes()
  {
    File recipesFile=new File("data/recipes/in/recipesLegacy.xml");
    RecipeXMLParser parser=new RecipeXMLParser();
    List<Recipe> recipes=parser.loadRecipes(recipesFile);
    return recipes;
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
    String iconUrl=itemRef.getIcon();
    String key=itemRef.getItemKey();

    boolean ok=false;
    // Find legacy item
    Integer id=_legacyItems.getByKeyAndIconUrl(key,iconUrl);
    if (id!=null)
    {
      itemRef.setId(id.intValue());
      ok=true;
    }
    else
    {
      //System.out.println("Legacy item not found: name="+name+", key="+key);
    }

    name=fixName(name);
    Item item=finder.resolveByName(name,null);
    if (item!=null)
    {
      itemRef.setId(item.getIdentifier());
      itemRef.setIcon(item.getIcon());
      itemRef.setItem(item);
      ok=true;
    }
    _stats.registerResolution(ok,name);
  }

  private String fixName(String name)
  {
    if ("High-quality Calenard Ingot".equals(name)) return "High-grade Calenard Ingot";
    if ("Medium-quality Calenard Ingot".equals(name)) return "Medium-grade Calenard Ingot";
    if ("Low-quality Calenard Ingot".equals(name)) return "Low-grade Calenard Ingot";
    return name;
  }
}
