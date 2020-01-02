package delta.games.lotro.tools.dat.crafting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.recipes.CraftingResult;
import delta.games.lotro.lore.crafting.recipes.Ingredient;
import delta.games.lotro.lore.crafting.recipes.Recipe;
import delta.games.lotro.lore.crafting.recipes.RecipeVersion;
import delta.games.lotro.lore.crafting.recipes.RecipesManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemProxy;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Get recipe definitions from DAT files.
 * @author DAM
 */
public class MainDatRecipesLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatRecipesLoader.class);

  private static final int CRAFTING_UI_CATEGORY=0x23000065;

  private DataFacade _facade;
  private ItemsManager _itemsManager;
  private Map<Integer,Integer> _xpMapping;
  private Map<Integer,Float> _cooldownMapping;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatRecipesLoader(DataFacade facade)
  {
    _facade=facade;
    _itemsManager=ItemsManager.getInstance();
  }

  private Recipe load(int indexDataId)
  {
    Recipe recipe=null;
    int dbPropertiesId=indexDataId+DATConstants.DBPROPERTIES_OFFSET;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println(properties.dump());
      recipe=new Recipe();
      // ID
      recipe.setIdentifier(indexDataId);
      // Name
      String name=getStringProperty(properties,"CraftRecipe_Name");
      name=StringUtils.fixName(name);
      recipe.setName(name);
      // Category
      Integer categoryIndex=(Integer)properties.getProperty("CraftRecipe_UICategory");
      if (categoryIndex!=null)
      {
        String category=getCategory(categoryIndex.intValue());
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
            Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_ResultIcon");
            newVersion.getRegular().setItem(buildItemProxy(resultId.intValue(),iconId));
          }
          // - critical result
          Integer critResultId=(Integer)outputProps.getProperty("CraftRecipe_CriticalResultItem");
          if ((critResultId!=null) && (critResultId.intValue()>0))
          {
            CraftingResult critical=newVersion.getCritical();
            Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_CritResultIcon");
            critical.setItem(buildItemProxy(critResultId.intValue(),iconId));
          }
          // Ingredient
          Integer ingredientId=(Integer)outputProps.getProperty("CraftRecipe_Ingredient");
          if (ingredientId!=null)
          {
            newVersion.getIngredients().get(0).setItem(buildItemProxy(ingredientId.intValue()));
          }
          recipe.getVersions().add(newVersion);
        }
      }

      // Profession
      Integer professionId=(Integer)properties.getProperty("CraftRecipe_Profession");
      if (professionId!=null)
      {
        Profession profession=getProfessionFromProfessionId(professionId.intValue());
        recipe.setProfession(profession.getName());
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
        name=recipe.getVersions().get(0).getRegular().getItem().getName();
        recipe.setName(name);
      }
      Integer guild=(Integer)properties.getProperty("CraftRecipe_RequiredCraftGuild");
      if ((guild!=null) && (guild.intValue()!=0))
      {
        recipe.setGuildRequired(true);
      }
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
        Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_ResultIcon");
        ItemProxy proxy=buildItemProxy(resultId.intValue(),iconId);
        regular.setItem(proxy);
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
      Integer iconId=(Integer)properties.getProperty("CraftRecipe_Field_CritResultIcon");
      ItemProxy proxy=buildItemProxy(criticalResultId.intValue(),iconId);
      criticalResult.setItem(proxy);
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
    Item item=_itemsManager.getItem(id);
    ItemProxy proxy=new ItemProxy();
    proxy.setId(id);
    if (item==null)
    {
      item=resolveItem(id);
    }
    proxy.setItem(item);
    return proxy;
  }

  private ItemProxy buildItemProxy(int id, Integer iconId)
  {
    ItemProxy proxy=buildItemProxy(id);
    if (proxy.getIcon()==null)
    {
      if (iconId!=null)
      {
        resolveIcon(iconId.intValue());
        proxy.getItem().setIcon(iconId.toString());
      }
    }
    return proxy;
  }

  private Item resolveItem(int itemId)
  {
    PropertiesSet properties=_facade.loadProperties(itemId+DATConstants.DBPROPERTIES_OFFSET);
    String name=DatUtils.getStringProperty(properties,"Name");
    name=StringUtils.fixName(name);
    Item item=new Item();
    item.setIdentifier(itemId);
    item.setName(name);
    return item;
  }

  private void resolveIcon(int iconId)
  {
    File iconFile=new File(GeneratedFiles.ITEM_ICONS_DIR,iconId+".png").getAbsoluteFile();
    if (!iconFile.exists())
    {
      DatIconsUtils.buildImageFile(_facade,iconId,iconFile);
    }
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

  private String getCategory(int key)
  {
    return _facade.getEnumsManager().resolveEnum(CRAFTING_UI_CATEGORY,key);
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
    _cooldownMapping=loadCooldownMapping();
    RecipesManager recipesManager=new RecipesManager(false);
    //useIndexes(recipesManager);
    scanAll(recipesManager);
    int nbRecipes=recipesManager.getRecipesCount();
    System.out.println("Found: "+nbRecipes+" recipes.");
    boolean ok=recipesManager.writeToFile(GeneratedFiles.RECIPES);
    if (ok)
    {
      System.out.println("Wrote recipes file: "+GeneratedFiles.RECIPES);
    }
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

  private Map<Integer,Float> loadCooldownMapping()
  {
    Map<Integer,Float> ret=new HashMap<Integer,Float>();
    PropertiesSet properties=_facade.loadProperties(0x79000264);
    if (properties!=null)
    {
      Object[] array=(Object[])properties.getProperty("CooldownControl_DurationMapList");
      for(int i=0;i<array.length;i++)
      {
        PropertiesSet item=(PropertiesSet)array[i];
        Integer key=(Integer)item.getProperty("CooldownControl_DurationType");
        Float value=(Float)item.getProperty("CooldownControl_DurationValue");
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
      ItemProxy regularResultProxy=regular.getItem();
      checkProxy(context,regularResultProxy);
      // Regular
      CraftingResult critical=version.getCritical();
      if (critical!=null)
      {
        ItemProxy criticalResultProxy=critical.getItem();
        checkProxy(context,criticalResultProxy);
      }
    }
  }

  private void checkProxy(String context,ItemProxy proxy)
  {
    if (proxy==null)
    {
      System.out.println(context+": missing proxy");
      return;
    }
    String icon=proxy.getIcon();
    if (icon==null)
    {
      System.out.println(context+": missing icon");
      return;
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatRecipesLoader(facade).doIt();
    facade.dispose();
  }
}
