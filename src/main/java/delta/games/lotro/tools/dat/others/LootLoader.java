package delta.games.lotro.tools.dat.others;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.treasure.FilteredTrophyTable;
import delta.games.lotro.common.treasure.FilteredTrophyTableEntry;
import delta.games.lotro.common.treasure.ItemsTable;
import delta.games.lotro.common.treasure.ItemsTableEntry;
import delta.games.lotro.common.treasure.LootsManager;
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
  private Map<Integer,Float> _probabilities;
  private LootsManager _lootsMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootsMgr Loots manager.
   */
  public LootLoader(DataFacade facade, LootsManager lootsMgr)
  {
    _facade=facade;
    _dropFrequency=facade.getEnumsManager().getEnumMapper(587202656);
    _probabilities=new HashMap<Integer,Float>();
    _lootsMgr=lootsMgr;
    loadProbabilities();
  }

  private void loadProbabilities()
  {
    // LootGenControl:
    PropertiesSet properties=_facade.loadProperties(1879076022+DATConstants.DBPROPERTIES_OFFSET);
    Object[] tableArray=(Object[])properties.getProperty("LootGenControl_DropFrequencyTable");
    for(Object tableEntryObj : tableArray)
    {
      PropertiesSet entryProps=(PropertiesSet)tableEntryObj;
      float percentage=((Float)entryProps.getProperty("LootGenControl_DropFrequency_Percentage")).floatValue();
      int code=((Integer)entryProps.getProperty("LootGenControl_DropFrequency_Label")).intValue();
      _probabilities.put(Integer.valueOf(code),Float.valueOf(percentage));
      System.out.println("Probability is "+percentage*100+" for "+_dropFrequency.getString(code));
    }
  }

  /**
   * Handle a weighted treasure table.
   * @param id Table identifier.
   * @return a weighted treasure table or <code>null</code>.
   */
  public WeightedTreasureTable handleWeightedTreasureTable(int id)
  {
    WeightedTreasureTable ret=_lootsMgr.getWeightedTreasureTables().getItem(id);
    if (ret==null)
    {
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
        _lootsMgr.getWeightedTreasureTables().add(ret);
      }
    }
    return ret;
  }

  /**
   * Handle a filtered trophy list list.
   * @param id Identifier.
   * @return a table.
   */
  public FilteredTrophyTable handleFilteredTrophyTable(int id)
  {
    FilteredTrophyTable ret=_lootsMgr.getFilteredTrophyTables().getItem(id);
    if (ret==null)
    {
      PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
      if (properties!=null)
      {
        ret=new FilteredTrophyTable(id);
        Object[] list=(Object[])properties.getProperty("LootGen_FilteredTreasureListArray");
        for(Object listItemObj : list)
        {
          PropertiesSet itemProps=(PropertiesSet)listItemObj;
          int lootTableId=((Integer)itemProps.getProperty("LootGen_FilteredTrophyTable_EntryDID")).intValue();
          TrophyList trophyList=handleTrophyList(lootTableId);
          WeightedTreasureTable treasureTable=handleWeightedTreasureTable(lootTableId);
          FilteredTrophyTableEntry entry=new FilteredTrophyTableEntry(trophyList,treasureTable);
          ret.addEntry(entry);
          // Filter
          Object[] filter=(Object[])itemProps.getProperty("EntityFilter_Array");
          loadFilterData(filter,entry.getUsageRequirement());
        }
        _lootsMgr.getFilteredTrophyTables().add(ret);
      }
    }
    return ret;
  }

  private void loadFilterData(Object[] filterArray, UsageRequirement requirements)
  {
    if (filterArray!=null)
    {
      int size=filterArray.length;
      for(Object filterEntry : filterArray)
      {
        if (filterEntry instanceof PropertiesSet)
        {
          // Level filter
          PropertiesSet levelProps=(PropertiesSet)filterEntry;
          handleLevelFilter(levelProps,requirements);
        }
        else
        {
          // Class filter
          Object[] classIdsArray=(Object[])filterArray[0];
          for(Object classIdObj : classIdsArray)
          {
            int classId=((Integer)classIdObj).intValue();
            CharacterClass characterClass=DatEnumsUtils.getCharacterClassFromId(classId);
            requirements.addAllowedClass(characterClass);
          }
        }
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
    TrophyList ret=_lootsMgr.getTrophyLists().getItem(id);
    if (ret==null)
    {
      PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
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
        _lootsMgr.getTrophyLists().add(ret);
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
    TreasureGroupProfile ret=_lootsMgr.getTreasureGroupProfiles().getItem(id);
    if (ret==null)
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
      ret=new TreasureGroupProfile(id,itemsTable,treasureList);
      _lootsMgr.getTreasureGroupProfiles().add(ret);
    }
    return ret;
  }

  private ItemsTable handleItemsTable(int id, PropertiesSet properties)
  {
    ItemsTable ret=_lootsMgr.getItemsTables().getItem(id);
    if (ret==null)
    {
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
        _lootsMgr.getItemsTables().add(ret);
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
    TreasureList ret=_lootsMgr.getTreasureLists().getItem(id);
    if (ret==null)
    {
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
        _lootsMgr.getTreasureLists().add(ret);
      }
    }
    return ret;
  }

  private float getProbability(int frequencyCode)
  {
    Float probability=_probabilities.get(Integer.valueOf(frequencyCode));
    return probability.floatValue();
  }
}
