package delta.games.lotro.tools.extraction.misc;

import java.util.HashSet;
import java.util.Set;

import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.effects.EffectLoader;

/**
 * Loader for propery response maps.
 * @author DAM
 */
public class PropertyResponseMapsLoader
{
  private DataFacade _facade;
  private EffectLoader _effectsLoader;
  private Set<Integer> _handledMaps;
  private Set<Integer> _handledCallbacks;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param effectsLoader Effects loader.
   */
  public PropertyResponseMapsLoader(DataFacade facade, EffectLoader effectsLoader)
  {
    _facade=facade;
    _effectsLoader=effectsLoader;
    _handledMaps=new HashSet<Integer>();
    _handledCallbacks=new HashSet<Integer>();
  }

  /**
   * Handle a property response map.
   * @param propertyResponseMapID Identifier of the map to load.
   */
  public void handlePropertyResponseMap(int propertyResponseMapID)
  {
    Integer key=Integer.valueOf(propertyResponseMapID);
    if (_handledMaps.contains(key))
    {
      return;
    }
    /*
PropertyResponse_WorldEventResponse_List:
  #1: PropertyResponse_WorldEventResponse_Entry
    PropertyResponse_Callback: 1879145607
    PropertyResponse_WorldEvent: 1879145610
    ...
     */
    PropertiesSet props=_facade.loadProperties(propertyResponseMapID+DATConstants.DBPROPERTIES_OFFSET);
    Object[] array=(Object[])props.getProperty("PropertyResponse_WorldEventResponse_List");
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      @SuppressWarnings("unused")
      int worldEventID=((Integer)entryProps.getProperty("PropertyResponse_WorldEvent")).intValue();
      int callbackID=((Integer)entryProps.getProperty("PropertyResponse_Callback")).intValue();
      handlePropertyResponseCallback(callbackID);
    }
    _handledMaps.add(key);
  }

  @SuppressWarnings("unused")
  private void handlePropertyResponseCallback(int callbackID)
  {
    Integer key=Integer.valueOf(callbackID);
    if (_handledCallbacks.contains(key))
    {
      return;
    }
    /*
DefaultPermissionBlobStruct:
  Usage_ValidClassificationType: 5 (Player)
PropertyResponseCallback_AppliedEffect_Array:
  #1: PropertyResponseCallback_AppliedEffect_Entry
    PropertyResponseCallback_AppliedEffect_List:
      #1: PropertyResponseCallback_AppliedEffect 1879145604
    PropertyResponseCallback_Ceiling: 1
    PropertyResponseCallback_Floor: 1
  #2: PropertyResponseCallback_AppliedEffect_Entry
    PropertyResponseCallback_AppliedEffect_List:
      #1: PropertyResponseCallback_AppliedEffect 1879145605
    PropertyResponseCallback_Ceiling: 2
    PropertyResponseCallback_Floor: 2
PropertyResponseCallback_UseTargetLevelForSpellcraft: 1
PropertyResponse_Filter_Array:
  #1: PropertyResponse_Filter_Alignment 1 (Good)
     */
    PropertiesSet props=_facade.loadProperties(callbackID+DATConstants.DBPROPERTIES_OFFSET);
    Object[] array=(Object[])props.getProperty("PropertyResponseCallback_AppliedEffect_Array");
    for(Object entry : array)
    {
      PropertiesSet entryProps=(PropertiesSet)entry;
      Integer floor=(Integer)entryProps.getProperty("PropertyResponseCallback_Floor");
      Integer ceiling=(Integer)entryProps.getProperty("PropertyResponseCallback_Ceiling");
      Object[] effectsArray=(Object[])entryProps.getProperty("PropertyResponseCallback_AppliedEffect_List");
      for(Object effectEntry : effectsArray)
      {
        int effectID=((Integer)effectEntry).intValue();
        _effectsLoader.getEffect(effectID);
      }
    }
    _handledCallbacks.add(key);
  }
}
