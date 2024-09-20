package delta.games.lotro.tools.extraction.factions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.requirements.RaceRequirement;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.quests.objectives.FactionLevelCondition;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.lore.reputation.Faction;

/**
 * Finds reputation deeds.
 * @author DAM
 */
public class ReputationDeedsFinder
{
  private static final Logger LOGGER=LoggerFactory.getLogger(ReputationDeedsFinder.class);

  /**
   * Map faction identifiers to maps of tier to deed.
   */
  private Map<Integer,Map<Integer,DeedDescription>> _storage;

  /**
   * Constructor.
   */
  public ReputationDeedsFinder()
  {
    _storage=new HashMap<Integer,Map<Integer,DeedDescription>>();
  }

  /**
   * Init with some deeds.
   * @param deeds Input deeds.
   */
  public void init(List<DeedDescription> deeds)
  {
    for(DeedDescription deed : deeds)
    {
      if (useDeed(deed))
      {
        scanDeed(deed);
      }
    }
  }

  private void scanDeed(DeedDescription deed)
  {
    FactionLevelCondition condition=getFactionLevelCondition(deed);
    if (condition!=null)
    {
      Faction faction=condition.getFaction();
      int factionId=faction.getIdentifier();
      int tier=condition.getTier();
      registerDeed(factionId,tier,deed);
    }
  }

  private void registerDeed(int factionId, int tier, DeedDescription deed)
  {
    Integer factionKey=Integer.valueOf(factionId);
    Map<Integer,DeedDescription> deedsMap=_storage.get(factionKey);
    if (deedsMap==null)
    {
      deedsMap=new HashMap<Integer,DeedDescription>();
      _storage.put(factionKey,deedsMap);
    }
    Integer tierKey=Integer.valueOf(tier);
    DeedDescription oldDeed=deedsMap.get(tierKey);
    if (oldDeed==null)
    {
      deedsMap.put(tierKey,deed);
      LOGGER.debug("Found deed: "+deed+" for faction id="+factionId+", tier="+tier);
    }
    else
    {
      LOGGER.warn("Duplicate deed for faction: "+factionId+", tier: "+tier+": old="+oldDeed.getName()+", new="+deed.getName());
    }
  }

  /**
   * Extract the faction level condition for a deed, if any.
   * If several conditions are found, but for different factions, ignore the deed.
   * If several conditions are found for the same faction, then the highest tier is kept.
   * @param deed Input deed.
   * @return A faction level condition, or <code>null</code>.
   */
  private FactionLevelCondition getFactionLevelCondition(DeedDescription deed)
  {
    FactionLevelCondition ret=null;
    Integer highestTier=null;
    Integer currentFactionId=null;
    ObjectivesManager objectivesMgr=deed.getObjectives();
    List<Objective> objectives=objectivesMgr.getObjectives();
    for(Objective objective : objectives)
    {
      List<ObjectiveCondition> conditions=objective.getConditions();
      for(ObjectiveCondition condition : conditions)
      {
        if (condition instanceof FactionLevelCondition)
        {
          FactionLevelCondition factionLevelCondition=(FactionLevelCondition)condition;
          Faction faction=factionLevelCondition.getFaction();
          if (faction==null)
          {
            continue;
          }
          int factionId=faction.getIdentifier();
          if ((currentFactionId==null) || (currentFactionId.intValue()==factionId))
          {
            currentFactionId=Integer.valueOf(factionId);
            int tier=factionLevelCondition.getTier();
            if ((highestTier==null) || (highestTier.intValue()<tier))
            {
              ret=factionLevelCondition;
              highestTier=Integer.valueOf(tier);
            }
          }
          else
          {
            return null;
          }
        }
        else
        {
          // Ignore deed that have other types of conditions
          return null;
        }
      }
    }
    return ret;
  }

  private boolean useDeed(DeedDescription deed)
  {
    RaceRequirement raceReq=deed.getUsageRequirement().getRaceRequirement();
    return (raceReq==null);
  }

  /**
   * Find the deed for a given faction and reputation tier.
   * @param factionId Faction identifier.
   * @param tier Tier.
   * @return A deed or <code>null</code> if not found.
   */
  public DeedDescription findDeed(int factionId, int tier)
  {
    DeedDescription ret=null;
    Map<Integer,DeedDescription> deedsMap=_storage.get(Integer.valueOf(factionId));
    if (deedsMap!=null)
    {
      ret=deedsMap.get(Integer.valueOf(tier));
    }
    return ret;
  }
}
