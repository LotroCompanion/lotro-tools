package delta.games.lotro.tools.dat.others;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.treasure.FilteredTrophyTable;
import delta.games.lotro.common.treasure.FilteredTrophyTableEntry;
import delta.games.lotro.common.treasure.ItemsTable;
import delta.games.lotro.common.treasure.ItemsTableEntry;
import delta.games.lotro.common.treasure.TreasureGroupProfile;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TreasureListEntry;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.TrophyListEntry;
import delta.games.lotro.common.treasure.WeightedTreasureTable;
import delta.games.lotro.common.treasure.WeightedTreasureTableEntry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.tools.dat.utils.ProxyBuilder;
import delta.games.lotro.utils.Proxy;

/**
 * Loader for loot data.
 * @author DAM
 */
public class LootLoader
{
  private static final Logger LOGGER=Logger.getLogger(LootLoader.class);

  private DataFacade _facade;
  private EnumMapper _dropFrequency;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LootLoader(DataFacade facade)
  {
    _facade=facade;
    _dropFrequency=facade.getEnumsManager().getEnumMapper(587202656);
  }

  /**
   * Handle a weighted treasure table.
   * @param id Table identifier.
   * @return a weighted treasure table or <code>null</code>.
   */
  public WeightedTreasureTable handleWeightedTreasureTable(int id)
  {
    WeightedTreasureTable ret=null;
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    Object[] treasureTable=(Object[])properties.getProperty("LootGen_WeightedTreasureTable");
    if (treasureTable!=null)
    {
      ret=new WeightedTreasureTable(id);
      for(Object treasureTableItem : treasureTable)
      {
        PropertiesSet treasureTableItemProps=(PropertiesSet)treasureTableItem;
        int weight=((Integer)treasureTableItemProps.getProperty("LootGen_WeightedTreasureTable_TrophyList_Weight")).intValue();
        int trophyListId=((Integer)treasureTableItemProps.getProperty("LootGen_WeightedTreasureTable_TrophyList")).intValue();
        TrophyList trophyList=handleTrophyList(trophyListId);
        WeightedTreasureTableEntry entry=new WeightedTreasureTableEntry(weight,trophyList);
        ret.addEntry(entry);
      }
    }
    return ret;
  }

