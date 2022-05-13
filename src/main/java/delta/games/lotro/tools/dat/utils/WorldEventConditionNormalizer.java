package delta.games.lotro.tools.dat.utils;

import java.util.ArrayList;
import java.util.List;

import delta.common.utils.collections.filters.Operator;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.lore.worldEvents.CompoundWorldEventCondition;
import delta.games.lotro.lore.worldEvents.SimpleWorldEventCondition;

/**
 * Normalizer for world event conditions.
 * @author DAM
 */
public class WorldEventConditionNormalizer
{
  /**
   * Normalize a world event condition.
   * @param condition Input condition.
   * @return the normalized condition, or the source condition if no change.
   */
  public AbstractWorldEventCondition normalize(AbstractWorldEventCondition condition)
  {
    if (condition==null)
    {
      return null;
    }
    if (condition instanceof SimpleWorldEventCondition)
    {
      return condition;
    }
    CompoundWorldEventCondition compoundCondition=(CompoundWorldEventCondition)condition;
    // Simplify children first
    for(AbstractWorldEventCondition childCondition : compoundCondition.getItems())
    {
      AbstractWorldEventCondition normalizedChild=normalize(childCondition);
      if (normalizedChild!=childCondition)
      {
        compoundCondition.replace(childCondition,normalizedChild);
      }
    }
    boolean isAllSameOperator=isAllSameOperator(compoundCondition);
    if (isAllSameOperator)
    {
      return simplifySameOperator(compoundCondition);
    }
    return compoundCondition;
  }

  private boolean isAllSameOperator(CompoundWorldEventCondition compoundCondition)
  {
    Operator expectedOperator=compoundCondition.getOperator();
    for(AbstractWorldEventCondition childCondition : compoundCondition.getItems())
    {
      if (childCondition instanceof CompoundWorldEventCondition)
      {
        CompoundWorldEventCondition compoundChild=(CompoundWorldEventCondition)childCondition;
        Operator compoundChildOperator=compoundChild.getOperator();
        if (compoundChildOperator!=expectedOperator)
        {
          return false;
        }
        boolean childOK=isAllSameOperator(compoundChild);
        if (!childOK)
        {
          return false;
        }
      }
    }
    return true;
  }

  private CompoundWorldEventCondition simplifySameOperator(CompoundWorldEventCondition source)
  {
    CompoundWorldEventCondition ret=new CompoundWorldEventCondition(source.getOperator());
    for(AbstractWorldEventCondition childCondition : getConditions(source))
    {
      ret.addItem(childCondition);
    }
    return ret;
  }

  private List<AbstractWorldEventCondition> getConditions(CompoundWorldEventCondition source)
  {
    List<AbstractWorldEventCondition> ret=new ArrayList<AbstractWorldEventCondition>();
    for(AbstractWorldEventCondition childCondition : source.getItems())
    {
      if (childCondition instanceof SimpleWorldEventCondition)
      {
        ret.add(childCondition);
      }
      else
      {
        ret.addAll(getConditions((CompoundWorldEventCondition)childCondition));
      }
    }
    return ret;
  }
}

