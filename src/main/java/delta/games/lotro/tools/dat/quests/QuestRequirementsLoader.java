package delta.games.lotro.tools.dat.quests;

import org.apache.log4j.Logger;

import delta.common.utils.collections.filters.Operator;
import delta.games.lotro.common.requirements.AbstractAchievableRequirement;
import delta.games.lotro.common.requirements.CompoundQuestRequirement;
import delta.games.lotro.common.requirements.QuestRequirement;
import delta.games.lotro.common.requirements.QuestStatus;
import delta.games.lotro.common.utils.ComparisonOperator;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.tools.dat.utils.OperatorUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for quest requirements.
 * @author DAM
 */
public class QuestRequirementsLoader
{
  private static final Logger LOGGER=Logger.getLogger(QuestRequirementsLoader.class);

  /**
   * ID of the "hiding content" quest.
   */
  public static final int HIDING_CONTENT_QUEST_ID=1879049597;

  private EnumMapper _operator;
  private EnumMapper _questStatus;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public QuestRequirementsLoader(DataFacade facade)
  {
    _operator=facade.getEnumsManager().getEnumMapper(587202617);
    _questStatus=facade.getEnumsManager().getEnumMapper(587202583);
  }

  private boolean isHidden(QuestRequirement questRequirement)
  {
    QuestStatus questStatus=questRequirement.getQuestStatus();
    int questID=questRequirement.getQuestId();
    if ((questStatus==QuestStatus.COMPLETED) && (questID==HIDING_CONTENT_QUEST_ID))
    {
      return true;
    }
    return false;
  }

/*
Quest 1879202431:
DefaultPermissionBlobStruct: 
  Usage_QuestRequirements_OrList: 
    #1: Usage_QuestRequirement 
      Usage_Operator: 3 (EqualTo)
      Usage_QuestID: 1879202423
      Usage_QuestStatus: 805306368 (Completed)
    #2: Usage_QuestRequirement 
      Usage_Operator: 3 (EqualTo)
      Usage_QuestID: 1879202429
      Usage_QuestStatus: 805306368 (Completed)
    #3: Usage_QuestRequirement 
      Usage_Operator: 3 (EqualTo)
      Usage_QuestID: 1879202365
      Usage_QuestStatus: 805306368 (Completed)
    #4: Usage_QuestRequirement 
      Usage_Operator: 3 (EqualTo)
      Usage_QuestID: 1879202351
      Usage_QuestStatus: 805306368 (Completed)
 * 
 */

  /**
   * Load quest requirements from raw properties.
   * @param achievable Parent achievable.
   * @param properties Input properties.
   * @return A quest requirement or <code>null</code> if none.
   */
  public AbstractAchievableRequirement loadQuestRequirements(Achievable achievable, PropertiesSet properties)
  {
    AbstractAchievableRequirement ret=null;
    AbstractAchievableRequirement abstractOr=null;
    Object[] orRequirement=(Object[])properties.getProperty("Usage_QuestRequirements_OrList");
    if (orRequirement!=null)
    {
      CompoundQuestRequirement or=new CompoundQuestRequirement(Operator.OR);
      loadQuestRequirements(achievable,orRequirement,or);
      abstractOr=cleanupRequirement(or);
    }
    AbstractAchievableRequirement abstractAnd=null;
    Object[] andRequirement=(Object[])properties.getProperty("Usage_QuestRequirements");
    if (andRequirement!=null)
    {
      CompoundQuestRequirement and=new CompoundQuestRequirement(Operator.AND);
      loadQuestRequirements(achievable,andRequirement,and);
      abstractAnd=cleanupRequirement(and);
    }
    if (abstractAnd!=null)
    {
      if (abstractOr!=null)
      {
        CompoundQuestRequirement compound=null;
        if (abstractAnd instanceof CompoundQuestRequirement)
        {
          compound=(CompoundQuestRequirement)abstractAnd;
        }
        else
        {
          compound=new CompoundQuestRequirement(Operator.AND);
          compound.addRequirement(abstractAnd);
        }
        compound.addRequirement(abstractOr);
        ret=compound;
      }
      else
      {
        ret=abstractAnd;
      }
    }
    else
    {
      if (abstractOr!=null)
      {
        ret=abstractOr;
      }
    }
    if (ret!=null)
    {
      AbstractAchievableRequirement ret2=cleanupRequirement(ret);
      return ret2;
    }
    return null;
  }

