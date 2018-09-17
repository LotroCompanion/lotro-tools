package delta.games.lotro.tools.lore.recipes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeUtils;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.ItemProxy;

/**
 * Merge legacy and LUA recipes into a single recipes database.
 * @author DAM
 */
public class MainMergeLegacyAndLuaRecipes
{
  private RecipesManager _legacyRecipes;
  private RecipesManager _luaRecipes;
  private Map<String,Integer> _names2ids;
  private int _nbPureLegacy;
  private int _nbPureLua;
  private int _nbMerges;

  private void doIt()
  {
    loadRecipes();
    RecipesManager merged=mergeRecipes();
    File mergedFile=new File("data/recipes/mergedRecipes.xml");
    merged.writeToFile(mergedFile);
  }

  private void loadRecipes()
  {
    // Legacy
    _legacyRecipes=new RecipesManager();
    File legacyFile=new File("data/recipes/resolvedLegacyRecipes.xml");
    _legacyRecipes.loadRecipesFromFile(legacyFile);
    System.out.println("Loaded "+_legacyRecipes.getRecipesCount()+" legacy recipes.");
    // LUA
    _luaRecipes=new RecipesManager();
    File luaFile=new File("data/recipes/resolvedLuaRecipes.xml");
    _luaRecipes.loadRecipesFromFile(luaFile);
    System.out.println("Loaded "+_luaRecipes.getRecipesCount()+" LUA recipes.");
    // Load map name->id from legacy items
    _names2ids=loadItemsMaps();
  }

  private Map<String,Integer> loadItemsMaps()
  {
    Map<String,Integer> ret=new HashMap<String,Integer>();
    List<Recipe> recipes=_legacyRecipes.getAll();
    for(Recipe recipe : recipes)
    {
      List<Ingredient> ingredients=recipe.getIngredients();
      for(Ingredient ingredient : ingredients)
      {
        int id=ingredient.getItem().getId();
        if (id!=0)
        {
          String name=ingredient.getName();
          Integer oldId=ret.get(name);
          if (oldId!=null)
          {
            if (oldId.intValue()!=id)
            {
              System.out.println("ID ambiguity for "+name+": "+oldId+", "+id);
            }
          }
          ret.put(name,Integer.valueOf(id));
        }
      }
    }
    return ret;
  }

  private RecipesManager mergeRecipes()
  {
    RecipesManager ret=new RecipesManager();
    List<String> professions=_legacyRecipes.getProfessions();
    for(String profession : professions)
    {
      Set<Integer> tiers=new HashSet<Integer>();
      tiers.addAll(_legacyRecipes.getTiers(profession));
      tiers.addAll(_luaRecipes.getTiers(profession));
      List<Integer> sortedTiers=new ArrayList<Integer>(tiers);
      Collections.sort(sortedTiers);
      for(Integer tier : sortedTiers)
      {
        List<Recipe> legacyRecipes=_legacyRecipes.getRecipes(profession,tier.intValue());
        List<Recipe> luaRecipes=_luaRecipes.getRecipes(profession,tier.intValue());
        mergeRecipes(ret,profession,tier.intValue(),legacyRecipes,luaRecipes);
      }
    }
    System.out.println("Legacy gave "+_nbPureLegacy+" recipes.");
    System.out.println("LUA gave "+_nbPureLua+" recipes.");
    System.out.println("Merged "+_nbMerges+" recipes.");
    int total=_nbPureLegacy+_nbPureLua+_nbMerges;
    System.out.println("Total "+total+" recipes.");
    return ret;
  }

  private void mergeRecipes(RecipesManager manager, String profession, int tier, List<Recipe> legacyRecipes, List<Recipe> luaRecipes)
  {
    for(Recipe legacyRecipe : legacyRecipes)
    {
      String name=legacyRecipe.getName();
      List<Recipe> foundLuaRecipes=findRecipesByName(name,luaRecipes);
      if (foundLuaRecipes.size()==0)
      {
        // Simple merge!
        manager.registerRecipe(legacyRecipe);
        _nbPureLegacy++;
      }
      else if (foundLuaRecipes.size()>=1)
      {
        Recipe luaRecipe=foundLuaRecipes.get(0);
        Recipe merged=mergeRecipes(legacyRecipe,luaRecipe);
        luaRecipes.remove(luaRecipe);
        manager.registerRecipe(merged);
        _nbMerges++;
      }
      else
      {
        System.out.println("Ambiguity on recipe: "+RecipeUtils.getContext(legacyRecipe));
      }
    }
    for(Recipe luaRecipe : luaRecipes)
    {
      manager.registerRecipe(luaRecipe);
      _nbPureLua++;
    }
  }

  /*
  private List<List<Recipe>> getRecipeByName(List<Recipe> recipes)
  {
    List<List<Recipe>> ret=new ArrayList<List<Recipe>>();
    return ret;
  }
  */

  private Recipe mergeRecipes(Recipe legacyRecipe, Recipe luaRecipe)
  {
    //String context=RecipeUtils.getContext(legacyRecipe);
    // Copy category, XP
    legacyRecipe.setCategory(luaRecipe.getCategory());
    legacyRecipe.setXP(luaRecipe.getXP());
    // Ingredients
    List<Ingredient> legacyIngredients=legacyRecipe.getIngredients();
    List<Ingredient> luaIngredients=luaRecipe.getIngredients();
    /*
    int nbLegacyIngredients=legacyIngredients.size();
    int nbLuaIngredients=luaIngredients.size();
    if (nbLegacyIngredients!=nbLuaIngredients)
    {
      System.out.println(context+": ingredients count mismatch: "+nbLegacyIngredients+"!="+nbLuaIngredients);
    }
    */
    // Trust ingredients of the LUA recipe
    legacyIngredients.clear();
    legacyIngredients.addAll(luaIngredients);
    for(Ingredient luaIngredient : luaIngredients)
    {
      ItemProxy proxy=luaIngredient.getItem();
      int id=proxy.getId();
      if (id==0)
      {
        String ingredientName=proxy.getName();
        Integer foundId=_names2ids.get(ingredientName);
        if (foundId!=null)
        {
          proxy.setId(foundId.intValue());
        }
      }
    }
    return legacyRecipe;
  }

  private List<Recipe> findRecipesByName(String name, List<Recipe> recipes)
  {
    List<Recipe> found=new ArrayList<Recipe>();
    for(Recipe recipe : recipes)
    {
      if (name.equals(recipe.getName()))
      {
        found.add(recipe);
      }
    }
    return found;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainMergeLegacyAndLuaRecipes().doIt();
  }
}
