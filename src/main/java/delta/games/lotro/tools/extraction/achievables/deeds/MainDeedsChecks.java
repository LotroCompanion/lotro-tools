package delta.games.lotro.tools.extraction.achievables.deeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.common.utils.io.Console;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;

/**
 * Tool to perform some checks on deeds.
 * @author DAM
 */
public class MainDeedsChecks
{
  private void doIt()
  {
    for(DeedDescription deed : DeedsManager.getInstance().getAll())
    {
      checkDeed(deed);
    }
  }

  private void checkDeed(DeedDescription deed)
  {
    Set<Integer> objectiveIndexes=new HashSet<Integer>();
    ObjectivesManager objectivesMgr=deed.getObjectives();
    for(Objective objective : objectivesMgr.getObjectives())
    {
      Integer index=Integer.valueOf(objective.getIndex());
      if (objectiveIndexes.contains(index))
      {
        Console.println("Duplicate objective index "+index+" for deed: "+deed);
      }
      else
      {
        objectiveIndexes.add(index);
      }
    }
    List<Integer> indexes=new ArrayList<Integer>(objectiveIndexes);
    Collections.sort(indexes);
    int nbObjectives=objectivesMgr.getObjectivesCount();
    if (indexes.size()!=nbObjectives)
    {
      Console.println("Bad objectives count for deed: "+deed);
    }
    if (indexes.get(0).intValue()!=1)
    {
      Console.println("Bad first index for deed: "+deed);
    }
    if (indexes.get(nbObjectives-1).intValue()!=nbObjectives)
    {
      Console.println("Bad last index for deed: "+deed);
    }
    for(Objective objective : objectivesMgr.getObjectives())
    {
      checkDeedObjective(deed,objective);
    }
  }

  private void checkDeedObjective(DeedDescription deed, Objective objective)
  {
    Set<Integer> conditionIndexes=new HashSet<Integer>();
    for(ObjectiveCondition condition : objective.getConditions())
    {
      Integer index=Integer.valueOf(condition.getIndex());
      if (conditionIndexes.contains(index))
      {
        Console.println("Duplicate condition index "+index+" for deed: "+deed+" objective #"+objective.getIndex());
      }
      else
      {
        conditionIndexes.add(index);
      }
    }
    List<Integer> indexes=new ArrayList<Integer>(conditionIndexes);
    Collections.sort(indexes);
    int nbConditions=objective.getConditions().size();
    if (indexes.size()!=nbConditions)
    {
      Console.println("Bad conditions count for deed: "+deed);
    }
    if (indexes.get(0).intValue()!=0)
    {
      Console.println("Bad first index for deed: "+deed+", objective #"+objective.getIndex());
    }
    if (indexes.get(nbConditions-1).intValue()!=nbConditions-1)
    {
      Console.println("Bad last index for deed: "+deed+", objective #"+objective.getIndex());
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainDeedsChecks().doIt();
  }
}
