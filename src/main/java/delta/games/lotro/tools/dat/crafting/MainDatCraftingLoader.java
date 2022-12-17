package delta.games.lotro.tools.dat.crafting;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingLevelTier;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.Vocation;
import delta.games.lotro.lore.crafting.io.xml.CraftingXMLWriter;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.lore.titles.TitleDescription;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for combat data.
 * @author DAM
 */
public class MainDatCraftingLoader
{
  private DataFacade _facade;
  private EnumMapper _tier;
  private CraftingData _data;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatCraftingLoader(DataFacade facade)
  {
    _facade=facade;
    _tier=_facade.getEnumsManager().getEnumMapper(587202659);
    _data=new CraftingData();
  }

  /**
   * Load crafting system data.
   */
  public void doIt()
  {
    // CraftDirectory
    PropertiesSet props=_facade.loadProperties(1879048722+DATConstants.DBPROPERTIES_OFFSET);
    // - vocations
    Object[] vocationArray=(Object[])props.getProperty("CraftDirectory_VocationArray");
    for(Object vocationObj : vocationArray)
    {
      int vocationId=((Integer)vocationObj).intValue();
      handleVocation(vocationId);
    }
    // - guilds
    Object[] guildArray=(Object[])props.getProperty("CraftDirectory_CraftGuildArray");
    if (guildArray!=null)
    {
      for(Object guildObj : guildArray)
      {
        int guildId=((Integer)guildObj).intValue();
        handleGuild(guildId);
      }
    }
    // Icons
    RecipeIconsInitializer.setupRecipeIcons(_data);
    save();
  }

  private Vocation handleVocation(int vocationId)
  {
    Vocation ret=new Vocation();
    ret.setIdentifier(vocationId);
    PropertiesSet vocationProps=_facade.loadProperties(vocationId+DATConstants.DBPROPERTIES_OFFSET);
    // - name
    String name=DatUtils.getStringProperty(vocationProps,"CraftVocation_Name");
    ret.setName(name);
    // - description
    String description=DatUtils.getStringProperty(vocationProps,"CraftVocation_Description");
    ret.setDescription(description);
    // - professions
    Object[] professionsArray=(Object[])vocationProps.getProperty("CraftVocation_ProfessionArray");
    for(Object professionObj : professionsArray)
    {
      int professionId=((Integer)professionObj).intValue();
      Profession profession=handleProfession(professionId);
      ret.addProfession(profession);
    }
    // - key
    String key=getVocationKey(vocationId);
    ret.setKey(key);
    _data.getVocationsRegistry().addVocation(ret);
    return ret;
    /*
CraftVocation_ChangeString: 
  #1: Armsmen are capable of crafting any weapon you might ever need, so it's an excellent choice for anyone who relies on a blade.  An Armsman is proficient in mining and smelting, but will need to trade for wood with which to make bows, spears, and the like.  Do you wish to learn this trade?  (WEAPONSMITH, PROSPECTOR, WOODWORKER)
CraftVocation_ItemArray: 
  #1: 1879084067
  #2: 1879085802
  #3: 1879085799
  #4: 1879085795
CraftVocation_LearnString: 
  #1: Armsmen are capable of crafting any weapon you might ever need, so it's an excellent choice for anyone who relies on a blade.  An Armsman is proficient in mining and smelting, but will need to trade for wood with which to make bows, spears, and the like.  Do you wish to learn this trade?  (WEAPONSMITH, PROSPECTOR, WOODWORKER)
     */
  }

