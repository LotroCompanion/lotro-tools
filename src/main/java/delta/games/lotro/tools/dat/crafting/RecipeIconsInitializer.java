package delta.games.lotro.tools.dat.crafting;

import java.util.List;

import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.io.xml.CraftingXMLWriter;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Tool to setup recipe icons.
 * @author DAM
 */
public class RecipeIconsInitializer
{
  /**
   * Setup recipe icons.
   * @param crafting Crafting data to update.
   */
  public static void setupRecipeIcons(CraftingData crafting)
  {
    Professions professionsRegistry=crafting.getProfessionsRegistry();
    List<Profession> professions=professionsRegistry.getAll();
    RecipesManager recipesMgr=RecipesManager.getInstance();
    for(Profession profession : professions)
    {
      for(CraftingLevel level : profession.getLevels())
      {
        int tier=level.getTier();
        String iconId=getDefaultIcon(profession.getKey(),tier);
        if (iconId.isEmpty())
        {
          List<Recipe> recipes=recipesMgr.getRecipes(profession,tier);
          for(Recipe recipe : recipes)
          {
            Item scroll=recipe.getRecipeScroll();
            if (scroll!=null)
            {
              iconId=scroll.getIcon();
              break;
            }
          }
        }
        level.setIcon(iconId);
      }
    }
    // Save
    CraftingXMLWriter.write(GeneratedFiles.CRAFTING_DATA,crafting);
  }

  private static String getDefaultIcon(String key, int tier)
  {
    boolean isLive=LotroCoreConfig.isLive();
    if (isLive)
    {
      return getDefaultIconLive(key,tier);
    }
    return getDefaultIconSoA(key);
  }

  private static String getDefaultIconLive(String key, int tier)
  {
    if ("FARMER".equals(key))
    {
      if ((tier==10) || (tier==11) || (tier==14) || (tier==15)) return "1091804717-1090522692";
    }
    else if ("FORESTER".equals(key))
    {
      if ((tier>=10) && (tier<=15)) return "1091804719-1090522692";
    }
    else if ("PROSPECTOR".equals(key))
    {
      if ((tier>=10) && (tier<=15)) return "1091804728-1090522692";
    }
    else if ("JEWELLER".equals(key))
    {
      if (tier==9)
      {
        return "1091804721-1090522692";
      }
    }
    return "";
  }

  private static String getDefaultIconSoA(String key)
  {
    if ("FORESTER".equals(key))
    {
      return "1091388632-1090522692-1091388623";
    }
    else if ("PROSPECTOR".equals(key))
    {
      return "1090536809-1090522692-1090536810";
    }
    return "";
  }
}
