package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.common.requirements.FactionRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.lore.reputation.FactionsRegistry;

/**
 * Utility methods to load requirements.
 * @author DAL
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
    // TODO: Usage_MinLevel_FloatToCap
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
        int characterClassId=((Integer)classReq).intValue();
        CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(characterClassId);
        if (characterClass!=null)
        {
          requirements.addAllowedClass(characterClass);
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
        Race race=DatEnumsUtils.getRaceFromRaceId(raceId);
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
        LOGGER.warn("Incomplete faction requirement: factionId="+factionId+", tier="+tier);
      }
    }
  }
}
