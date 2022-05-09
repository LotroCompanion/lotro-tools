package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.lore.worldEvents.AbstractWorldEventCondition;
import delta.games.lotro.lore.worldEvents.BooleanWorldEvent;
import delta.games.lotro.lore.worldEvents.ConditionWorldEvent;
import delta.games.lotro.lore.worldEvents.IntegerWorldEvent;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.tools.dat.utils.WorldEventConditionsLoader;

/**
 * Loader for world events.
 * @author DAM
 */
public class WorldEventsLoader
{
  private DataFacade _facade;
  private Map<Integer,WorldEvent> _registry;
  private WorldEventConditionsLoader _weConditionsLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public WorldEventsLoader(DataFacade facade)
  {
    _facade=facade;
    _registry=new HashMap<Integer,WorldEvent>();
    _weConditionsLoader=new WorldEventConditionsLoader(this);
  }

  /**
   * Get the managed world events.
   * @return a list of world events, sorted by identifier.
   */
  public List<WorldEvent> getWorldEvents()
  {
    List<WorldEvent> ret=new ArrayList<WorldEvent>();
    ret.addAll(_registry.values());
    Collections.sort(ret,new IdentifiableComparator<WorldEvent>());
    return ret;
  }

  /**
   * Get a world event (load it if necessary).
   * @param worldEventId Identifier of the world event to get.
   * @return A world event or <code>null</code> if not found.
   */
  public WorldEvent getWorldEvent(int worldEventId)
  {
    Integer key=Integer.valueOf(worldEventId);
    if (_registry.containsKey(key))
    {
      return _registry.get(key);
    }
    WorldEvent ret=handleWorldEvent(worldEventId);
    _registry.put(key,ret);
    return ret;
  }

  private WorldEvent handleWorldEvent(int worldEventId)
  {
    WorldEvent ret=null;
    PropertiesSet props=_facade.loadProperties(worldEventId+DATConstants.DBPROPERTIES_OFFSET);
    int propertyID=((Integer)props.getProperty("WorldEvent_WorldPropertyName")).intValue();
    PropertyDefinition propertyDefinition=_facade.getPropertiesRegistry().getPropertyDef(propertyID);
    String name=propertyDefinition.getName();
    //System.out.println("\tPropertyID="+propertyID+", name: "+name);
    PropertyType type=propertyDefinition.getPropertyType();
    if (type==PropertyType.INT)
    {
      Integer minValue=(Integer)props.getProperty("WorldEvent_MinIntValue");
      int maxValue=((Integer)props.getProperty("WorldEvent_MaxIntValue")).intValue();
      Integer defaultValue=(Integer)props.getProperty("WorldEvent_DefaultIntValue");
      //System.out.println("\tINTEGER Min="+minValue+", Max="+maxValue+", Default="+defaultValue);
      IntegerWorldEvent integerWE=new IntegerWorldEvent();
      integerWE.setDefaultValue(defaultValue);
      integerWE.setMinValue(minValue);
      integerWE.setMaxValue(Integer.valueOf(maxValue));
      ret=integerWE;
    }
    else if (type==PropertyType.BOOLEAN)
    {
      Integer defaultValueInt=(Integer)props.getProperty("WorldEvent_DefaultBoolValue");
      //System.out.println("\tBOOLEAN Default="+defaultValue);
      AbstractWorldEventCondition condition=_weConditionsLoader.loadWorldEventsConditions(props,"WorldEvent_AllConditionList","WorldEvent_AnyConditionList");
      if (condition!=null)
      {
        ConditionWorldEvent conditionWE=new ConditionWorldEvent();
        conditionWE.setCondition(condition);
        ret=conditionWE;
      }
      else
      {
        BooleanWorldEvent booleanWE=new BooleanWorldEvent();
        boolean defaultValue=false;
        if (defaultValueInt!=null)
        {
          defaultValue=(defaultValueInt.intValue()!=0);
        }
        booleanWE.setDefaultValue(defaultValue);
        ret=booleanWE;
      }
    }
    ret.setIdentifier(worldEventId);
    ret.setPropertyID(propertyID);
    ret.setPropertyName(name);
    // Description
    String description=(String)props.getProperty("WorldEvent_DescriptionString");
    if (description!=null)
    {
      //System.out.println("\tDescription: "+description);
      ret.setDescription(description);
    }
    // Progress
    String progress=(String)props.getProperty("WorldEvent_ProgressString");
    if (progress!=null)
    {
      //System.out.println("\tProgress: "+progress);
      ret.setProgress(progress);
    }
    return ret;
  }
}
