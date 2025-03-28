package delta.games.lotro.tools.extraction.crafting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOGGER=LoggerFactory.getLogger(RecipeItemsLoader.class);

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
            LOGGER.warn("Found multiple recipe items for recipe: {}",recipe);
          }
          _unknownRecipes.remove(recipeKey);
          recipe.setRecipeScroll(item);
        }
      }
    }
    LOGGER.info("Found {} items that hold a recipe!",Integer.valueOf(_recipe2RecipeItemMap.size()));
    LOGGER.info("There are {} recipes.",Integer.valueOf(recipesManager.getRecipesCount()));
    Set<Integer> autobestowed=getAutobestowedRecipes();
    LOGGER.info("There are {} autobestowed recipes.",Integer.valueOf(autobestowed.size()));
    _unknownRecipes.removeAll(autobestowed);
    if (!_unknownRecipes.isEmpty())
    {
      LOGGER.warn("There are {} recipes with unknown source:",Integer.valueOf(_unknownRecipes.size()));
      for(Integer unknownRecipeId : _unknownRecipes)
      {
        Recipe unknownRecipe=recipesManager.getRecipeById(unknownRecipeId.intValue());
        LOGGER.warn("\t{}\t{}",unknownRecipe,Integer.valueOf(unknownRecipe.getTier()));
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
