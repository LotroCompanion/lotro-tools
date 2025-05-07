package delta.games.lotro.tools.extraction.common.worldEvents;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.collections.filters.Operator;
import delta.games.lotro.common.utils.ComparisonOperator;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.lore.worldEvents.CompoundWorldEventCondition;
import delta.games.lotro.lore.worldEvents.SimpleWorldEventCondition;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.tools.extraction.utils.OperatorUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for world event conditions.
 * @author DAM
 */
public class WorldEventConditionsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(WorldEventConditionsLoader.class);

  private WorldEventsLoader _loader;
  private WorldEventConditionNormalizer _normalizer;

  /**
   * Constructor.
   * @param loader World events loader.
   */
  public WorldEventConditionsLoader(WorldEventsLoader loader)
  {
    _loader=loader;
    _normalizer=new WorldEventConditionNormalizer();
  }

  /**
   * Load world events conditions.
   * @param properties Source properties.
   * @return the loaded condition or <code>null</code>.
   */
  public AbstractWorldEventCondition loadWorldEventsUsageConditions(PropertiesSet properties)
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
  public AbstractWorldEventCondition loadWorldEventsConditions(PropertiesSet properties, String allConditionPropertyName, String anyConditionPropertyName)
  {
    AbstractWorldEventCondition ret=extractWorldEventsConditions(properties,allConditionPropertyName,anyConditionPropertyName);
    ret=_normalizer.normalize(ret);
    return ret;
  }

  private AbstractWorldEventCondition extractWorldEventsConditions(PropertiesSet properties, String allConditionPropertyName, String anyConditionPropertyName)
  {
    AbstractWorldEventCondition andCondition=loadWorldEventList(properties,Operator.AND,allConditionPropertyName);
    AbstractWorldEventCondition orCondition=loadWorldEventList(properties,Operator.OR,anyConditionPropertyName);
    if ((andCondition!=null) && (orCondition!=null))
    {
      CompoundWorldEventCondition ret=new CompoundWorldEventCondition(Operator.AND);
      ret.addItem(andCondition);
      ret.addItem(orCondition);
      return ret;
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

  private AbstractWorldEventCondition loadWorldEventList(PropertiesSet properties, Operator operator, String propertyName)
  {
    Object[] list=(Object[])properties.getProperty(propertyName);
    if (list!=null)
    {
      CompoundWorldEventCondition ret=new CompoundWorldEventCondition(operator);
      for(Object entryObj : list)
      {
        PropertiesSet entryProps=(PropertiesSet)entryObj;
        SimpleWorldEventCondition element=handleWorldEventCondition(entryProps);
        if (element!=null)
        {
          ret.addItem(element);
        }
      }
      List<AbstractWorldEventCondition> conditions=ret.getItems();
      if (conditions.size()==1)
      {
        return conditions.get(0);
      }
      return ret;
    }
    return null;
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

  /**
   * Parse a simple world event condition from some properties.
   * @param props Input properties.
   * @return A world event condition or <code>null</code>.
   */
  public SimpleWorldEventCondition handleWorldEventCondition(PropertiesSet props)
  {
    SimpleWorldEventCondition ret=null;
    int operatorCode=((Integer)props.getProperty("WorldEvent_Operator")).intValue();
    ComparisonOperator operator=OperatorUtils.getComparisonOperatorFromCode(operatorCode);
    Integer worldEventID=(Integer)props.getProperty("WorldEvent_WorldEvent");
    Proxy<WorldEvent> worldEvent=buildWorldEventProxy(worldEventID.intValue());
    Integer conditionValue=(Integer)props.getProperty("WorldEvent_ConditionValue");
    Integer idToCompareWith=(Integer)props.getProperty("WorldEvent_Condition_WorldEventToCompareWith");
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Condition: {}, operator={}",worldEventID,operator);
    }
    // If idToCompareWith is not null, then conditionValue is null
    if (idToCompareWith!=null)
    {
      Proxy<WorldEvent> comparetToWorldEvent=buildWorldEventProxy(idToCompareWith.intValue());
      ret=new SimpleWorldEventCondition(operator,worldEvent,comparetToWorldEvent);
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\tCompare with world event: {}",idToCompareWith);
      }
    }
    else if (conditionValue!=null)
    {
      ret=new SimpleWorldEventCondition(operator,worldEvent,conditionValue.intValue());
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("\tCompare with value: {}",conditionValue);
      }
    }
    else
    {
      LOGGER.warn("Unexpected case: no value and no 'compare with'");
    }
    String usageString=(String)props.getProperty("WorldEvent_Condition_UsageString");
    if (usageString!=null)
    {
      // Not used
      LOGGER.warn("\tUsage Info: {}",usageString);
    }
    return ret;
  }
}
