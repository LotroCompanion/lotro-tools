package delta.games.lotro.tools.dat.quests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.objectives.FactionLevelCondition;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.lore.reputation.Faction;
import delta.games.lotro.utils.Proxy;

/**
 * Finds reputation deeds.
 * @author DAM
 */
public class ReputationDeedsFinder
{
  private static final Logger LOGGER=Logger.getLogger(ReputationDeedsFinder.class);

  private Map<Integer,Map<Integer,DeedDescription>> _storage;

  /**
   * Constructor.
   */
  public ReputationDeedsFinder()
  {
    _storage=new HashMap<Integer,Map<Integer,DeedDescription>>();
  }

  /**
   * Do it.
   * @param deeds Input deeds.
   */
  public void doIt(List<DeedDescription> deeds)
  {
    for(DeedDescription deed : deeds)
    {
      scanDeed(deed);
    }
  }

  private void scanDeed(DeedDescription deed)
  {
    FactionLevelCondition condition=getFactionLevelCondition(deed);
    if (condition!=null)
    {
      Proxy<Faction> factionProxy=condition.getProxy();
      int factionId=factionProxy.getId();
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
      System.out.println("Found deed: "+deed+" for faction id="+factionId+", tier="+tier);
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
          int factionId=factionLevelCondition.getProxy().getId();
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

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DeedsManager deedsMgr=DeedsManager.getInstance();
    List<DeedDescription> deeds=deedsMgr.getAll();
    new ReputationDeedsFinder().doIt(deeds);
  }
}