  /**
   * Handle a filtered treasure list.
   * @param id Identifier.
   * @return a table.
   */
  public FilteredTrophyTable handleFilteredTreasureList(int id)
  {
    FilteredTrophyTable ret=null;
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=new FilteredTrophyTable(id);
      Object[] list=(Object[])properties.getProperty("LootGen_FilteredTreasureListArray");
      for(Object listItemObj : list)
      {
        PropertiesSet itemProps=(PropertiesSet)listItemObj;
        //System.out.println(itemProps.dump());
        int lootTableId=((Integer)itemProps.getProperty("LootGen_FilteredTrophyTable_EntryDID")).intValue();
        TrophyList trophyList=handleTrophyList(lootTableId);
        WeightedTreasureTable treasureTable=handleWeightedTreasureTable(lootTableId);
        FilteredTrophyTableEntry entry=new FilteredTrophyTableEntry(trophyList,treasureTable);
        ret.addEntry(entry);

        // Filter
        Object[] filter=(Object[])itemProps.getProperty("EntityFilter_Array");
        loadFilterData(filter,entry.getUsageRequirement());
      }
    }
    return ret;
  }

  private void loadFilterData(Object[] filterArray, UsageRequirement requirements)
  {
    if (filterArray!=null)
    {
      int size=filterArray.length;
      // Item #1: class filter?
      if (size>=1)
      {
        Object[] classIdsArray=(Object[])filterArray[0];
        for(Object classIdObj : classIdsArray)
        {
          int classId=((Integer)classIdObj).intValue();
          CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classId);
          requirements.addAllowedClass(characterClass);
        }
      }
      // Item #2: never used
      // Item #3: level range
      if (size>=3)
      {
        PropertiesSet levelProps=(PropertiesSet)filterArray[2];
        handleLevelFilter(levelProps,requirements);
      }
      if (size>3)
      {
        LOGGER.warn("Unsupported size: "+size);
      }
    }
  }

  private void handleLevelFilter(PropertiesSet levelProps, UsageRequirement requirements)
  {
    /*
    EntityFilter_PropertyRange_Max: 
      #1: 39
    EntityFilter_PropertyRange_Min: 
      #1: 30
     */
    Object[] minArray=(Object[])levelProps.getProperty("EntityFilter_PropertyRange_Min");
    Object[] maxArray=(Object[])levelProps.getProperty("EntityFilter_PropertyRange_Max");
    if ((minArray!=null) && (maxArray!=null))
    {
      int nbRanges=Math.min(minArray.length,maxArray.length);
      for(int i=0;i<nbRanges;i++)
      {
        int min=((Integer)minArray[i]).intValue();
        requirements.setMinLevel(Integer.valueOf(min));
        int max=((Integer)maxArray[i]).intValue();
        requirements.setMaxLevel(Integer.valueOf(max));
      }
    }
  }

  /**
   * Handle a trophy list.
   * @param id List identifier.
   * @return a trophy list or <code>null</code>.
   */
  public TrophyList handleTrophyList(int id)
  {
    TrophyList ret=null;
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(properties.dump());
    Object[] trophyList=(Object[])properties.getProperty("LootGen_TrophyList");
    if (trophyList!=null)
    {
      ret=new TrophyList(id);
      for(Object trophyObj : trophyList)
      {
        PropertiesSet trophyProps=(PropertiesSet)trophyObj;
        // Probability
        int frequencyCode=((Integer)trophyProps.getProperty("LootGen_TrophyList_DropFrequency")).intValue();
        float probability=getProbability(frequencyCode);
        // Quantity
        Integer quantityInt=(Integer)trophyProps.getProperty("LootGen_TrophyList_ItemQuantity");
        int quantity=(quantityInt!=null)?quantityInt.intValue():1;
        int itemOrProfile=((Integer)trophyProps.getProperty("LootGen_TrophyList_ItemOrProfile")).intValue();
        Proxy<Item> itemProxy=null;
        TreasureGroupProfile treasureGroup=null;
        String itemName=getItemName(itemOrProfile);
        if (itemName!=null)
        {
          // Item
          itemProxy=ProxyBuilder.buildItemProxy(itemOrProfile);
          if (itemProxy==null)
          {
            itemProxy=new Proxy<Item>();
            itemProxy.setId(itemOrProfile);
            itemProxy.setName(itemName);
          }
        }
        else
        {
          // or treasure group
          treasureGroup=handleTreasureGroupProfile(itemOrProfile);
        }
        if ((itemProxy==null) && (treasureGroup==null))
        {
          LOGGER.warn("Could not find item or treasure group for ID: "+itemOrProfile);
        }
        TrophyListEntry entry=new TrophyListEntry(probability,itemProxy,treasureGroup,quantity);
        ret.addEntry(entry);
      }
    }
    return ret;
  }

  private String getItemName(int id)
  {
    String ret=null;
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      ret=DatUtils.getStringProperty(properties,"Name");
    }
    return ret;
  }

  private TreasureGroupProfile handleTreasureGroupProfile(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    ItemsTable itemsTable=handleItemsTable(id,properties);
    TreasureList treasureList=handleTreasureList(id,properties);
    if ((itemsTable==null) && (treasureList==null))
    {
      System.out.println("**********************");
      System.out.println(properties.dump());
      // Sometimes we get here with an ID of an unknown item (ex: 1879347509 TBD eq_u21_rar_guardian_tank_T3_set_a_shield)
    }
    TreasureGroupProfile ret=new TreasureGroupProfile(id,itemsTable,treasureList);
    return ret;
  }

  private ItemsTable handleItemsTable(int id, PropertiesSet properties)
  {
    ItemsTable ret=null;
    Object[] itemTableArray=(Object[])properties.getProperty("TreasureGroupProfile_ItemTable");
    if (itemTableArray!=null)
    {
      ret=new ItemsTable(id);
      for(Object itemTableEntryObj : itemTableArray)
      {
        PropertiesSet entryProps=(PropertiesSet)itemTableEntryObj;
        int weight=((Integer)entryProps.getProperty("TreasureGroupProfile_ItemTable_Weight")).intValue();
        int itemId=((Integer)entryProps.getProperty("TreasureGroupProfile_ItemTable_Item")).intValue();
        Integer quantityInt=(Integer)entryProps.getProperty("TreasureGroupProfile_ItemTable_Quantity");
        int quantity=(quantityInt!=null)?quantityInt.intValue():1;
        Proxy<Item> itemProxy=ProxyBuilder.buildItemProxy(itemId);
        if (itemProxy!=null)
        {
          ItemsTableEntry entry=new ItemsTableEntry(weight,itemProxy,quantity);
          ret.addEntry(entry);
        }
      }
    }
    return ret;
  }

  /**
   * Handle a treasure list.
   * @param id Identifier.
   * @param properties Input data.
   * @return A treasure list or <code>null</code>.
   */
  public TreasureList handleTreasureList(int id, PropertiesSet properties)
  {
    TreasureList ret=null;
    Object[] treasureList=(Object[])properties.getProperty("LootGen_TreasureList");
    if (treasureList!=null)
    {
      ret=new TreasureList(id);
      for(Object treasureObj : treasureList)
      {
        PropertiesSet treasureProps=(PropertiesSet)treasureObj;
        int weight=((Integer)treasureProps.getProperty("LootGen_TreasureList_Weight")).intValue();
        int profile=((Integer)treasureProps.getProperty("LootGen_TreasureList_GroupProfile")).intValue();
        TreasureGroupProfile treasureGroupProfile=handleTreasureGroupProfile(profile);
        TreasureListEntry entry=new TreasureListEntry(weight,treasureGroupProfile);
        ret.addEntry(entry);
      }
    }
    return ret;
  }

  private float getProbability(int frequencyCode)
  {
    /*String frequencyStr=*/_dropFrequency.getString(frequencyCode);
    return 1.0f;
  }
}
