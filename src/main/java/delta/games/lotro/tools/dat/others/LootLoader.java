package delta.games.lotro.tools.dat.others;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;

/**
 * Loader for loot data.
 * @author DAM
 */
public class LootLoader
{
  private DataFacade _facade;
  private ItemsManager _itemsManager;
  private EnumMapper _dropFrequency;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public LootLoader(DataFacade facade)
  {
    _facade=facade;
    _itemsManager=ItemsManager.getInstance();
    _dropFrequency=facade.getEnumsManager().getEnumMapper(587202656);
  }
 

  /**
   * Handle a weighted treasure table.
   * @param id Table identifier.
   */
  public void handleWeightedTreasureTable(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+0x09000000);
    Object[] treasureTable=(Object[])properties.getProperty("LootGen_WeightedTreasureTable");
    for(Object treasureTableItem : treasureTable)
    {
      PropertiesSet treasureTableItemProps=(PropertiesSet)treasureTableItem;
      int weight=((Integer)treasureTableItemProps.getProperty("LootGen_WeightedTreasureTable_TrophyList_Weight")).intValue();
      int trophyListId=((Integer)treasureTableItemProps.getProperty("LootGen_WeightedTreasureTable_TrophyList")).intValue();
      System.out.println("Weight: "+weight);
      handleTrophyList(trophyListId);
    }
  }

  /**
   * Handle a trophy list.
   * @param id List identifier.
   */
  public void handleTrophyList(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+0x09000000);
    //System.out.println(properties.dump());
    Object[] trophyList=(Object[])properties.getProperty("LootGen_TrophyList");
    for(Object trophyObj : trophyList)
    {
      PropertiesSet trophyProps=(PropertiesSet)trophyObj;
      int frequency=((Integer)trophyProps.getProperty("LootGen_TrophyList_DropFrequency")).intValue();
      int itemOrProfile=((Integer)trophyProps.getProperty("LootGen_TrophyList_ItemOrProfile")).intValue();
      Integer quantityInt=(Integer)trophyProps.getProperty("LootGen_TrophyList_ItemQuantity");
      int quantity=(quantityInt!=null)?quantityInt.intValue():1;
      Item item=_itemsManager.getItem(itemOrProfile);
      String frequencyStr=_dropFrequency.getString(frequency);
      if (item!=null)
      {
        System.out.println("\tFreq:"+frequencyStr+", item="+item+", quantity="+quantity);
      }
      else
      {
        System.out.println("\tFreq:"+frequencyStr+", quantity="+quantity);
        handleTreasureGroupProfile(itemOrProfile);
      }
    }
  }

  private void handleTreasureGroupProfile(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+0x09000000);
    Object[] trophyList=(Object[])properties.getProperty("TreasureGroupProfile_ItemTable");
    if (trophyList!=null)
    {
      for(Object trophyObj : trophyList)
      {
        PropertiesSet trophyProps=(PropertiesSet)trophyObj;
        int weight=((Integer)trophyProps.getProperty("TreasureGroupProfile_ItemTable_Weight")).intValue();
        int itemOrProfile=((Integer)trophyProps.getProperty("TreasureGroupProfile_ItemTable_Item")).intValue();
        Integer quantityInt=(Integer)trophyProps.getProperty("TreasureGroupProfile_ItemTable_Quantity");
        int quantity=(quantityInt!=null)?quantityInt.intValue():1;
        Item item=_itemsManager.getItem(itemOrProfile);
        System.out.println("\t\tWeight:"+weight+", item="+item+", quantity="+quantity);
      }
    }
    boolean hasTreasureList=handleTreasureList(properties);
    if ((trophyList==null) && (hasTreasureList))
    {
      System.out.println("**********************");
      System.out.println(properties.dump());
      return;
    }
  }

  /**
   * Handle a treasure list.
   * @param properties Input data.
   * @return <code>true</code> if something was found, <code>false</code> otherwise.
   */
  public boolean handleTreasureList(PropertiesSet properties)
  {
    Object[] treasureList=(Object[])properties.getProperty("LootGen_TreasureList");
    if (treasureList!=null)
    {
      for(Object treasureObj : treasureList)
      {
        PropertiesSet treasureProps=(PropertiesSet)treasureObj;
        Integer weight=(Integer)treasureProps.getProperty("TreasureGroupProfile_ItemTable_Weight");
        int profile=((Integer)treasureProps.getProperty("LootGen_TreasureList_GroupProfile")).intValue();
        System.out.println("\t\tWeight:"+weight);
        handleTreasureGroupProfile(profile);
      }
    }
    return (treasureList!=null);
  }
}
