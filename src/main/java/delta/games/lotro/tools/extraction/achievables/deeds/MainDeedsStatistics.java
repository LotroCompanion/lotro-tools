package delta.games.lotro.tools.extraction.achievables.deeds;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;

/**
 * Tool to build some statistics on deeds.
 * @author DAM
 */
public class MainDeedsStatistics
{
  private Map<Integer,IntegerHolder> _conditionsCount;
  private Map<Integer,IntegerHolder> _objectivesCount;
  private PrintStream _out;

  private MainDeedsStatistics()
  {
    _conditionsCount=new HashMap<Integer,IntegerHolder>();
    _objectivesCount=new HashMap<Integer,IntegerHolder>();
    _out=System.out;
  }

  private void doIt()
  {
    for(DeedDescription deed : DeedsManager.getInstance().getAll())
    {
      handleDeed(deed);
    }
    _out.println("Objectives count: "+_objectivesCount);
    _out.println("Conditions count: "+_conditionsCount);
  }

  private void handleDeed(DeedDescription deed)
  {
    ObjectivesManager objectivesMgr=deed.getObjectives();
    int objectivesCount=objectivesMgr.getObjectivesCount();
    Integer key=Integer.valueOf(objectivesCount);
    IntegerHolder countHolder=_objectivesCount.get(key);
    if (countHolder==null)
    {
      countHolder=new IntegerHolder();
      _objectivesCount.put(key,countHolder);
    }
    countHolder.increment();
    if (objectivesCount>2)
    {
      _out.println(deed+" has "+objectivesCount+" objectives");
    }
    List<Objective> objectives=objectivesMgr.getObjectives();
    for(Objective objective : objectives)
    {
      handleObjective(deed,objective);
    }
  }

  private void handleObjective(DeedDescription deed, Objective objective)
  {
    int conditionsCount=objective.getConditions().size();
    Integer key=Integer.valueOf(conditionsCount);
    IntegerHolder countHolder=_conditionsCount.get(key);
    if (countHolder==null)
    {
      countHolder=new IntegerHolder();
      _conditionsCount.put(key,countHolder);
    }
    if (conditionsCount>20)
    {
      _out.println(deed+", objective #"+objective.getIndex()+" has "+conditionsCount+" conditions");
    }
    countHolder.increment();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainDeedsStatistics().doIt();
  }
}
