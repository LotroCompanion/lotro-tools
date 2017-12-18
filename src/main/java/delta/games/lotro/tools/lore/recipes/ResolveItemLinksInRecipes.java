package delta.games.lotro.tools.lore.recipes;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.common.utils.NumericTools;
import delta.common.utils.files.filter.ExtensionPredicate;
import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.lore.crafting.recipes.CraftingResult;
import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.ItemReference;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeVersion;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.lore.items.ItemsResolver;

/**
 * Resolve items referenced in recipe files.
 * @author DAM
 */
public class ResolveItemLinksInRecipes
{
  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new ResolveItemLinksInRecipes().doIt();
  }

  private void doIt()
  {
    ItemsResolver resolver=new ItemsResolver();
    handleRecipes(resolver);
  }

  private void handleRecipes(ItemsResolver resolver)
  {
    // Load recipes
    RecipesManager rMgr=RecipesManager.getInstance();
    File recipesDir=LotroCoreConfig.getInstance().getRecipesDir();
    FileFilter fileFilter=new ExtensionPredicate("xml");
    File[] recipeFiles=recipesDir.listFiles(fileFilter);
    if (recipeFiles!=null)
    {
      Set<String> missingKeys=new HashSet<String>();
      for(File recipeFile : recipeFiles)
      {
        String idStr=recipeFile.getName();
        idStr=idStr.substring(0,idStr.length()-4);
        int id=NumericTools.parseInt(idStr,-1);
        if (id!=-1)
        {
          Recipe recipe=rMgr.getRecipe(Integer.valueOf(id));
          List<Ingredient> ingredients=recipe.getIngredients();
          for(Ingredient ingredient : ingredients)
          {
            ItemReference itemRef=ingredient.getItem();
            handleItemRef(resolver,missingKeys,itemRef);
          }
          /*
          ItemReference scroll=recipe.getRecipeScroll();
          if (scroll!=null)
          {
            handleItemRef(ids,missingKeys,scroll);
          }
          */
          List<RecipeVersion> versions=recipe.getVersions();
          for(RecipeVersion version : versions)
          {
            CraftingResult regular=version.getRegular();
            if (regular!=null)
            {
              ItemReference ref=regular.getItem();
              handleItemRef(resolver,missingKeys,ref);
            }
            CraftingResult critical=version.getCritical();
            if (critical!=null)
            {
              ItemReference ref=critical.getItem();
              handleItemRef(resolver,missingKeys,ref);
            }
          }
        }
      }
      
      List<String> sortedKeys=new ArrayList<String>(missingKeys);
      Collections.sort(sortedKeys);
      for(String missingKey : sortedKeys)
      {
        System.out.println("Missing : "+missingKey);
      }
      System.out.println("Missing : "+sortedKeys.size());
    }
  }

  private void handleItemRef(ItemsResolver resolver, Set<String> missingKeys, ItemReference itemRef)
  {
    Item item=resolver.resolveLorebookKey(itemRef.getItemKey());
    if (item!=null)
    {
      itemRef.setItemId(item.getIdentifier());
    }
    else
    {
      missingKeys.add(itemRef.getItemKey());
    }
  }
}
