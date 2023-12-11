package delta.games.lotro.tools.dat.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.enums.CraftingUICategory;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.recipes.CraftingResult;
import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.IngredientPack;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeVersion;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.misc.CooldownLoader;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;
import delta.games.lotro.tools.dat.utils.i18n.I18nUtils;

/**
 * Get recipe definitions from DAT files.
 * @author DAM
 */
public class MainDatRecipesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRecipesLoader.class);

  private DataFacade _facade;
  private ItemsManager _itemsManager;
  private Map<Integer,Integer> _xpMapping;
  private Map<Integer,Float> _cooldownMapping;
  private RecipeItemsLoader _recipeItemsLoader;
  private LotroEnum<CraftingUICategory> _category;
  private I18nUtils _i18n;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatRecipesLoader(DataFacade facade)
  {
    _facade=facade;
    _itemsManager=ItemsManager.getInstance();
    _recipeItemsLoader=new RecipeItemsLoader(_facade);
    _category=LotroEnumsRegistry.getInstance().get(CraftingUICategory.class);
    _i18n=new I18nUtils("recipes",facade.getGlobalStringsManager());
  }

  private Recipe load(int indexDataId)
  {
    Recipe recipe=null;
    long dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println(properties.dump());
      recipe=new Recipe();
      // ID
      recipe.setIdentifier(indexDataId);
      // Name
      String name=_i18n.getNameStringProperty(properties,"CraftRecipe_Name",indexDataId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
      recipe.setName(name);
      // Category
      Integer categoryIndex=(Integer)properties.getProperty("CraftRecipe_UICategory");
      if (categoryIndex!=null)
      {
        CraftingUICategory category=_category.getEntry(categoryIndex.intValue());
        recipe.setCategory(category);
      }
      // XP
      Integer xpId=(Integer)properties.getProperty("CraftRecipe_XPReward");
      if (xpId!=null)
      {
        Integer xpValue=_xpMapping.get(xpId);
        if (xpValue!=null)
        {
          recipe.setXP(xpValue.intValue());
        }
      }
      // Cooldown
      Integer cooldownId=(Integer)properties.getProperty("CraftRecipe_CooldownDuration");
      if (cooldownId!=null)
      {
        Float cooldownValue=_cooldownMapping.get(cooldownId);
        if (cooldownValue!=null)
        {
          recipe.setCooldown(cooldownValue.intValue());
        }
      }
      // Single use
      Integer singleUse=(Integer)properties.getProperty("CraftRecipe_OneTimeRecipe");
      if ((singleUse!=null) && (singleUse.intValue()==1))
      {
        recipe.setOneTimeUse(true);
      }
      // Ingredients
      List<Ingredient> ingredients=getIngredientsList(properties,"CraftRecipe_IngredientList",false);
      // Optional ingredients
      List<Ingredient> optionalIngredients=getIngredientsList(properties,"CraftRecipe_OptionalIngredientList",true);
      // Ingredient pack
      Integer ingredientPackID=(Integer)properties.getProperty("CraftRecipe_IngredientPack");
      if (ingredientPackID!=null)
      {
        Integer packQuantity=(Integer)properties.getProperty("CraftRecipe_IngredientPackQuantity");
        int count=(packQuantity!=null)?packQuantity.intValue():1;
        Item item=_itemsManager.getItem(ingredientPackID.intValue());
        if (item!=null)
        {
          IngredientPack ingredientPack=new IngredientPack(item,count);
          recipe.setIngredientPack(ingredientPack);
        }
      }
      // Results
      RecipeVersion firstResult=buildVersion(properties);
      firstResult.getIngredients().addAll(ingredients);
      firstResult.getIngredients().addAll(optionalIngredients);
      recipe.getVersions().add(firstResult);
      // Multiple output results
      Object[] multiOutput=(Object[])properties.getProperty("CraftRecipe_MultiOutputArray");
      if (multiOutput!=null)
      {
        for(Object output : multiOutput)
        {
          PropertiesSet outputProps=(PropertiesSet)output;
          RecipeVersion newVersion=firstResult.cloneData();

          // Patch
          // - result
          Integer resultId=(Integer)outputProps.getProperty("CraftRecipe_ResultItem");
          if ((resultId!=null) && (resultId.intValue()>0))
          {
            CraftingResult regular=newVersion.getRegular();
            Item resultItem=_itemsManager.getItem(resultId.intValue());
            regular.setItem(resultItem);
          }
          // - critical result
          Integer critResultId=(Integer)outputProps.getProperty("CraftRecipe_CriticalResultItem");
          if ((critResultId!=null) && (critResultId.intValue()>0))
          {
            CraftingResult critical=newVersion.getCritical();
            Item critResultItem=_itemsManager.getItem(critResultId.intValue());
            critical.setItem(critResultItem);
          }
          checkForItemLevels(newVersion.getRegular(),newVersion.getCritical());
          // Ingredient
          Integer ingredientId=(Integer)outputProps.getProperty("CraftRecipe_Ingredient");
          if (ingredientId!=null)
          {
            Item firstIngredient=_itemsManager.getItem(ingredientId.intValue());
            newVersion.getIngredients().get(0).setItem(firstIngredient);
          }
          recipe.getVersions().add(newVersion);
        }
      }

      // Profession
      Integer professionId=(Integer)properties.getProperty("CraftRecipe_Profession");
      if (professionId!=null)
      {
        Profession profession=getProfessionFromProfessionId(professionId.intValue());
        recipe.setProfession(profession);
      }
      // Tier
      Integer tier=getTier(properties);
      if (tier!=null)
      {
        recipe.setTier(tier.intValue());
      }
      // Fixes
      if (name==null)
      {
        name=recipe.getDefaultName();
        recipe.setName(name);
      }
      Integer guild=(Integer)properties.getProperty("CraftRecipe_RequiredCraftGuild");
      // TODO Store guild ID here (faction)
      if ((guild!=null) && (guild.intValue()!=0))
      {
        recipe.setGuildRequired(true);
      }
      // Other attributes
      // CraftRecipe_NameItemOnNormalSuccess
      // CraftRecipe_NameItemOnCriticalSuccess
      // CraftRecipe_ExecutionTime
      checkRecipe(recipe);
    }
    else
    {
      LOGGER.warn("Could not handle recipe ID="+indexDataId);
    }
    return recipe;
  }

  private Integer getTier(PropertiesSet properties)
  {
    Integer tier=(Integer)properties.getProperty("CraftRecipe_Tier");
    if (tier==null)
    {
      Object[] tierArray=(Object[])properties.getProperty("CraftRecipe_TierArray");
      if (tierArray!=null)
      {
        tier=(Integer)(tierArray[0]);
      }
    }
    return tier;
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
        Item ingredientItem=_itemsManager.getItem(ingredientId.intValue());
        ingredient.setItem(ingredientItem);
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

  private RecipeVersion buildVersion(PropertiesSet properties)
  {
    RecipeVersion version=new RecipeVersion();
    // Regular result
    CraftingResult regular=new CraftingResult();
    {
      Integer resultId=(Integer)properties.getProperty("CraftRecipe_ResultItem");
      if (resultId!=null)
      {
        // Item
        Item resultItem=_itemsManager.getItem(resultId.intValue());
        regular.setItem(resultItem);
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
      Item critResultItem=_itemsManager.getItem(criticalResultId.intValue());
      criticalResult.setItem(critResultItem);
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
    checkForItemLevels(regular,criticalResult);
    return version;
  }

  private void checkForItemLevels(CraftingResult regular, CraftingResult critical)
  {
    if (critical==null)
    {
      return;
    }
    Item regularItem=regular.getItem();
    int regularItemID=(regularItem!=null)?regularItem.getIdentifier():0;
    int regularQuantity=regular.getQuantity();
    Item critItem=critical.getItem();
    int critItemID=(critItem!=null)?critItem.getIdentifier():0;
    int critQuantity=critical.getQuantity();
    if ((regularItemID==critItemID) && (regularQuantity==critQuantity))
    {
      PropertiesSet props=_facade.loadProperties(regularItemID+DATConstants.DBPROPERTIES_OFFSET);
      Integer itemLevel=(Integer)props.getProperty("Item_Level");
      Integer craftingOffset=(Integer)props.getProperty("Item_iLevel_Crafting_Offset");
      if (craftingOffset!=null)
      {
        int regularItemLevel=itemLevel.intValue();
        regular.setItemLevel(regularItemLevel);
        int critItemLevel=itemLevel.intValue()+craftingOffset.intValue();
        critical.setItemLevel(critItemLevel);
      }
    }
  }

  private Profession getProfessionFromProfessionId(int id)
  {
    CraftingData crafting=CraftingSystem.getInstance().getData();
    Professions professions=crafting.getProfessionsRegistry();
    Profession profession=professions.getProfessionById(id);
    return profession;
  }

  /**
   * Load recipes.
   */
  public void doIt()
  {
    // XP mapping
    _xpMapping=loadXpMapping();
    // Cooldown mapping
    _cooldownMapping=CooldownLoader.doIt(_facade);
    // Load recipes
    RecipesManager recipesManager=new RecipesManager(false);
    scanAll(recipesManager);
    int nbRecipes=recipesManager.getRecipesCount();
    LOGGER.info("Found: "+nbRecipes+" recipes.");
    // Load recipe->recipe item links
    _recipeItemsLoader.loadRecipeItems(recipesManager);
    // Save
    boolean ok=recipesManager.writeToFile(GeneratedFiles.RECIPES);
    if (ok)
    {
      LOGGER.info("Wrote recipes file: "+GeneratedFiles.RECIPES);
    }
    // Labels
    _i18n.save();
  }

  private void scanAll(RecipesManager recipesManager)
  {
    for(int i=0x70000000;i<=0x77FFFFFF;i++)
    {
      byte[] data=_facade.loadData(i);
      if (data!=null)
      {
        //int did=BufferUtils.getDoubleWordAt(data,0);
        int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
        if (classDefIndex==1024)
        {
          Recipe recipe=load(i);
          if (recipe!=null)
          {
            recipesManager.registerRecipe(recipe);
          }
        }
      }
    }
  }

  private Map<Integer,Integer> loadXpMapping()
  {
    Map<Integer,Integer> ret=new HashMap<Integer,Integer>();
    PropertiesSet properties=_facade.loadProperties(0x7900021E);
    if (properties!=null)
    {
      Object[] array=(Object[])properties.getProperty("CraftControl_XPRewardArray");
      for(int i=0;i<array.length;i++)
      {
        PropertiesSet item=(PropertiesSet)array[i];
        Integer key=(Integer)item.getProperty("CraftControl_XPRewardEnum");
        Integer value=(Integer)item.getProperty("CraftControl_XPRewardValue");
        ret.put(key,value);
      }
    }
    return ret;
  }

  private void checkRecipe(Recipe recipe)
  {
    String context=recipe.getName();
    List<RecipeVersion> versions=recipe.getVersions();
    for(RecipeVersion version : versions)
    {
      // Regular
      CraftingResult regular=version.getRegular();
      Item regularResultItem=regular.getItem();
      checkItem(context,regularResultItem);
      // Regular
      CraftingResult critical=version.getCritical();
      if (critical!=null)
      {
        Item criticalResultItem=critical.getItem();
        checkItem(context,criticalResultItem);
      }
    }
  }

  private void checkItem(String context,Item item)
  {
    if (item==null)
    {
      LOGGER.error(context+": missing item");
      return;
    }
    String icon=item.getIcon();
    if (icon==null)
    {
      LOGGER.warn(context+": missing icon");
      return;
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new MainDatRecipesLoader(facade).doIt();
    facade.dispose();
  }
}