  private AbstractAchievableRequirement cleanupRequirement(AbstractAchievableRequirement input)
  {
    if (input==null)
    {
      return null;
    }
    if (input instanceof CompoundQuestRequirement)
    {
      CompoundQuestRequirement compoundRequirement=(CompoundQuestRequirement)input;
      int nbReqs=compoundRequirement.getRequirements().size();
      if (nbReqs==1)
      {
        return compoundRequirement.getRequirements().get(0);
      }
      if (nbReqs==0)
      {
        return null;
      }
    }
    return input;
  }

  /**
   * Deep requirement cleanup.
   * @param input Input requirement.
   * @return the cleaned up requirement.
   */
  public AbstractAchievableRequirement deepRequirementCleanup(AbstractAchievableRequirement input)
  {
    if (input==null)
    {
      return null;
    }
    if (input instanceof CompoundQuestRequirement)
    {
      CompoundQuestRequirement compoundRequirement=(CompoundQuestRequirement)input;
      int nbRequirements=compoundRequirement.getNumberOfRequirements();
      for(int i=0;i<nbRequirements;i++)
      {
        AbstractAchievableRequirement childRequirement=compoundRequirement.getRequirement(i);
        AbstractAchievableRequirement cleanedChildRequirement=deepRequirementCleanup(childRequirement);
        if (cleanedChildRequirement!=childRequirement)
        {
          if (cleanedChildRequirement==null)
          {
            compoundRequirement.removeRequirement(i);
            i--;
            nbRequirements--;
          }
          else
          {
            compoundRequirement.setRequirement(i,cleanedChildRequirement);
          }
        }
      }
      int nbReqs=compoundRequirement.getRequirements().size();
      if (nbReqs==1)
      {
        return compoundRequirement.getRequirements().get(0);
      }
      if (nbReqs==0)
      {
        return null;
      }
      return input;
    }
    else if (input instanceof QuestRequirement)
    {
      QuestRequirement requirement=(QuestRequirement)input;
      Proxy<Achievable> proxy=requirement.getRequiredAchievable();
      if (proxy==null)
      {
        return null;
      }
      Achievable resolvedAchievable=proxy.getObject();
      if (resolvedAchievable==null)
      {
        return null;
      }
      return requirement;
    }
    return null;
  }

  private void loadQuestRequirements(Achievable achievable, Object[] requirementItems, CompoundQuestRequirement storage)
  {
    for(Object requirementItem : requirementItems)
    {
      PropertiesSet questRequirementProps=(PropertiesSet)requirementItem;
      QuestRequirement questRequirement=loadQuestRequirement(questRequirementProps);
      if (questRequirement!=null)
      {
        boolean hidden=isHidden(questRequirement);
        if (hidden)
        {
          if (achievable!=null)
          {
            achievable.setHidden(true);
          }
        }
        else
        {
          storage.addRequirement(questRequirement);
        }
      }
    }
  }

  private QuestRequirement loadQuestRequirement(PropertiesSet questRequirementProps)
  {
    int operatorCode=((Integer)questRequirementProps.getProperty("Usage_Operator")).intValue();
    int questId=((Integer)questRequirementProps.getProperty("Usage_QuestID")).intValue();
    int questStatusCode=((Integer)questRequirementProps.getProperty("Usage_QuestStatus")).intValue();
    ComparisonOperator operator=OperatorUtils.getComparisonOperatorFromCode(operatorCode);
    if (operator==null)
    {
      LOGGER.warn("Unmanaged operator: "+operatorCode+": "+_operator.getLabel(operatorCode));
      return null;
    }
    QuestStatus questStatus=getQuestStatusFromCode(questStatusCode);
    if (questStatus==null) // Completed
    {
      LOGGER.warn("Unmanaged quest status: "+questStatusCode+": "+_questStatus.getLabel(questStatusCode));
      return null;
    }
    QuestRequirement ret=new QuestRequirement(questId,questStatus);
    ret.setOperator(operator);
    return ret;
  }

  private QuestStatus getQuestStatusFromCode(int questStatus)
  {
    if (questStatus==0x10000000) return QuestStatus.UNDERWAY;
    if (questStatus==0x20000000) return QuestStatus.FAILED;
    if (questStatus==0x30000000) return QuestStatus.COMPLETED;
    //if (questStatus==0x40000000) return QuestStatus.ABANDONED;
    //if (questStatus==0x50000000) return QuestStatus.TAPPED;
    if ((questStatus&0x10000000)==0x10000000)
    {
      int objectiveIndex=questStatus&0xFFFFFFF;
      return QuestStatus.getUnderwayObjective(objectiveIndex);
    }
    return null;
  }
}
