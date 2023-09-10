package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.enums.CraftTier;
import delta.games.lotro.common.requirements.FactionRequirement;
import delta.games.lotro.common.requirements.GloryRankRequirement;
import delta.games.lotro.common.requirements.ProfessionRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;

/**
 * Utility methods to load requirements.
 * @author DAM
 */
public class RequirementsLoadingUtils
{
  private static final Logger LOGGER=Logger.getLogger(RequirementsLoadingUtils.class);

  /**
   * Load level requirements.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadLevelRequirements(PropertiesSet properties, UsageRequirement requirements)
  {
    Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");
    if ((minLevel!=null) && (minLevel.intValue()>1))
    {
      requirements.setMinLevel(minLevel);
    }
    Integer maxLevel=(Integer)properties.getProperty("Usage_MaxLevel");
    if ((maxLevel!=null) && (maxLevel.intValue()!=-1))
    {
      requirements.setMaxLevel(maxLevel);
    }
    Integer floatToCap=(Integer)properties.getProperty("Usage_MinLevel_FloatToCap");
    if ((floatToCap!=null) && (floatToCap.intValue()==1))
    {
      int capLevel=LotroCoreConfig.getInstance().getMaxCharacterLevel();
      requirements.setMinLevel(Integer.valueOf(capLevel));
    }
  }

  /**
   * Load class requirements.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredClasses(PropertiesSet properties, UsageRequirement requirements)
  {
    /*
    Usage_RequiredClassList: 
      #1: 162
    */
    Object[] classReqs=(Object[])properties.getProperty("Usage_RequiredClassList");
    if (classReqs!=null)
    {
      for(Object classReq : classReqs)
      {
        int classCode=((Integer)classReq).intValue();
        AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
        if (abstractClass!=null)
        {
          requirements.addAllowedClass(abstractClass);
        }
      }
    }
  }

  /**
   * Load race requirements.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredRaces(PropertiesSet properties, UsageRequirement requirements)
  {
    /*
    Usage_RequiredRaces: 
      #1: 81
      #2: 23
      #3: 114
    */
    Object[] raceReqs=(Object[])properties.getProperty("Usage_RequiredRaces");
    if (raceReqs!=null)
    {
      for(Object raceReq : raceReqs)
      {
        int raceId=((Integer)raceReq).intValue();
        RaceDescription race=RacesManager.getInstance().getByCode(raceId);
        if (race!=null)
        {
          requirements.addAllowedRace(race);
        }
      }
    }
  }

  /**
   * Load faction requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredFaction(PropertiesSet properties, UsageRequirement requirements)
  {
    PropertiesSet factionReqProps=(PropertiesSet)properties.getProperty("Usage_RequiredFaction");
    if (factionReqProps!=null)
    {
      Integer factionId=(Integer)factionReqProps.getProperty("Usage_RequiredFaction_DataID");
      Integer tier=(Integer)factionReqProps.getProperty("Usage_RequiredFaction_Tier");
      if ((factionId!=null) && (tier!=null))
      {
        Faction faction=FactionsRegistry.getInstance().getById(factionId.intValue());
        if (faction!=null)
        {
          FactionRequirement factionRequirement=new FactionRequirement(faction,tier.intValue());
          requirements.setFactionRequirement(factionRequirement);
        }
        else
        {
          LOGGER.warn("Faction not found: "+factionId);
        }
      }
      else
      {
        LOGGER.debug("Incomplete faction requirement: factionId="+factionId+", tier="+tier);
      }
    }
  }

  /**
   * Load profession requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredProfession(PropertiesSet properties, UsageRequirement requirements)
  {
    Integer professionId=(Integer)properties.getProperty("Usage_RequiredCraftProfession");
    if (professionId!=null)
    {
      Professions professions=CraftingSystem.getInstance().getData().getProfessionsRegistry();
      Profession profession=professions.getProfessionById(professionId.intValue());
      if (profession!=null)
      {
        // For permission blob (DefaultPermissionBlobStruct)
        Integer proficiency=(Integer)properties.getProperty("Usage_RequiredCraftProficiency");
        CraftTier tier=null;
        if (proficiency!=null)
        {
          CraftingLevel level=profession.getByTier(proficiency.intValue());
          tier=level.getCraftTier();
        }
        // For items
        proficiency=(Integer)properties.getProperty("Usage_RequiredCraftTier");
        if (proficiency!=null)
        {
          CraftingLevel level=profession.getByTier(proficiency.intValue());
          tier=level.getCraftTier();
        }
        ProfessionRequirement professionRequirement=new ProfessionRequirement(profession,tier);
        requirements.setProfessionRequirement(professionRequirement);
      }
      else
      {
        LOGGER.warn("Profession not found: "+professionId);
      }
    }
  }

  /**
   * Load glory rank requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredGloryRank(PropertiesSet properties, UsageRequirement requirements)
  {
    Integer minRank=(Integer)properties.getProperty("Usage_MinGloryRank");
    if (minRank!=null)
    {
      GloryRankRequirement requirement=new GloryRankRequirement();
      requirement.setRank(minRank.intValue());
      requirements.setGloryRankRequirement(requirement);
    }
  }
}
