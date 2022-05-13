package delta.games.lotro.tools.lore.deeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.lore.worldEvents.CompoundWorldEventCondition;
import delta.games.lotro.lore.worldEvents.SimpleWorldEventCondition;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.lore.worldEvents.WorldEventsManager;
import delta.games.lotro.utils.Proxy;

/**
 * Assess usage of world events in deeds.
 * @author DAM
 */
public class MainAssessWorldEventUsageInDeeds
{
  private Map<Integer,IntegerHolder> _counters=new HashMap<Integer,IntegerHolder>();

  private void doIt()
  {
    for(DeedDescription deed : DeedsManager.getInstance().getAll())
    {
      checkDeed(deed);
    }
    showResults();
  }

  private void checkDeed(DeedDescription deed)
  {
    AbstractWorldEventCondition condition=deed.getWorldEventsRequirement();
    if (condition==null)
    {
      return;
    }
    inspectCondition(condition);
  }

  private void inspectCondition(AbstractWorldEventCondition condition)
  {
    if (condition instanceof CompoundWorldEventCondition)
    {
      CompoundWorldEventCondition compoundCondition=(CompoundWorldEventCondition)condition;
      for(AbstractWorldEventCondition childCondition : compoundCondition.getItems())
      {
        inspectCondition(childCondition);
      }
    }
    else if (condition instanceof SimpleWorldEventCondition)
    {
      SimpleWorldEventCondition simpleCondition=(SimpleWorldEventCondition)condition;
      inspectSimpleCondition(simpleCondition);
    }
  }

  private void inspectSimpleCondition(SimpleWorldEventCondition condition)
  {
    inspectWorldEvent(condition.getCompareToWorldEvent());
    inspectWorldEvent(condition.getWorldEvent());
  }

  private void inspectWorldEvent(Proxy<WorldEvent> worldEvent)
  {
    if (worldEvent==null)
    {
      return;
    }
    Integer key=Integer.valueOf(worldEvent.getId());
    IntegerHolder counter=_counters.get(key);
    if (counter==null)
    {
      counter=new IntegerHolder();
      _counters.put(key,counter);
    }
    counter.increment();
  }

  private void showResults()
  {
    List<Integer> ids=new ArrayList<Integer>(_counters.keySet());
    Collections.sort(ids);
    WorldEventsManager worldEventsMgr=WorldEventsManager.getInstance();
    for(Integer id : ids)
    {
      WorldEvent worldEvent=worldEventsMgr.getWorldEvent(id.intValue());
      System.out.println(worldEvent+" => "+_counters.get(id));
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainAssessWorldEventUsageInDeeds().doIt();
  }
}
