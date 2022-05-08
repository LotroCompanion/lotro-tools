package delta.games.lotro.tools.dat.misc;

import java.util.HashMap;
import java.util.Map;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.dat.data.PropertyType;
import delta.games.lotro.lore.worldEvents.WorldEvent;
import delta.games.lotro.lore.worldEvents.WorldEventBooleanCondition;
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

  @SuppressWarnings("unused")
  private WorldEvent handleWorldEvent(int worldEventId)
  {
    //System.out.println("World Event: "+worldEventId);
    PropertiesSet props=_facade.loadProperties(worldEventId+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(props.dump());
    int propertyID=((Integer)props.getProperty("WorldEvent_WorldPropertyName")).intValue();

    WorldEvent ret=new WorldEvent(worldEventId,propertyID);
    PropertyDefinition propertyDefinition=_facade.getPropertiesRegistry().getPropertyDef(propertyID);
    String name=propertyDefinition.getName();
    ret.setPropertyName(name);
    //System.out.println("\tPropertyID="+propertyID+", name: "+name);
    PropertyType type=propertyDefinition.getPropertyType();
    if (type==PropertyType.INT)
    {
      Integer minValue=(Integer)props.getProperty("WorldEvent_MinIntValue");
      int maxValue=((Integer)props.getProperty("WorldEvent_MaxIntValue")).intValue();
      Integer defaultValue=(Integer)props.getProperty("WorldEvent_DefaultIntValue");
      //System.out.println("\tINTEGER Min="+minValue+", Max="+maxValue+", Default="+defaultValue);
    }
    else if (type==PropertyType.BOOLEAN)
    {
      Integer defaultValue=(Integer)props.getProperty("WorldEvent_DefaultBoolValue");
      //System.out.println("\tBOOLEAN Default="+defaultValue);
    }
    String description=(String)props.getProperty("WorldEvent_DescriptionString");
    if (description!=null)
    {
      //System.out.println("\tDescription: "+description);
      ret.setDescription(description);
    }
    String progress=(String)props.getProperty("WorldEvent_ProgressString");
    if (progress!=null)
    {
      //System.out.println("\tProgress: "+progress);
      ret.setProgress(progress);
    }
    WorldEventBooleanCondition condition=_weConditionsLoader.loadWorldEventsConditions(props,"WorldEvent_AllConditionList","WorldEvent_AnyConditionList");
    ret.setValueComputer(condition);
    return ret;
  }
}
