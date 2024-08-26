package delta.games.lotro.tools.reports;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.common.utils.collections.filters.Operator;
import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedsManager;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.lore.worldEvents.CompoundWorldEventCondition;
import delta.games.lotro.lore.worldEvents.SimpleWorldEventCondition;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.lore.worldEvents.WorldEventsManager;
import delta.games.lotro.utils.Proxy;

/**
 * Assess usage of world events in achievables (deeds and quests).
 * @author DAM
 */
public class MainAssessWorldEventUsageInAchievables
{
  private Map<Integer,IntegerHolder> _weCounters=new HashMap<Integer,IntegerHolder>();
  private Map<Integer,IntegerHolder> _complexityCounters=new HashMap<Integer,IntegerHolder>();
  private int _nbCompound;
  private int _nbAnd;
  private PrintStream _out=System.out;

  private void doIt()
  {
    doQuests();
    doDeeds();
    showResults();
  }

  void doQuests()
  {
    for(QuestDescription quest : QuestsManager.getInstance().getAll())
    {
      checkAchievable(quest);
    }
  }

  void doDeeds()
  {
    for(DeedDescription deed : DeedsManager.getInstance().getAll())
    {
      checkAchievable(deed);
    }
  }

  private void checkAchievable(Achievable achievable)
  {
    checkWorldEventsInAchievable(achievable);
    checkConditionComplexity(achievable);
  }

  private void checkWorldEventsInAchievable(Achievable achievable)
  {
    AbstractWorldEventCondition condition=achievable.getWorldEventsRequirement();
    if (condition==null)
    {
      return;
    }
    inspectWorldEventsInCondition(condition);
  }

  private void inspectWorldEventsInCondition(AbstractWorldEventCondition condition)
  {
    if (condition instanceof CompoundWorldEventCondition)
    {
      CompoundWorldEventCondition compoundCondition=(CompoundWorldEventCondition)condition;
      for(AbstractWorldEventCondition childCondition : compoundCondition.getItems())
      {
        inspectWorldEventsInCondition(childCondition);
      }
    }
    else if (condition instanceof SimpleWorldEventCondition)
    {
      SimpleWorldEventCondition simpleCondition=(SimpleWorldEventCondition)condition;
      inspectWorldEventsInSimpleCondition(simpleCondition);
    }
  }

  private void inspectWorldEventsInSimpleCondition(SimpleWorldEventCondition condition)
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
    IntegerHolder counter=_weCounters.get(key);
    if (counter==null)
    {
      counter=new IntegerHolder();
      _weCounters.put(key,counter);
    }
    counter.increment();
  }

  private void showResults()
  {
    showWorldEventsUsage();
    showComplexity();
  }

  void showComplexity()
  {
    IntegerHolder nbComplexConditions=_complexityCounters.get(Integer.valueOf(-1));
    _out.println("Nb Complex condition: "+nbComplexConditions);
    IntegerHolder nbNoConditions=_complexityCounters.get(Integer.valueOf(0));
    _out.println("Nb NO condition: "+nbNoConditions);
    IntegerHolder nbSimpleConditions=_complexityCounters.get(Integer.valueOf(1));
    _out.println("Nb simple condition: "+nbSimpleConditions);
    for(int i=2;i<20;i++)
    {
      IntegerHolder nb=_complexityCounters.get(Integer.valueOf(i));
      if (nb!=null)
      {
        _out.println(i+" conditions: "+nb);
      }
    }
    _out.println("Nb compound: "+_nbCompound+", nb AND: "+_nbAnd+", nb OR: "+(_nbCompound-_nbAnd));
  }

  void showWorldEventsUsage()
  {
    Comparator<Map.Entry<Integer,IntegerHolder>> c=new Comparator<Map.Entry<Integer,IntegerHolder>>()
    {
      @Override
      public int compare(Map.Entry<Integer,IntegerHolder> e1, Map.Entry<Integer,IntegerHolder> e2)
      {
        return Integer.compare(e1.getValue().getInt(),e2.getValue().getInt());
      }
    };
    List<Map.Entry<Integer,IntegerHolder>> entries=new ArrayList<Map.Entry<Integer,IntegerHolder>>(_weCounters.entrySet());
    Collections.sort(entries,c);
    Collections.reverse(entries);
    WorldEventsManager worldEventsMgr=WorldEventsManager.getInstance();
    for(Map.Entry<Integer,IntegerHolder> entry : entries)
    {
      int id=entry.getKey().intValue();
      WorldEvent worldEvent=worldEventsMgr.getWorldEvent(id);
      _out.println(worldEvent+" => "+entry.getValue());
    }
  }

  private void addComplexity(int intKey)
  {
    Integer key=Integer.valueOf(intKey);
    IntegerHolder counter=_complexityCounters.get(key);
    if (counter==null)
    {
      counter=new IntegerHolder();
      _complexityCounters.put(key,counter);
    }
    counter.increment();
  }

  private void checkConditionComplexity(Achievable achievable)
  {
    // - no condition; 0
    // - simple condition: 1
    // - compound condition with N>=2 items: N
    // - other: -1
    AbstractWorldEventCondition condition=achievable.getWorldEventsRequirement();
    if (condition==null)
    {
      addComplexity(0);
    }
    else if (condition instanceof SimpleWorldEventCondition)
    {
      addComplexity(1);
    }
    else if (condition instanceof CompoundWorldEventCondition)
    {
      _nbCompound++;
      CompoundWorldEventCondition compoundCondition=(CompoundWorldEventCondition)condition;
      if (compoundCondition.getOperator()==Operator.AND)
      {
        _nbAnd++;
      }
      List<AbstractWorldEventCondition> childConditions=compoundCondition.getItems();
      boolean hasCompoundChild=false;
      for(AbstractWorldEventCondition childCondition : childConditions)
      {
        if (childCondition instanceof CompoundWorldEventCondition)
        {
          hasCompoundChild=true;
          break;
        }
      }
      if (hasCompoundChild)
      {
        addComplexity(-1);
        show(achievable);
      }
      else
      {
        addComplexity(childConditions.size());
        if ((childConditions.size()>2) || (compoundCondition.getOperator()==Operator.OR))
        {
          show(achievable);
        }
      }
    }
  }

  private void show(Achievable achievable)
  {
    _out.println("Achievable: "+achievable.getName());
    AbstractWorldEventCondition condition=achievable.getWorldEventsRequirement();
    if (condition instanceof SimpleWorldEventCondition)
    {
      _out.println("\t"+condition);
    }
    else if (condition instanceof CompoundWorldEventCondition)
    {
      CompoundWorldEventCondition compoundCondition=(CompoundWorldEventCondition)condition;
      _out.println("Operator: "+compoundCondition.getOperator());
      List<AbstractWorldEventCondition> childConditions=compoundCondition.getItems();
      for(AbstractWorldEventCondition childCondition : childConditions)
      {
        _out.println("\t"+childCondition);
      }
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainAssessWorldEventUsageInAchievables().doIt();
  }
}
