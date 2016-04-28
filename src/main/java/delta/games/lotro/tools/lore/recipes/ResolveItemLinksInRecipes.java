package delta.games.lotro.tools.lore.recipes;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.common.utils.NumericTools;
import delta.common.utils.files.filter.ExtensionPredicate;
import delta.games.lotro.LotroCoreConfig;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemPropertyNames;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.recipes.Recipe;
import delta.games.lotro.lore.recipes.Recipe.CraftingResult;
import delta.games.lotro.lore.recipes.Recipe.Ingredient;
import delta.games.lotro.lore.recipes.Recipe.ItemReference;
import delta.games.lotro.lore.recipes.Recipe.RecipeVersion;
import delta.games.lotro.lore.recipes.RecipesManager;

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
    HashMap<String,List<Integer>> ids=loadFileIds();
    handleRecipes(ids);
  }

  /**
   * Load map (keys/names)->list of item ids
   * @return a map.
   */
  private HashMap<String,List<Integer>> loadFileIds()
  {
    HashMap<String,List<Integer>> idStr2Id=new HashMap<String,List<Integer>>(); 
    ItemsManager mgr=ItemsManager.getInstance();
    mgr.loadAllItems();
    List<Item> items=mgr.getAllItems();
    for(Item item : items)
    {
      int id=item.getIdentifier();
      String key=item.getKey();
      registerMapping(idStr2Id,key,id);
      String name=item.getName();
      registerMapping(idStr2Id,name,id);
      String legacyName=item.getProperty(ItemPropertyNames.LEGACY_NAME);
      registerMapping(idStr2Id,legacyName,id);
      String oldTulkasName=item.getProperty(ItemPropertyNames.OLD_TULKAS_NAME);
      registerMapping(idStr2Id,oldTulkasName,id);
    }
    // Dump keys
    List<String> keys=new ArrayList<String>(idStr2Id.keySet());
    Collections.sort(keys);
    for(String key : keys)
    {
      List<Integer> ids=idStr2Id.get(key);
      //if (ids.size()>1)
      {
        System.out.println("*************** "+key+" ******************");
        Collections.sort(ids);
        for(Integer id : ids)
        {
          System.out.println("\t"+id);
        }
      }
    }
    return idStr2Id;
  }

  private void registerMapping(HashMap<String,List<Integer>> idStr2Id, String key, int id)
  {
    if (key!=null)
    {
      List<Integer> ids=idStr2Id.get(key);
      if (ids==null)
      {
        ids=new ArrayList<Integer>();
        idStr2Id.put(key,ids);
      }
      ids.add(Integer.valueOf(id));
    }
  }

  private void handleRecipes(HashMap<String,List<Integer>> ids)
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
            handleItemRef(ids,missingKeys,itemRef);
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
              handleItemRef(ids,missingKeys,ref);
            }
            CraftingResult critical=version.getCritical();
            if (critical!=null)
            {
              ItemReference ref=critical.getItem();
              handleItemRef(ids,missingKeys,ref);
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

  private void handleItemRef(HashMap<String,List<Integer>> ids, Set<String> missingKeys, ItemReference itemRef)
  {
    String key=itemRef.getItemKey();
    /*
    key=key.replace("'","%27");
    key=key.replace("â","%C3%A2");
    key=key.replace("ú","%C3%BA");
    key=key.replace("ó","%C3%B3");
    key=key.replace("û","%C3%BB");
    */

    List<Integer> intIds=ids.get(key);
    if (intIds!=null)
    {
      if (intIds.size()>1)
      {
        System.out.println("Warn: "+key+" : "+intIds.size()+" : "+intIds);
      }
      else
      {
        int id=intIds.get(0).intValue();
        itemRef.setItemId(id);
      }
    }
    else
    {
      if (key.startsWith("Item:"))
      {
        key=key.substring(5);
        key=key.replace("_"," ");
        intIds=ids.get(key);
        if (intIds==null)
        {
          missingKeys.add(key);
        }
      }
      //System.out.println("No item for key : "+key);
    }
  }
}
