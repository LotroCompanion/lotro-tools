package delta.games.lotro.tools.dat.utils;

import org.apache.log4j.Logger;

import delta.common.utils.collections.filters.Operator;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.utils.ComparisonOperator;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.lore.worldEvents.WorldEventBooleanCondition;
import delta.games.lotro.lore.worldEvents.WorldEventConditionItem;
import delta.games.lotro.tools.dat.misc.WorldEventsLoader;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for world event conditions.
 * @author DAM
 */
public class WorldEventConditionsLoader
{
  private static final Logger LOGGER=Logger.getLogger(WorldEventConditionsLoader.class);

  private WorldEventsLoader _loader;

  /**
   * Constructor.
   * @param loader World events loader.
   */
  public WorldEventConditionsLoader(WorldEventsLoader loader)
  {
    _loader=loader;
  }

  /**
   * Handle world events requirement.
   * @param properties Source properties.
   * @param requirements Storage for loaded data.
   */
  public void loadWorldEventsUsageRequirements(PropertiesSet properties, UsageRequirement requirements)
  {
    WorldEventBooleanCondition condition=loadWorldEventsUsageConditions(properties);
    if (condition!=null)
    {
      System.out.println(condition);
    }
  }

  /**
   * Load world events conditions.
   * @param properties Source properties.
   * @return the loaded condition or <code>null</code>.
   */
  public WorldEventBooleanCondition loadWorldEventsUsageConditions(PropertiesSet properties)
  {
    return loadWorldEventsConditions(properties,"Usage_WorldEvent_AllConditionList","Usage_WorldEvent_AnyConditionList");
  }

  /**
   * Load world events conditions.
   * @param properties Source properties.
   * @param allConditionPropertyName Name of the "all conditions" property.
   * @param anyConditionPropertyName Name of the "any condition" property.
   * @return the loaded condition or <code>null</code>.
   */
  public WorldEventBooleanCondition loadWorldEventsConditions(PropertiesSet properties, String allConditionPropertyName, String anyConditionPropertyName)
  {
    WorldEventBooleanCondition andCondition=loadWorldEventList(properties,Operator.AND,allConditionPropertyName);
    WorldEventBooleanCondition orCondition=loadWorldEventList(properties,Operator.OR,anyConditionPropertyName);
    if ((andCondition!=null) && (orCondition!=null))
    {
      LOGGER.warn("Both AND and OR world event condition: AND="+andCondition+", OR="+orCondition);
      System.out.println(properties.dump());
    }
    if (andCondition!=null)
    {
      return andCondition;
    }
    if (orCondition!=null)
    {
      return orCondition;
    }
    return null;
  }

  private WorldEventBooleanCondition loadWorldEventList(PropertiesSet properties, Operator operator, String propertyName)
  {
    WorldEventBooleanCondition ret=null;
    Object[] allList=(Object[])properties.getProperty(propertyName);
    if (allList!=null)
    {
      ret=new WorldEventBooleanCondition(operator);
      for(Object entryPropsObj : allList)
      {
        //System.out.println(operator);
        PropertiesSet entryProps=(PropertiesSet)entryPropsObj;
        WorldEventConditionItem element=handleWorldEventCondition(entryProps);
        if (element!=null)
        {
          ret.addItem(element);
        }
      }
    }
    return ret;
  }

  private Proxy<WorldEvent> buildWorldEventProxy(int worldEventID)
  {
    WorldEvent worldEvent=_loader.getWorldEvent(worldEventID);
    if (worldEvent!=null)
    {
      Proxy<WorldEvent> ret=new Proxy<WorldEvent>();
      ret.setId(worldEventID);
      ret.setObject(worldEvent);
      ret.setName(worldEvent.getPropertyName());
      return ret;
    }
    return null;
  }

  private WorldEventConditionItem handleWorldEventCondition(PropertiesSet props)
  {
    WorldEventConditionItem ret=null;
    int operatorCode=((Integer)props.getProperty("WorldEvent_Operator")).intValue();
    ComparisonOperator operator=OperatorUtils.getComparisonOperatorFromCode(operatorCode);
    int worldEventID=((Integer)props.getProperty("WorldEvent_WorldEvent")).intValue();
    Proxy<WorldEvent> worldEvent=buildWorldEventProxy(worldEventID);
    Integer conditionValue=(Integer)props.getProperty("WorldEvent_ConditionValue");
    Integer idToCompareWith=(Integer)props.getProperty("WorldEvent_Condition_WorldEventToCompareWith");
    String usageString=(String)props.getProperty("WorldEvent_Condition_UsageString");
    //System.out.print("Condition: "+worldEventID+", operator="+operator);
    if (idToCompareWith!=null)
    {
      Proxy<WorldEvent> comparetToWorldEvent=buildWorldEventProxy(idToCompareWith.intValue());
      ret=new WorldEventConditionItem(operator,worldEvent,comparetToWorldEvent);
      //System.out.println("\tCompare with world event: "+idToCompareWith);
    }
    else if (conditionValue!=null)
    {
      ret=new WorldEventConditionItem(operator,worldEvent,conditionValue.intValue());
      //System.out.println("\tCompare with value: "+conditionValue);
    }
    else
    {
      LOGGER.warn("Unexpected case: no value and no 'compare with'");
    }
    // If idToCompareWith is not null, then conditionValue is null
    if (usageString!=null)
    {
      // Not used
      LOGGER.warn("\tUsage Info: "+usageString);
    }
    return ret;
  }
}
