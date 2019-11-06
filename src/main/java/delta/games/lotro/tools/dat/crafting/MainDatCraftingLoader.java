package delta.games.lotro.tools.dat.crafting;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.crafting.CraftingData;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingLevelTier;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.crafting.Vocation;
import delta.games.lotro.lore.crafting.io.xml.CraftingXMLWriter;
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

  private void doIt()
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
    /*
    Object[] guildArray=(Object[])props.getProperty("CraftDirectory_CraftGuildArray");
    for(Object guildObj : guildArray)
    {
      int guildId=((Integer)guildObj).intValue();
      handleGuild(guildId);
    }
    */
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
    System.out.println("Vocation: "+name);
    // - description
    String description=DatUtils.getStringProperty(vocationProps,"CraftVocation_Description");
    ret.setDescription(description);
    System.out.println("\tDescription: "+description);
    // - professions
    Object[] professionsArray=(Object[])vocationProps.getProperty("CraftVocation_ProfessionArray");
    for(Object professionObj : professionsArray)
    {
      int professionId=((Integer)professionObj).intValue();
      System.out.println("\tProfession: "+professionId);
      Profession profession=handleProfession(professionId);
      if (profession!=null)
      {
        ret.addProfession(profession);
      }
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
      //System.out.println("Profession: "+name);
      // - description
      String description=DatUtils.getStringProperty(professionProps,"CraftProfession_Description");
      ret.setDescription(description);
      //System.out.println("\tDescription: "+description);
      // - tiers
      CraftingLevel beginner=buildBeginnerLevel();
      ret.addLevel(beginner);
      Object[] tiersArray=(Object[])professionProps.getProperty("CraftProfession_TierArray");
      for(Object tierObj : tiersArray)
      {
        PropertiesSet tierProps=(PropertiesSet)tierObj;
        CraftingLevel level=handleProfessionTier(tierProps);
        ret.addLevel(level);
      }
      // - key
      String key=getProfessionKey(professionId);
      ret.setKey(key);
      professions.addProfession(ret);
    }
    return ret;

    /*
CraftProfession_AllowsXPAcceleration: 1
CraftProfession_Characteristic: 0
CraftProfession_CompletedMasteryLevel_PropertyName: 268436939 (Craft_Weaponsmith_CompletedMasteryLevel)
CraftProfession_CompletedProficiencyLevel_PropertyName: 268439502 (Craft_Weaponsmith_CompletedProficiencyLevel)
CraftProfession_CriticalChanceAddModifier_PropertyName: 268436462 (Craft_Weaponsmith_CriticalChanceAddModifier)
CraftProfession_Enabled_PropertyName: 268435997 (Craft_Weaponsmith_Enabled)
CraftProfession_ExtraRecipeArray_PropertyName: 268439738 (Craft_Weaponsmith_ExtraRecipeArray)
CraftProfession_MasteryLevel_PropertyName: 268437879 (Craft_Weaponsmith_MasteryLevel)
CraftProfession_MasteryXP_PropertyName: 268438517 (Craft_Weaponsmith_MasteryXP)
CraftProfession_OpenLevel_PropertyName: 268445421 (Craft_Weaponsmith_OpenLevel)
CraftProfession_ProficiencyLevel_PropertyName: 268435526 (Craft_Weaponsmith_ProficiencyLevel)
CraftProfession_ProficiencyXP_PropertyName: 268435525 (Craft_Weaponsmith_ProficiencyXP)
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

  private CraftingLevel buildBeginnerLevel()
  {
    CraftingLevel ret=new CraftingLevel(0);
    ret.setName("Beginner");
    return ret;
  }

  private CraftingLevel handleProfessionTier(PropertiesSet tierProps)
  {
    //System.out.println(tierProps.dump());

    // Tier
    int tier=((Integer)tierProps.getProperty("CraftProfession_Tier")).intValue();

    CraftingLevel ret=new CraftingLevel(tier);

    String tierName=_tier.getString(tier);
    ret.setName(tierName);
    //System.out.println("Tier "+tier+": "+tierName);

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
      //String proficiencyTitle=loadTitleLabel(proficiencyTitleId);
      //System.out.println("Proficiency: XP="+proficiencyXp+", title="+proficiencyTitle);
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
      //String masteryTitle=loadTitleLabel(masteryTitleId);
      //System.out.println("Mastery: XP="+masteryXp+", title="+masteryTitle);
    }

    /*
    Object[] recipesArray=(Object[])tierProps.getProperty("CraftProfession_RecipeArray");
    for(Object recipeObj : recipesArray)
    {
      int recipeId=((Integer)recipeObj).intValue();
      System.out.println("\t"+recipeId);
    }
    */

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