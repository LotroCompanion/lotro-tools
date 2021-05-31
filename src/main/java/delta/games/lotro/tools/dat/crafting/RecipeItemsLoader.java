package delta.games.lotro.tools.dat.crafting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Get recipe<->recipe items links from DAT files.
 * @author DAM
 */
public class RecipeItemsLoader
{
  private static final Logger LOGGER=Logger.getLogger(RecipeItemsLoader.class);

  private DataFacade _facade;
  private Map<Integer,Item> _recipe2RecipeItemMap;
  private Set<Integer> _unknownRecipes;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public RecipeItemsLoader(DataFacade facade)
  {
    _facade=facade;
    _recipe2RecipeItemMap=new HashMap<Integer,Item>();
    _unknownRecipes=new HashSet<Integer>();
  }

  /**
   * Find recipe items for crafting recipes.
   * @param recipesManager Recipes manager to use.
   */
  public void loadRecipeItems(RecipesManager recipesManager)
  {
    ItemsManager itemsManager=ItemsManager.getInstance();
    for(Recipe recipe : recipesManager.getAll())
    {
      _unknownRecipes.add(Integer.valueOf(recipe.getIdentifier()));
    }
    for(Item item : itemsManager.getAllItems())
    {
      PropertiesSet props=_facade.loadProperties(item.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
      if (props==null)
      {
        continue;
      }
      Integer recipeId=(Integer)props.getProperty("RecipeItem_Recipe");
      if (recipeId!=null)
      {
        Recipe recipe=recipesManager.getRecipeById(recipeId.intValue());
        if (recipe!=null)
        {
          Integer recipeKey=Integer.valueOf(recipe.getIdentifier());
          Item old=_recipe2RecipeItemMap.put(recipeKey,item);
          if (old!=null)
          {
            LOGGER.warn("Found multiple recipe items for recipe: "+recipe);
          }
          _unknownRecipes.remove(recipeKey);
          //System.out.println("Recipe "+recipe+" comes from "+item);
          recipe.setRecipeScroll(item);
        }
      }
    }
    LOGGER.info("Found "+_recipe2RecipeItemMap.size()+" items that hold a recipe!");
    LOGGER.info("There are "+recipesManager.getRecipesCount()+" recipes.");
    Set<Integer> autobestowed=getAutobestowedRecipes();
    LOGGER.info("There are "+autobestowed.size()+" autobestowed recipes.");
    _unknownRecipes.removeAll(autobestowed);
    if (_unknownRecipes.size()>0)
    {
      LOGGER.warn("There are "+_unknownRecipes.size()+" recipe with unknown source:");
      for(Integer unknownRecipeId : _unknownRecipes)
      {
        Recipe unknownRecipe=recipesManager.getRecipeById(unknownRecipeId.intValue());
        LOGGER.warn("\t"+unknownRecipe+"\t"+unknownRecipe.getTier());
      }
    }
  }

  private Set<Integer> getAutobestowedRecipes()
  {
    Set<Integer> ret=new HashSet<Integer>();
    CraftingData data=CraftingSystem.getInstance().getData();
    Professions professions=data.getProfessionsRegistry();
    for(Profession profession : professions.getAll())
    {
      for(CraftingLevel level : profession.getLevels())
      {
        int[] recipes=level.getRecipes();
        for(int recipe : recipes)
        {
          ret.add(Integer.valueOf(recipe));
        }
      }
    }
    return ret;
  }
}
