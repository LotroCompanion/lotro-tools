package delta.games.lotro.tools.dat.others.boxes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.FilteredTrophyTable;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.RelicsList;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.WeightedTreasureTable;
import delta.games.lotro.common.treasure.io.xml.TreasureXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.instances.loot.InstanceLootsTable;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsContainer;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.containers.LootType;
import delta.games.lotro.lore.items.io.xml.ContainerXMLWriter;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsContainer;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.others.LootLoader;

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
  private CustomInstancesLootLoader _instancesLootLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatContainerLoader(DataFacade facade)
  {
    _facade=facade;
    _loots=new LootsManager();
    _lootLoader=new LootLoader(facade,_loots);
    _instancesLootLoader=new CustomInstancesLootLoader(_facade,_lootLoader);
  }

  /**
   * Load a container.
   * @param indexDataId Container item identifier.
   * @return the loaded container.
   */
  public Container load(int indexDataId)
  {
    Item item=ItemsManager.getInstance().getItem(indexDataId);
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not handle container item ID="+indexDataId);
      return null;
    }
    // Filtered trophy table
    FilteredTrophyTable filteredTable=null;
    Integer filteredLootTableId=(Integer)properties.getProperty("PackageItem_FilteredTrophyTableTemplate");
    if (filteredLootTableId!=null)
    {
      filteredTable=_lootLoader.handleFilteredTrophyTable(filteredLootTableId.intValue());
    }
    // Free-people weighted treasure table ID
    WeightedTreasureTable weightedTable=null;
    Integer freepWeightedTreasureTableId=(Integer)properties.getProperty("PackageItem_Freep_WeightedTreasureTableID");
    if ((freepWeightedTreasureTableId!=null) && (freepWeightedTreasureTableId.intValue()!=0))
    {
      weightedTable=_lootLoader.handleWeightedTreasureTable(freepWeightedTreasureTableId.intValue());
    }
    // Trophy template
    TrophyList trophyList=null;
    Integer trophyTemplateId=(Integer)properties.getProperty("PackageItem_TrophyListTemplate");
    if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
    {
      trophyList=_lootLoader.handleTrophyList(trophyTemplateId.intValue());
    }
    // Other filtered trophy table
    Integer filteredTrophyTableId=(Integer)properties.getProperty("LootGen_FilteredTrophyTable");
    if ((filteredTrophyTableId!=null) && (filteredTrophyTableId.intValue()!=0))
    {
      if (filteredTable!=null)
      {
        LOGGER.warn("Filtered table override: "+item);
      }
      filteredTable=_lootLoader.handleFilteredTrophyTable(filteredTrophyTableId.intValue());
    }
    // Other filtered trophy table
    /*
    Integer filteredTrophyTable2Id=(Integer)properties.getProperty("LootGen_FilteredTrophyTable2");
    if ((filteredTrophyTable2Id!=null) && (filteredTrophyTable2Id.intValue()!=0))
    {
      if (filteredTable!=null)
      {
        LOGGER.warn("Filtered table override (2): "+item);
      }
      filteredTable=_lootLoader.handleFilteredTrophyTable(filteredTrophyTable2Id.intValue());
    }
    // Other filtered trophy table
    Integer filteredTrophyTable3Id=(Integer)properties.getProperty("LootGen_FilteredTrophyTable3");
    if ((filteredTrophyTable3Id!=null) && (filteredTrophyTable3Id.intValue()!=0))
    {
      if (filteredTable!=null)
      {
        LOGGER.warn("Filtered table override (3): "+item);
      }
      filteredTable=_lootLoader.handleFilteredTrophyTable(filteredTrophyTable3Id.intValue());
    }
    */
    // Trophy list override
    Integer trophyListOverrideId=(Integer)properties.getProperty("LootGen_TrophyList_Override");
    if ((trophyListOverrideId!=null) && (trophyListOverrideId.intValue()!=0))
    {
      if (trophyList!=null)
      {
        LOGGER.warn("Trophy list override");
      }
      trophyList=_lootLoader.handleTrophyList(trophyListOverrideId.intValue());
    }
    // Barter trophy list
    TrophyList barterTrophyList=null;
    Integer barterTrophyListId=(Integer)properties.getProperty("LootGen_BarterTrophyList");
    if ((barterTrophyListId!=null) && (barterTrophyListId.intValue()!=0))
    {
      barterTrophyList=_lootLoader.handleTrophyList(barterTrophyListId.intValue());
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
    TreasureList treasureList=null;
    treasureList=handleEffects(properties);
    // Treasure list override
    Integer treasureListOverrideId=(Integer)properties.getProperty("LootGen_TreasureList_Override");
    if ((treasureListOverrideId!=null) && (treasureListOverrideId.intValue()!=0))
    {
      if (treasureList!=null)
      {
        LOGGER.warn("Treasure list override");
      }
      PropertiesSet treasureListProps=_facade.loadProperties(treasureListOverrideId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      treasureList=_lootLoader.handleTreasureList(treasureListOverrideId.intValue(),treasureListProps);
    }
    // Reputation trophy
    Integer reputationTrophyList=(Integer)properties.getProperty("LootGen_ReputationTrophyList");
    if ((reputationTrophyList!=null) && (reputationTrophyList.intValue()!=0))
    {
      // Never happens
      LOGGER.warn("Reputation trophy - should never happen");
    }
    // Custom skirmish loot lookup table
    InstanceLootsTable table=null;
    Integer customSkirmishLootLookupTableId=(Integer)properties.getProperty("LootGen_CustomSkirmishLootLookupTable");
    if ((customSkirmishLootLookupTableId!=null) && (customSkirmishLootLookupTableId.intValue()!=0))
    {
      table=_instancesLootLoader.handleCustomSkirmishLootLookupTable(customSkirmishLootLookupTableId.intValue());
    }

    Container ret=null;
    int count=((filteredTable!=null)?1:0)+((weightedTable!=null)?1:0)+((trophyList!=null)?1:0)+((barterTrophyList!=null)?1:0)+((treasureList!=null)?1:0)+((table!=null)?1:0);
    if (count>=1)
    {
      ItemsContainer itemsContainer=new ItemsContainer(indexDataId);
      ret=itemsContainer;
      if (filteredTable!=null)
      {
        itemsContainer.set(LootType.FILTERED_TROPHY_TABLE,filteredTable);
      }
      if (weightedTable!=null)
      {
        itemsContainer.set(LootType.WEIGHTED_TREASURE_TABLE,weightedTable);
      }
      if (trophyList!=null)
      {
        itemsContainer.set(LootType.TROPHY_LIST,trophyList);
      }
      if (barterTrophyList!=null)
      {
        itemsContainer.set(LootType.BARTER_TROPHY_LIST,barterTrophyList);
      }
      if (treasureList!=null)
      {
        itemsContainer.set(LootType.TREASURE_LIST,treasureList);
      }
      if ((customSkirmishLootLookupTableId!=null) && (customSkirmishLootLookupTableId.intValue()!=0))
      {
        itemsContainer.setCustomSkirmishLootTableId(customSkirmishLootLookupTableId);
      }
      //System.out.println(properties.dump());
    }

    // Relics?
    RelicsContainer relicsContainer=handleRelics(indexDataId,properties);
    if (relicsContainer!=null)
    {
      if (ret!=null)
      {
        LOGGER.warn("Both containers (items+relics) for: "+indexDataId);
      }
      ret=relicsContainer;
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
    return ret;
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

  private RelicsContainer handleRelics(int containerId, PropertiesSet properties)
  {
    RelicsContainer ret=null;
    Integer relicId=(Integer)properties.getProperty("ItemAdvancement_RunicToCreate");
    if (relicId!=null)
    {
      RelicsManager relicsMgr=RelicsManager.getInstance();
      Relic relic=relicsMgr.getById(relicId.intValue());
      if (relic!=null)
      {
        ret=new RelicsContainer(containerId);
        ret.setRelic(relic);
      }
      else
      {
        LOGGER.warn("Relic not found: "+relicId);
      }
    }

    Integer runicLootListId=(Integer)properties.getProperty("ItemAdvancement_RunicLootListOverride");
    if (runicLootListId!=null)
    {
      RelicsList list=_lootLoader.handleRelicsList(runicLootListId.intValue());
      //System.out.println(list);
      ret=new RelicsContainer(containerId);
      ret.setRelicsList(list);
    }
    return ret;
  }

  /**
   * Load containers.
   */
  public void doIt()
  {
    List<Container> containers=new ArrayList<Container>();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      Container container=load(item.getIdentifier());
      if (container!=null)
      {
        containers.add(container);
      }
    }
    // Dump some stats
    _loots.dump();
    // Write loot data
    TreasureXMLWriter.writeLootsFile(GeneratedFiles.LOOTS,_loots);
    // Write container data
    ContainerXMLWriter.writeContainersFile(GeneratedFiles.CONTAINERS,containers);
    // Write custom instance loots
    _instancesLootLoader.writeData();
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
