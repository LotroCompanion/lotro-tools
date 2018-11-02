package delta.games.lotro.tools.lore.recipes.dat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.crafting.recipes.CraftingResult;
import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeVersion;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemProxy;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Get recipe definitions from DAT files.
 * @author DAM
 */
public class MainDatRecipesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRecipesLoader.class);

  // Professions...
  private static final String[] PROFESSIONS={"Cook","Farmer","Forester","Jeweller","Metalsmith","Prospector","Scholar","Tailor","Weaponsmith","Woodworker"};
  private static final int[] INDEX={0,0,0,0,0,0,0,0x79001C75,0,0};

  /**
   * Constructor.
   */
  public MainDatRecipesLoader()
  {
  }

  private Map<Integer,List<Integer>> loadProfessionIndex(DataFacade facade, int indexDataId)
  {
    Map<Integer,List<Integer>> ret=new HashMap<Integer,List<Integer>>();
    PropertiesSet properties=facade.loadProperties(indexDataId);
    if (properties!=null)
    {
      Object[] tiersPropertiesGen=(Object[])properties.getProperty("CraftProfession_TierArray");
      for(Object tierPropertiesGen : tiersPropertiesGen)
      {
        PropertiesSet tierProperties=(PropertiesSet)tierPropertiesGen;
        Integer tier=(Integer)tierProperties.getProperty("CraftProfession_Tier");
        Object[] recipeDataIdsGen=(Object[])tierProperties.getProperty("CraftProfession_RecipeArray");
        if ((tier!=null) && (recipeDataIdsGen!=null))
        {
          List<Integer> recipeIds=new ArrayList<Integer>();
          for(Object recipeDataIdGen : recipeDataIdsGen)
          {
            recipeIds.add((Integer)recipeDataIdGen);
          }
          ret.put(tier,recipeIds);
        }
      }
    }
    return ret;
  }

  private Recipe load(DataFacade facade, int indexDataId)
  {
    Recipe recipe=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println(properties.dump());
      recipe=new Recipe();
      // ID
      recipe.setIdentifier(indexDataId);
      // Name
      String name=getStringProperty(properties,"CraftRecipe_Name");
      recipe.setName(name);
      // Category
      //recipe.setCategory(category);
      // XP
      // Ingredients
      List<Ingredient> ingredients=getIngredientsList(properties,"CraftRecipe_IngredientList",false);
      // Optional ingredients
      List<Ingredient> optionalIngredients=getIngredientsList(properties,"CraftRecipe_OptionalIngredientList",true);
      recipe.getIngredients().addAll(ingredients);
      recipe.getIngredients().addAll(optionalIngredients);
      // Results
      RecipeVersion firstResult=buildResult(properties);
      recipe.getVersions().add(firstResult);
      // Multiple output results
      Object[] multiOutput=(Object[])properties.getProperty("CraftRecipe_MultiOutputArray");
      if (multiOutput!=null)
      {
        for(Object output : multiOutput)
        {
          PropertiesSet outputProps=(PropertiesSet)output;
          RecipeVersion otherResult=buildResult(outputProps);
          recipe.getVersions().add(otherResult);
        }
      }

      // Fixes
      if (name==null)
      {
        name=recipe.getVersions().get(0).getRegular().getItem().getName();
        recipe.setName(name);
      }
    }
    else
    {
      LOGGER.warn("Could not handle recipe ID="+indexDataId);
    }
    return recipe;
  }

  private List<Ingredient> getIngredientsList(PropertiesSet properties, String propertyName, boolean optional)
  {
    List<Ingredient> ret=new ArrayList<Ingredient>();
    Object[] ingredientsGen=(Object[])properties.getProperty(propertyName);
    if (ingredientsGen!=null)
    {
      for(Object ingredientGen : ingredientsGen)
      {
        PropertiesSet ingredientProperties=(PropertiesSet)ingredientGen;
        // ID
        Integer ingredientId=(Integer)ingredientProperties.getProperty("CraftRecipe_Ingredient");
        // Quantity
        Integer quantity=(Integer)ingredientProperties.getProperty("CraftRecipe_IngredientQuantity");
        Ingredient ingredient=new Ingredient();
        if (quantity!=null)
        {
          ingredient.setQuantity(quantity.intValue());
        }
        // Build item proxy
        ItemProxy ingredientProxy=buildItemProxy(ingredientId.intValue());
        ingredient.setItem(ingredientProxy);
        // Optionals
        ingredient.setOptional(optional);
        if (optional)
        {
          Float critBonus=(Float)ingredientProperties.getProperty("CraftRecipe_IngredientCritBonus");
          if (critBonus!=null)
          {
            ingredient.setCriticalChanceBonus(Integer.valueOf((int)(critBonus.floatValue()*100)));
          }
        }
        ret.add(ingredient);
      }
    }
    return ret;
  }

  private RecipeVersion buildResult(PropertiesSet properties)
  {
    RecipeVersion version=new RecipeVersion();
    // Regular result
    CraftingResult regular=new CraftingResult();
    {
      Integer resultId=(Integer)properties.getProperty("CraftRecipe_ResultItem");
      if (resultId!=null)
      {
        // Item
        regular.setItem(buildItemProxy(resultId.intValue()));
        // Quantity
        Integer quantity=(Integer)properties.getProperty("CraftRecipe_ResultItemQuantity");
        if (quantity!=null)
        {
          regular.setQuantity(quantity.intValue());
        }
      }
    }
    version.setRegular(regular);
    // Critical result
    CraftingResult criticalResult=null;
    Integer criticalResultId=(Integer)properties.getProperty("CraftRecipe_CriticalResultItem");
    if ((criticalResultId!=null) && (criticalResultId.intValue()>0))
    {
      criticalResult=new CraftingResult();
      criticalResult.setCriticalResult(true);
      // Item
      criticalResult.setItem(buildItemProxy(criticalResultId.intValue()));
      // Quantity
      Integer quantity=(Integer)properties.getProperty("CraftRecipe_CriticalResultItemQuantity");
      if (quantity!=null)
      {
        criticalResult.setQuantity(quantity.intValue());
      }
      version.setCritical(criticalResult);
      // Critical success chance
      Float critBonus=(Float)properties.getProperty("CraftRecipe_CriticalSuccessChance");
      if (critBonus!=null)
      {
        version.setBaseCriticalChance(Integer.valueOf((int)(critBonus.floatValue()*100)));
      }
    }
    return version;
  }

  private ItemProxy buildItemProxy(int id)
  {
    ItemsManager items=ItemsManager.getInstance();
    Item item=items.getItem(id);
    ItemProxy proxy=new ItemProxy();
    proxy.setItem(item);
    return proxy;
  }

  private String getStringProperty(PropertiesSet properties, String propertyName)
  {
    String ret=null;
    Object value=properties.getProperty(propertyName);
    if (value!=null)
    {
      if (value instanceof String[])
      {
        ret=((String[])value)[0];
      }
    }
    return ret;
  }

  private void doIt()
  {
    DataFacade facade=new DataFacade();

    RecipesManager recipesManager=new RecipesManager();
    int nbProfessions=PROFESSIONS.length;
    for(int i=0;i<nbProfessions;i++)
    {
      String profession=PROFESSIONS[i];
      // Load profession index
      int indexDataId=INDEX[i];
      if (indexDataId!=0)
      {
        Map<Integer,List<Integer>> map=loadProfessionIndex(facade,indexDataId);
        for(Integer tier : map.keySet())
        {
          List<Integer> recipedDataIds=map.get(tier);
          System.out.println("Tier "+tier+": "+recipedDataIds);
          for(Integer recipeDataId : recipedDataIds)
          {
            Recipe recipe=load(facade,recipeDataId.intValue());
            if (recipe!=null)
            {
              recipe.setProfession(profession);
              recipe.setTier(tier.intValue());
              recipesManager.registerRecipe(recipe);
            }
          }
        }
      }
    }
    int nbRecipes=recipesManager.getRecipesCount();
    System.out.println("Found: "+nbRecipes+" recipes.");
    File out=new File("../lotro-companion/data/lore/recipes_dat.xml");
    recipesManager.writeToFile(out);
    facade.dispose();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainDatRecipesLoader().doIt();
  }
}
