package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyDefinition;
import delta.games.lotro.lore.items.Item;

/**
 * Loader for item sorting data.
 * @author DAM
 */
public class ItemSortingDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(ItemSortingDataLoader.class);

  private Map<Integer,List<String>> _sortCriterias;
  private DataFacade _facade;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public ItemSortingDataLoader(DataFacade facade)
  {
    _sortCriterias=new HashMap<Integer,List<String>>();
    _facade=facade;
    loadSortCriterias();
  }

  private void loadSortCriterias()
  {
    // InventoryControl
    PropertiesSet props=_facade.loadProperties(0x79000230);
    Object[] sortInfoArray=(Object[])props.getProperty("InventoryControl_SmartSortInfo_Array");
    if (sortInfoArray==null)
    {
      return;
    }
    PropertiesRegistry propsRegistry=_facade.getPropertiesRegistry();
    for(Object sortInfoEntry : sortInfoArray)
    {
      PropertiesSet entryProps=(PropertiesSet)sortInfoEntry;
      /*
    Item_Client_Sort_PropertyNameArray: 
      #1: 268436135 (Item_Quality)
      #2: 268438017 (Item_Class)
      #3: 268437097 (Item_Level)
    Item_Client_Sort_Type: 3 (WSL_Otherwise_Unassigned_Item_AutoApplied)
       */
      Integer sortType=(Integer)entryProps.getProperty("Item_Client_Sort_Type");
      Object[] propertiesArray=(Object[])entryProps.getProperty("Item_Client_Sort_PropertyNameArray");
      List<String> propertyNames=new ArrayList<String>();
      for(Object propertyIDObj : propertiesArray)
      {
        Integer propertyID=(Integer)propertyIDObj;
        PropertyDefinition propDefinition=propsRegistry.getPropertyDef(propertyID.intValue());
        if (propDefinition!=null)
        {
          String propertyName=propDefinition.getName();
          propertyNames.add(propertyName);
        }
        else
        {
          LOGGER.warn("Could not find property with ID: "+propertyID);
        }
      }
      _sortCriterias.put(sortType,propertyNames);
    }
  }

  /**
   * Handle an item.
   * @param item Item to use.
   * @param properties Properties to use.
   */
  public void handleItem(Item item, PropertiesSet properties)
  {
    Integer sortType=(Integer)properties.getProperty("Item_Client_Sort_Type");
    if (sortType==null)
    {
      return;
    }
    /*
    List<String> sortProperties=_sortCriterias.get(sortType);
    List<Object> values=new ArrayList<Object>();
    for(String sortProperty : sortProperties)
    {
      values.add(properties.getProperty(sortProperty));
    }
    System.out.println("Item: "+item+" => "+values);
    */
  }
}