  private Profession handleProfession(int professionId)
  {
    Professions professions=_data.getProfessionsRegistry();
    Profession ret=professions.getProfessionById(professionId);
    if (ret==null)
    {
      ret=new Profession();
      PropertiesSet professionProps=_facade.loadProperties(professionId+DATConstants.DBPROPERTIES_OFFSET);
      ret.setIdentifier(professionId);
      // - name
      String name=DatUtils.getStringProperty(professionProps,"CraftProfession_Name");
      ret.setName(name);
      // - description
      String description=DatUtils.getStringProperty(professionProps,"CraftProfession_Description");
      ret.setDescription(description);
      // - tiers
      CraftingLevel beginner=buildBeginnerLevel(ret);
      ret.addLevel(beginner);
      Object[] tiersArray=(Object[])professionProps.getProperty("CraftProfession_TierArray");
      for(Object tierObj : tiersArray)
      {
        PropertiesSet tierProps=(PropertiesSet)tierObj;
        CraftingLevel level=handleProfessionTier(ret,tierProps);
        ret.addLevel(level);
      }
      // - key
      String key=getProfessionKey(professionId);
      ret.setKey(key);

      // Property names
      PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
      // - enabled
      int enabledPropertyId=((Integer)professionProps.getProperty("CraftProfession_Enabled_PropertyName")).intValue();
      String enabledPropertyName=propsRegistry.getPropertyDef(enabledPropertyId).getName();
      // - mastery level
      int masteryLevelPropertyId=((Integer)professionProps.getProperty("CraftProfession_MasteryLevel_PropertyName")).intValue();
      String masteryLevelPropertyName=propsRegistry.getPropertyDef(masteryLevelPropertyId).getName();
      // - mastery XP
      int masteryXpPropertyId=((Integer)professionProps.getProperty("CraftProfession_MasteryXP_PropertyName")).intValue();
      String masteryXpPropertyName=propsRegistry.getPropertyDef(masteryXpPropertyId).getName();
      // - proficiency level
      int proficiencyLevelPropertyId=((Integer)professionProps.getProperty("CraftProfession_ProficiencyLevel_PropertyName")).intValue();
      String proficiencyLevelPropertyName=propsRegistry.getPropertyDef(proficiencyLevelPropertyId).getName();
      // - proficiency XP
      int proficiencyXpPropertyId=((Integer)professionProps.getProperty("CraftProfession_ProficiencyXP_PropertyName")).intValue();
      String proficiencyXpPropertyName=propsRegistry.getPropertyDef(proficiencyXpPropertyId).getName();
      // Extra recipes
      int extraRecipesPropertyId=((Integer)professionProps.getProperty("CraftProfession_ExtraRecipeArray_PropertyName")).intValue();
      String extraRecipesPropertyName=propsRegistry.getPropertyDef(extraRecipesPropertyId).getName();
      ret.setPropertyNames(enabledPropertyName,masteryLevelPropertyName,masteryXpPropertyName,proficiencyLevelPropertyName,proficiencyXpPropertyName,extraRecipesPropertyName);

      // Register profession
      professions.addProfession(ret);
    }
    return ret;

    /*
CraftProfession_AllowsXPAcceleration: 1
CraftProfession_Characteristic: 0
CraftProfession_CriticalChanceAddModifier_PropertyName: 268436462 (Craft_Weaponsmith_CriticalChanceAddModifier)
CraftProfession_OpenLevel_PropertyName: 268445421 (Craft_Weaponsmith_OpenLevel)
CraftProfession_RecipeWebStoreCategory: 19 (CraftRecipe_Weaponsmith)
CraftProfession_RequiredTool: 128 (Smithing Hammer)
CraftProfession_StartTime_PropertyName: 268439959 (Craft_Weaponsmith_StartTime)
     */

  }

  private String getProfessionKey(int professionId)
  {
    if (professionId==1879054946) return "SCHOLAR";
    if (professionId==1879055079) return "METALSMITH";
    if (professionId==1879055299) return "JEWELLER";
    if (professionId==1879055477) return "TAILOR";
    if (professionId==1879055778) return "WEAPONSMITH";
    if (professionId==1879055941) return "WOODWORKER";
    if (professionId==1879061252) return "COOK";
    if (professionId==1879062816) return "FARMER";
    if (professionId==1879062818) return "PROSPECTOR";
    if (professionId==1879062817) return "FORESTER";
    return null;
  }

  private String getVocationKey(int vocationId)
  {
    if (vocationId==1879062809) return "ARMOURER";
    if (vocationId==1879062810) return "EXPLORER";
    if (vocationId==1879062811) return "ARMSMAN";
    if (vocationId==1879062812) return "TINKER";
    if (vocationId==1879062813) return "YEOMAN";
    if (vocationId==1879062814) return "WOODSMAN";
    if (vocationId==1879062815) return "HISTORIAN";
    return null;
  }

