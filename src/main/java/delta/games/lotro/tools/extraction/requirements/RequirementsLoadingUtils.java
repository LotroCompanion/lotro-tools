package delta.games.lotro.tools.extraction.requirements;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.enums.CraftTier;
import delta.games.lotro.common.requirements.ClassRequirement;
import delta.games.lotro.common.requirements.EffectRequirement;
import delta.games.lotro.common.requirements.FactionRequirement;
import delta.games.lotro.common.requirements.GloryRankRequirement;
import delta.games.lotro.common.requirements.LevelRangeRequirement;
import delta.games.lotro.common.requirements.ProfessionRequirement;
import delta.games.lotro.common.requirements.RaceRequirement;
import delta.games.lotro.common.requirements.Requirements;
import delta.games.lotro.common.requirements.TraitRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.crafting.CraftingLevel;
import delta.games.lotro.lore.crafting.CraftingSystem;
import delta.games.lotro.lore.crafting.Profession;
import delta.games.lotro.lore.crafting.Professions;
import delta.games.lotro.lore.parameters.Game;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;
import delta.games.lotro.tools.extraction.effects.EffectLoader;

/**
 * Utility methods to load requirements.
 * @author DAM
 */
public class RequirementsLoadingUtils
{
  private static final Logger LOGGER=LoggerFactory.getLogger(RequirementsLoadingUtils.class);

  /**
   * Load level requirements.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadLevelRequirements(PropertiesSet properties, Requirements requirements)
  {
    Integer minLevel=(Integer)properties.getProperty("Usage_MinLevel");
    if ((minLevel!=null) && (minLevel.intValue()<=1))
    {
      minLevel=null;
    }
    Integer maxLevel=(Integer)properties.getProperty("Usage_MaxLevel");
    if ((maxLevel!=null) && (maxLevel.intValue()==-1))
    {
      maxLevel=null;
    }
    Integer floatToCap=(Integer)properties.getProperty("Usage_MinLevel_FloatToCap");
    if ((floatToCap!=null) && (floatToCap.intValue()==1))
    {
      int capLevel=Game.getParameters().getMaxCharacterLevel();
      minLevel=Integer.valueOf(capLevel);
    }
    if ((minLevel!=null) || (maxLevel!=null))
    {
      LevelRangeRequirement levelRequirement=new LevelRangeRequirement(minLevel,maxLevel);
      requirements.setRequirement(LevelRangeRequirement.class,levelRequirement);
    }
  }

  /**
   * Load class requirements.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredClasses(PropertiesSet properties, Requirements requirements)
  {
    /*
    Usage_RequiredClassList:
      #1: 162
    */
    Object[] classReqs=(Object[])properties.getProperty("Usage_RequiredClassList");
    if (classReqs!=null)
    {
      List<AbstractClassDescription> classes=new ArrayList<AbstractClassDescription>();
      for(Object classReq : classReqs)
      {
        int classCode=((Integer)classReq).intValue();
        AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
        if (abstractClass!=null)
        {
          classes.add(abstractClass);
        }
      }
      if (!classes.isEmpty())
      {
        ClassRequirement classRequirement=new ClassRequirement(classes);
        requirements.setRequirement(ClassRequirement.class,classRequirement);
      }
    }
  }

  /**
   * Load race requirements.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredRaces(PropertiesSet properties, Requirements requirements)
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
      List<RaceDescription> races=new ArrayList<RaceDescription>();
      for(Object raceReq : raceReqs)
      {
        int raceId=((Integer)raceReq).intValue();
        RaceDescription race=RacesManager.getInstance().getByCode(raceId);
        if (race!=null)
        {
          races.add(race);
        }
      }
      if (!races.isEmpty())
      {
        RaceRequirement raceRequirement=new RaceRequirement(races);
        requirements.setRequirement(RaceRequirement.class,raceRequirement);
      }
    }
  }

  /**
   * Load faction requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredFaction(PropertiesSet properties, Requirements requirements)
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
          requirements.setRequirement(FactionRequirement.class,factionRequirement);
        }
        else
        {
          LOGGER.warn("Faction not found: {}",factionId);
        }
      }
      else
      {
        LOGGER.debug("Incomplete faction requirement: factionId={}, tier={}",factionId,tier);
      }
    }
  }

  /**
   * Load profession requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredProfession(PropertiesSet properties, Requirements requirements)
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
        requirements.setRequirement(ProfessionRequirement.class,professionRequirement);
      }
      else
      {
        LOGGER.warn("Profession not found: {}",professionId);
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
    if ((minRank!=null) && (minRank.intValue()>0))
    {
      GloryRankRequirement requirement=new GloryRankRequirement();
      requirement.setRank(minRank.intValue());
      requirements.setGloryRankRequirement(requirement);
    }
  }

  /**
   * Load effect requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   * @param loader Effects loader.
   */
  public static void loadRequiredEffect(PropertiesSet properties, UsageRequirement requirements, EffectLoader loader)
  {
    Integer effectId=(Integer)properties.getProperty("Usage_RequiredEffect");
    if ((effectId!=null) && (effectId.intValue()>0))
    {
      Effect effect=loader.getEffect(effectId.intValue());
      EffectRequirement requirement=new EffectRequirement(effect);
      requirements.setEffectRequirement(requirement);
    }
  }

  /**
   * Load trait requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public static void loadRequiredTrait(PropertiesSet properties, UsageRequirement requirements)
  {
    Integer traitId=(Integer)properties.getProperty("Usage_RequiredTrait");
    if ((traitId!=null) && (traitId.intValue()>0))
    {
      TraitDescription trait=TraitsManager.getInstance().getTrait(traitId.intValue());
      TraitRequirement requirement=new TraitRequirement(trait);
      requirements.setTraitRequirement(requirement);
    }
  }
}
