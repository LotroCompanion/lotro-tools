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
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsContainer;
import delta.games.lotro.lore.items.ItemsManager;
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
  public Container load(int indexDataId)
  {
    Container ret=null;
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
      if (count>=1)
      {
        ItemsContainer itemsContainer=new ItemsContainer(indexDataId);
        ret=itemsContainer;
        if (filteredTable!=null)
        {
          itemsContainer.setFilteredTable(filteredTable);
        }
        if (weightedTable!=null)
        {
          itemsContainer.setWeightedTable(weightedTable);
        }
        if (trophyList!=null)
        {
          itemsContainer.setTrophyList(trophyList);
        }
        if (treasureList!=null)
        {
          itemsContainer.setTreasureList(treasureList);
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
    }
    else
    {
      LOGGER.warn("Could not handle container item ID="+indexDataId);
    }
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