  private CraftingLevel buildBeginnerLevel(Profession profession)
  {
    CraftingLevel ret=new CraftingLevel(profession,0);
    ret.setName("Beginner");
    return ret;
  }

  private CraftingLevel handleProfessionTier(Profession profession, PropertiesSet tierProps)
  {
    //System.out.println(tierProps.dump());

    // Tier
    int tier=((Integer)tierProps.getProperty("CraftProfession_Tier")).intValue();

    CraftingLevel ret=new CraftingLevel(profession,tier);

    String tierName=_tier.getString(tier);
    ret.setName(tierName);

    // Level cap gate (no more used?)
    //int levelCapGate=((Integer)tierProps.getProperty("CraftProfession_TierLevelCapGate")).intValue();
    //System.out.println("Level cap gate: "+levelCapGate);

    // Proficiency
    {
      CraftingLevelTier proficiency=ret.getProficiency();
      int proficiencyXp=((Integer)tierProps.getProperty("CraftProfession_ProficiencyXPCap")).intValue();
      proficiency.setXP(proficiencyXp);
      int proficiencyTitleId=((Integer)tierProps.getProperty("CraftProfession_ProficiencyTitle")).intValue();
      TitleDescription proficiencyTitle=new TitleDescription();
      proficiencyTitle.setIdentifier(proficiencyTitleId);
      proficiency.setTitle(proficiencyTitle);
    }
    // Mastery
    {
      CraftingLevelTier mastery=ret.getMastery();
      int masteryXp=((Integer)tierProps.getProperty("CraftProfession_MasteryXPCap")).intValue();
      mastery.setXP(masteryXp);
      int masteryTitleId=((Integer)tierProps.getProperty("CraftProfession_MasteryTitle")).intValue();
      TitleDescription masteryTitle=new TitleDescription();
      masteryTitle.setIdentifier(masteryTitleId);
      mastery.setTitle(masteryTitle);
    }

    // Recipes
    Object[] recipesArray=(Object[])tierProps.getProperty("CraftProfession_RecipeArray");
    if (recipesArray!=null)
    {
      for(Object recipeObj : recipesArray)
      {
        int recipeId=((Integer)recipeObj).intValue();
        ret.addRecipe(recipeId);
      }
    }

    /*
    CraftProfession_AutoCompleteMastery: 1
    CraftProfession_AutoCompleteProficiency: 1
    // Trait given by mastery completion
    CraftProfession_CompletedMasteryLevelCharacteristic: 1879055788
    // Trait given by proficiency completion
    CraftProfession_CompletedProficiencyLevelCharacteristic: 1879055787
    // Trait that opens this tier
    CraftProfession_TierOpeningCharacteristic: 1879272071
     */
    return ret;
  }

  private void handleGuild(int guildId)
  {
    PropertiesSet guildProps=_facade.loadProperties(guildId+DATConstants.DBPROPERTIES_OFFSET);
    int factionId=((Integer)guildProps.getProperty("CraftGuild_Faction")).intValue();
    int professionId=((Integer)guildProps.getProperty("CraftGuild_Profession")).intValue();
    Professions professions=_data.getProfessionsRegistry();
    Profession profession=professions.getProfessionById(professionId);
    Faction faction=FactionsRegistry.getInstance().getById(factionId);
    profession.setGuildFaction(faction);
    /*
    ******** Properties: 1879124441
    CraftGuild_Faction: 1879124448
    CraftGuild_LearnString: 
      #1: Do you want to join the Cook's Guild?
    CraftGuild_Profession: 1879061252
    CraftGuild_QuestCategory: 94 (Crafting Guild: Cook)
    CraftGuild_RequiredCraftTier: 2 (Journeyman)
    */
  }

  /**
   * Save the loaded data to a file.
   */
  private void save()
  {
    CraftingXMLWriter.write(GeneratedFiles.CRAFTING_DATA,_data);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatCraftingLoader(facade).doIt();
    facade.dispose();
  }
}
