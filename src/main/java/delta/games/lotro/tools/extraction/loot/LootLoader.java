package delta.games.lotro.tools.extraction.loot;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.common.treasure.FilteredTrophyTable;
import delta.games.lotro.common.treasure.FilteredTrophyTableEntry;
import delta.games.lotro.common.treasure.ItemsTable;
import delta.games.lotro.common.treasure.ItemsTableEntry;
import delta.games.lotro.common.treasure.LootTable;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.RelicsList;
import delta.games.lotro.common.treasure.RelicsListEntry;
import delta.games.lotro.common.treasure.RelicsTreasureGroup;
import delta.games.lotro.common.treasure.RelicsTreasureGroupEntry;
import delta.games.lotro.common.treasure.TreasureGroupProfile;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TreasureListEntry;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.TrophyListEntry;
import delta.games.lotro.common.treasure.WeightedTreasureTable;
import delta.games.lotro.common.treasure.WeightedTreasureTableEntry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.ArrayPropertyValue;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.PropertyValue;
import delta.games.lotro.dat.utils.DatIconsUtils;
import delta.games.lotro.dat.utils.DatStringUtils;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.utils.StringUtils;

/**
 * Loader for loot data.
 * @author DAM
 */
public class LootLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(LootLoader.class);

  private DataFacade _facade;
  private LootProbabilities _probabilities;
  private LootsManager _lootsMgr;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootsMgr Loots manager.
   */
  public LootLoader(DataFacade facade, LootsManager lootsMgr)
  {
    _facade=facade;
    _probabilities=new LootProbabilities(facade);
    _lootsMgr=lootsMgr;
  }

  /**
   * Get a weighted treasure table.
   * @param id Table identifier.
   * @return a weighted treasure table or <code>null</code>.
   */
  public WeightedTreasureTable getWeightedTreasureTable(int id)
  {
    WeightedTreasureTable ret=get(id,WeightedTreasureTable.class);
    if (ret==null)
    {
      ret=loadWeightedTreasureTable(id);
      if (ret!=null)
      {
        _lootsMgr.getTables().add(ret);
      }
    }
    return ret;
  }

  private WeightedTreasureTable loadWeightedTreasureTable(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    Object[] treasureTable=(Object[])properties.getProperty("LootGen_WeightedTreasureTable");
    if (treasureTable==null)
    {
      return null;
    }
    WeightedTreasureTable ret=new WeightedTreasureTable(id);
    for(Object treasureTableItem : treasureTable)
    {
      PropertiesSet treasureTableItemProps=(PropertiesSet)treasureTableItem;
      int weight=((Integer)treasureTableItemProps.getProperty("LootGen_WeightedTreasureTable_TrophyList_Weight")).intValue();
      int trophyListId=((Integer)treasureTableItemProps.getProperty("LootGen_WeightedTreasureTable_TrophyList")).intValue();
      TrophyList trophyList=getTrophyList(trophyListId);
      WeightedTreasureTableEntry entry=new WeightedTreasureTableEntry(weight,trophyList);
      ret.addEntry(entry);
    }
    return ret;
  }

  /**
   * Get a filtered trophy list.
   * @param id Identifier.
   * @return a table.
   */
  public FilteredTrophyTable getFilteredTrophyTable(int id)
  {
    FilteredTrophyTable ret=get(id,FilteredTrophyTable.class);
    if (ret==null)
    {
      ret=loadFilteredTrophyTable(id);
      if (ret!=null)
      {
        _lootsMgr.getTables().add(ret);
      }
    }
    return ret;
  }

  private FilteredTrophyTable loadFilteredTrophyTable(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    FilteredTrophyTable ret=new FilteredTrophyTable(id);
    Object[] list=(Object[])properties.getProperty("LootGen_FilteredTreasureListArray");
    for(Object listItemObj : list)
    {
      PropertiesSet itemProps=(PropertiesSet)listItemObj;
      int lootTableId=((Integer)itemProps.getProperty("LootGen_FilteredTrophyTable_EntryDID")).intValue();
      LootTable lootTable=getTrophyList(lootTableId);
      if (lootTable==null)
      {
        lootTable=getWeightedTreasureTable(lootTableId);
      }
      FilteredTrophyTableEntry entry=new FilteredTrophyTableEntry(lootTable);
      ret.addEntry(entry);
      // Filter
      ArrayPropertyValue filter=(ArrayPropertyValue)itemProps.getPropertyValueByName("EntityFilter_Array");
      if (filter!=null)
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Table ID="+id+" => "+itemProps.dump());
        }
        loadFilterData(id,filter,entry.getUsageRequirement());
      }
    }
    return ret;
  }

  private void loadFilterData(int tableID, ArrayPropertyValue filterArray, UsageRequirement requirements)
  {
    for(PropertyValue filterEntry : filterArray.getValues())
    {
      String propertyName=filterEntry.getDefinition().getName();
      if ("EntityFilter_PropertyRange".equals(propertyName))
      {
        // Level filter
        PropertiesSet levelProps=(PropertiesSet)filterEntry.getValue();
        loadLevelFilter(levelProps,requirements);
      }
      else if ("EntityFilter_PropertySet".equals(propertyName))
      {
        ArrayPropertyValue propertyArray=(ArrayPropertyValue)filterEntry;
        for(PropertyValue propertySet : propertyArray.getValues())
        {
          handlePropertySet(propertySet,requirements);
        }
      }
      else
      {
        LOGGER.warn("Unmanaged property: "+propertyName);
      }
    }
  }

  private void handlePropertySet(PropertyValue propertySet,UsageRequirement requirements)
  {
    String propertyName=propertySet.getDefinition().getName();
    if ("Agent_Class".equals(propertyName))
    {
      // Class filter
      int classCode=((Integer)propertySet.getValue()).intValue();
      AbstractClassDescription abstractClass=ClassesManager.getInstance().getClassByCode(classCode);
      if (abstractClass!=null)
      {
        requirements.addAllowedClass(abstractClass);
      }
    }
    else if ("Agent_Species".equals(propertyName))
    {
      // Race filter
      int raceCode=((Integer)propertySet.getValue()).intValue();
      RaceDescription race=RacesManager.getInstance().getByCode(raceCode);
      if (race!=null)
      {
        requirements.addAllowedRace(race);
      }
    }
    else
    {
      LOGGER.warn("Unmanaged property name: "+propertyName);
      // TODO: ze_skirmish_difficulty
    }
  }

  private void loadLevelFilter(PropertiesSet levelProps, UsageRequirement requirements)
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
   * Get a trophy list.
   * @param id List identifier.
   * @return a trophy list or <code>null</code>.
   */
  public TrophyList getTrophyList(int id)
  {
    TrophyList ret=get(id,TrophyList.class);
    if (ret==null)
    {
      ret=loadTrophyList(id);
      if (ret!=null)
      {
        _lootsMgr.getTables().add(ret);
      }
    }
    return ret;
  }

  private TrophyList loadTrophyList(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    Object[] trophyList=(Object[])properties.getProperty("LootGen_TrophyList");
    if (trophyList==null)
    {
      return null;
    }
    TrophyList ret=new TrophyList(id);
    String description=DatStringUtils.getStringProperty(properties,"Description");
    if (description!=null)
    {
      description=StringUtils.fixName(description);
      ret.setDescription(description);
    }
    Integer imageDid=(Integer)properties.getProperty("Icon_Layer_ImageOverrideDID");
    if (imageDid!=null)
    {
      ret.setImageId(imageDid);
      File toDir=new File(GeneratedFiles.MISC_ICONS,"trophyLists");
      File to=new File(toDir,imageDid.toString()+".png");
      if (!to.exists())
      {
        DatIconsUtils.buildImageFile(_facade,imageDid.intValue(),to);
      }
    }
    for(Object trophyObj : trophyList)
    {
      TrophyListEntry entry=null;
      PropertiesSet trophyProps=(PropertiesSet)trophyObj;
      // Probability
      int frequencyCode=((Integer)trophyProps.getProperty("LootGen_TrophyList_DropFrequency")).intValue();
      float probability=_probabilities.getProbability(frequencyCode);
      // Quantity
      int itemOrProfile=((Integer)trophyProps.getProperty("LootGen_TrophyList_ItemOrProfile")).intValue();
      Item item=null;
      TreasureGroupProfile treasureGroup=null;
      String itemName=getItemName(itemOrProfile);
      if (itemName!=null)
      {
        // Item
        item=ItemsManager.getInstance().getItem(itemOrProfile);
        if (item!=null)
        {
          Integer quantityInt=(Integer)trophyProps.getProperty("LootGen_TrophyList_ItemQuantity");
          int quantity=(quantityInt!=null)?quantityInt.intValue():1;
          entry=new TrophyListEntry(probability,item,quantity);
        }
      }
      else
      {
        // or treasure group
        treasureGroup=getTreasureGroupProfile(itemOrProfile);
        if (treasureGroup!=null)
        {
          entry=new TrophyListEntry(probability,treasureGroup);
        }
      }
      if (entry!=null)
      {
        // Group drop
        Integer groupDropInt=(Integer)trophyProps.getProperty("LootGen_TrophyList_GroupDrop");
        boolean groupDrop=((groupDropInt!=null) && (groupDropInt.intValue()==1));
        entry.setGroupDrop(groupDrop);
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
      ret=DatStringUtils.getStringProperty(properties,"Name");
    }
    return ret;
  }

  private TreasureGroupProfile getTreasureGroupProfile(int id)
  {
    TreasureGroupProfile ret=get(id,TreasureGroupProfile.class);
    if (ret==null)
    {
      ret=loadTreasureGroupProfile(id);
      if (ret!=null)
      {
        _lootsMgr.getTables().add(ret);
      }
    }
    return ret;
  }

  private TreasureGroupProfile loadTreasureGroupProfile(int id)
  {
    ItemsTable itemsTable=loadItemsTable(id);
    if (itemsTable!=null)
    {
      return itemsTable;
    }
    TreasureList treasureList=loadTreasureList(id);
    if (treasureList!=null)
    {
      return treasureList;
    }
    // Sometimes we get here with an ID of an unknown item (ex: 1879347509 TBD eq_u21_rar_guardian_tank_T3_set_a_shield)
    if (LOGGER.isWarnEnabled())
    {
      PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
      if (properties!=null)
      {
        LOGGER.warn(properties.dump());
      }
      else
      {
        LOGGER.warn("No properties for ID: "+id);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T extends LootTable> T get(int id, Class<T> clazz)
  {
    LootTable ret=_lootsMgr.getTables().getItem(id);
    if (ret!=null)
    {
      if (clazz.isAssignableFrom(ret.getClass()))
      {
        return (T)ret;
      }
    }
    return null;
  }

  private ItemsTable loadItemsTable(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    Object[] itemTableArray=(Object[])properties.getProperty("TreasureGroupProfile_ItemTable");
    if (itemTableArray==null)
    {
      return null;
    }
    ItemsTable ret=new ItemsTable(id);
    for(Object itemTableEntryObj : itemTableArray)
    {
      PropertiesSet entryProps=(PropertiesSet)itemTableEntryObj;
      int weight=((Integer)entryProps.getProperty("TreasureGroupProfile_ItemTable_Weight")).intValue();
      int itemId=((Integer)entryProps.getProperty("TreasureGroupProfile_ItemTable_Item")).intValue();
      Integer quantityInt=(Integer)entryProps.getProperty("TreasureGroupProfile_ItemTable_Quantity");
      int quantity=(quantityInt!=null)?quantityInt.intValue():1;
      Item item=ItemsManager.getInstance().getItem(itemId);
      if (item!=null)
      {
        ItemsTableEntry entry=new ItemsTableEntry(weight,item,quantity);
        ret.addEntry(entry);
      }
    }
    return ret;
  }

  /**
   * Get a treasure list.
   * @param id Identifier.
   * @return A treasure list or <code>null</code>.
   */
  public TreasureList getTreasureList(int id)
  {
    TreasureList ret=get(id,TreasureList.class);
    if (ret==null)
    {
      ret=loadTreasureList(id);
      if (ret!=null)
      {
        _lootsMgr.getTables().add(ret);
      }
    }
    return ret;
  }

  private TreasureList loadTreasureList(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    Object[] treasureList=(Object[])properties.getProperty("LootGen_TreasureList");
    if (treasureList==null)
    {
      return null;
    }
    TreasureList ret=new TreasureList(id);
    for(Object treasureObj : treasureList)
    {
      PropertiesSet treasureProps=(PropertiesSet)treasureObj;
      int weight=((Integer)treasureProps.getProperty("LootGen_TreasureList_Weight")).intValue();
      int profile=((Integer)treasureProps.getProperty("LootGen_TreasureList_GroupProfile")).intValue();
      TreasureGroupProfile treasureGroupProfile=getTreasureGroupProfile(profile);
      TreasureListEntry entry=new TreasureListEntry(weight,treasureGroupProfile);
      ret.addEntry(entry);
    }
    return ret;
  }

  /**
   * Get a relics list.
   * @param propertyId Identifier.
   * @return A relics list or <code>null</code>.
   */
  public RelicsList getRelicsList(int propertyId)
  {
    RelicsList ret=_lootsMgr.getRelicsLists().getItem(propertyId);
    if (ret==null)
    {
      PropertiesSet properties=_facade.loadProperties(propertyId+DATConstants.DBPROPERTIES_OFFSET);
      if (properties==null)
      {
        return null;
      }
      ret=new RelicsList(propertyId);
      Object[] entriesArray=(Object[])properties.getProperty("RunicList_Array");
      for(Object entryObj : entriesArray)
      {
        PropertiesSet entryProps=(PropertiesSet)entryObj;
        int frequencyCode=((Integer)entryProps.getProperty("RunicList_DropFrequency")).intValue();
        float probability=_probabilities.getProbability(frequencyCode);
        // id may be a Relic, or a runic loot table:
        int id=((Integer)entryProps.getProperty("RunicList_RunicOrTreasureGroup")).intValue();
        Relic relic=getRelic(id);
        RelicsTreasureGroup treasureGroup=null;
        if (relic==null)
        {
          treasureGroup=getRelicsTreasureGroup(id);
        }
        if ((relic!=null) || (treasureGroup!=null))
        {
          RelicsListEntry entry=new RelicsListEntry(probability,relic,treasureGroup);
          ret.addEntry(entry);
        }
      }
      _lootsMgr.getRelicsLists().add(ret);
    }
    return ret;
  }

  private RelicsTreasureGroup getRelicsTreasureGroup(int id)
  {
    RelicsTreasureGroup ret=_lootsMgr.getRelicsTreasureGroups().getItem(id);
    if (ret==null)
    {
      PropertiesSet properties=_facade.loadProperties(id+DATConstants.DBPROPERTIES_OFFSET);
      if (properties==null)
      {
        return null;
      }
      Object[] entriesArray=(Object[])properties.getProperty("RunicTreasureGroup_Array");
      if (entriesArray==null)
      {
        return null;
      }
      ret=new RelicsTreasureGroup(id);
      for(Object entryObj : entriesArray)
      {
        PropertiesSet entryProps=(PropertiesSet)entryObj;
        int relicId=((Integer)entryProps.getProperty("RunicTreasureGroup_Runic")).intValue();
        int weight=((Integer)entryProps.getProperty("RunicTreasureGroup_Weight")).intValue();
        Relic relic=getRelic(relicId);
        if (relic!=null)
        {
          RelicsTreasureGroupEntry entry=new RelicsTreasureGroupEntry(weight,relic);
          ret.addEntry(entry);
        }
        else
        {
          LOGGER.warn("Relic not found: "+relicId);
        }
      }
      Object[] countsArray=(Object[])properties.getProperty("RunicTreasureGroup_PullCount_Array");
      if (countsArray!=null)
      {
        for(Object entryObj : countsArray)
        {
          PropertiesSet entryProps=(PropertiesSet)entryObj;
          int count=((Integer)entryProps.getProperty("RunicTreasureGroup_PullCount")).intValue();
          int weight=((Integer)entryProps.getProperty("RunicTreasureGroup_Weight")).intValue();
          if (weight>0)
          {
            ret.addCount(count,weight);
          }
        }
      }
      _lootsMgr.getRelicsTreasureGroups().add(ret);
    }
    return ret;
  }

  private Relic getRelic(int relicId)
  {
    RelicsManager relicsMgr=RelicsManager.getInstance();
    Relic relic=relicsMgr.getById(relicId);
    return relic;
  }
}
