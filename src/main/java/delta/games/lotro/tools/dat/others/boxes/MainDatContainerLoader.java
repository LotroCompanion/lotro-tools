package delta.games.lotro.tools.dat.others.boxes;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.FilteredTrophyTable;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.WeightedTreasureTable;
import delta.games.lotro.common.treasure.io.xml.TreasureXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.others.LootLoader;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get the contents of a container (box,chest,scrolls...) from DAT files.
 * @author DAM
 */
public class MainDatContainerLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatContainerLoader.class);

  private DataFacade _facade;
  private LootLoader _lootLoader;
  private LootsManager _loots;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatContainerLoader(DataFacade facade)
  {
    _facade=facade;
    _loots=new LootsManager();
    _lootLoader=new LootLoader(facade,_loots);
  }

  /**
   * Load a container.
   * @param indexDataId Container item identifier.
   * @return the loaded container.
   */
  public Object load(int indexDataId)
  {
    FilteredTrophyTable filteredTable=null;
    WeightedTreasureTable weightedTable=null;
    TrophyList trophyList=null;
    TreasureList treasureList=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // Filtered trophy table
      Integer filteredLootTableId=(Integer)properties.getProperty("PackageItem_FilteredTrophyTableTemplate");
      if (filteredLootTableId!=null)
      {
        filteredTable=_lootLoader.handleFilteredTrophyTable(filteredLootTableId.intValue());
      }
      // Free-people weighted treasure table ID
      Integer freepWeightedTreasureTableId=(Integer)properties.getProperty("PackageItem_Freep_WeightedTreasureTableID");
      if ((freepWeightedTreasureTableId!=null) && (freepWeightedTreasureTableId.intValue()!=0))
      {
        weightedTable=_lootLoader.handleWeightedTreasureTable(freepWeightedTreasureTableId.intValue());
      }
      // Trophy template
      Integer trophyTemplateId=(Integer)properties.getProperty("PackageItem_TrophyListTemplate");
      if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
      {
        trophyList=_lootLoader.handleTrophyList(trophyTemplateId.intValue());
      }
      // Preview
      /*
      Integer preview=(Integer)properties.getProperty("PackageItem_IsPreviewable");
      if ((preview!=null) && (preview.intValue()!=0))
      {
        System.out.println("Preview:");
        Object[] previewList=(Object[])properties.getProperty("PackageItem_PreviewList");
        for(Object previewIdObj : previewList)
        {
          Integer previewId=(Integer)previewIdObj;
          Item item=ItemsManager.getInstance().getItem(previewId.intValue());
          System.out.println("\t"+item);
        }
      }
      */
      // Effects (for scrolls)
      treasureList=handleEffects(properties);

      int count=((filteredTable!=null)?1:0)+((weightedTable!=null)?1:0)+((trophyList!=null)?1:0)+((treasureList!=null)?1:0);
      if (count>1)
      //if ((filteredTable!=null) || (weightedTable!=null) || (trophyList!=null) || (treasureList!=null))
      {
        // Name
        String name=DatUtils.getStringProperty(properties,"Name");
        System.out.println("Container: "+name);
        if (filteredTable!=null)
        {
          System.out.println(filteredTable);
          System.out.println("\tFound a filtered trophy table!");
        }
        if (weightedTable!=null)
        {
          System.out.println(weightedTable);
          System.out.println("\tFound a weighted treasure table!");
        }
        if (trophyList!=null)
        {
          System.out.println(trophyList);
          System.out.println("\tFound a trophy list!");
        }
        if (treasureList!=null)
        {
          System.out.println(treasureList);
          System.out.println("\tFound a treasure list!");
        }
        //System.out.println(properties.dump());
      }
     /*
PackageItem_IsPreviewable: 0
PackageItem_BindAllItemsToAccount: 1
PackageItem_UsePlayerAsContainerForMunging: 1

If PackageItem_IsPreviewable: 1
=> PackageItem_PreviewList: 
  #1: 1879259202
  #2: 1879259205
  #3: 1879259200
  #4: 1879188748
     */
    }
    else
    {
      LOGGER.warn("Could not handle container item ID="+indexDataId);
    }
    return null;
  }

  private TreasureList handleEffects(PropertiesSet properties)
  {
    TreasureList ret=null;
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effects!=null)
    {
      for(Object effectObj : effects)
      {
        PropertiesSet effectItemProps=(PropertiesSet)effectObj;
        Integer effectId=(Integer)effectItemProps.getProperty("EffectGenerator_EffectID");
        if (effectId!=null)
        {
          ret=handleEffect(effectId.intValue());
        }
      }
    }
    return ret;
  }

  private TreasureList handleEffect(int effectId)
  {
    TreasureList ret=null;
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    Integer treasureListTemplateId=(Integer)effectProps.getProperty("Effect_Lootgen_DiscoveryTreasureListTemplate");
    if (treasureListTemplateId!=null)
    {
      PropertiesSet treasureListProps=_facade.loadProperties(treasureListTemplateId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      ret=_lootLoader.handleTreasureList(treasureListTemplateId.intValue(),treasureListProps);
    }
    return ret;
  }

  private void doIt()
  {
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      load(item.getIdentifier());
    }
    _loots.dump();
    TreasureXMLWriter.writeLootsFile(GeneratedFiles.LOOTS,_loots);
    // Test samples:
    /*
    // Battle Gift Box
    load(1879303552);
    // Cosmetic Gift Box
    load(1879303553);
    // Ancient Riddermark Scroll Case III
    load(1879265139);
    // Coffer of Adventurer's Armour - Heavy (Incomparable: 1879378494)
    load(1879378494);
    // Coffer of Adventurer's Jewellery - Might (Incomparable: 1879378473)
    load(1879378473);
    */
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatContainerLoader(facade).doIt();
    facade.dispose();
  }
}
