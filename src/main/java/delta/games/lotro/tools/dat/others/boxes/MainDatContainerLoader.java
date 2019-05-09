package delta.games.lotro.tools.dat.others.boxes;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.others.LootLoader;
import delta.games.lotro.tools.dat.utils.DatEnumsUtils;
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

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatContainerLoader(DataFacade facade)
  {
    _facade=facade;
    _lootLoader=new LootLoader(facade);
  }

  private Object load(int indexDataId)
  {
    Object ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+0x09000000);
    if (properties!=null)
    {
      //System.out.println(properties.dump());
      // Name
      String name=DatUtils.getStringProperty(properties,"Name");
      System.out.println("Container: "+name);

      // Filtered trophy table
      Integer filteredLootTableId=(Integer)properties.getProperty("PackageItem_FilteredTrophyTableTemplate");
      if (filteredLootTableId!=null)
      {
        handleFilteredTrophyTable(filteredLootTableId.intValue());
      }
      // Free-people weighted treasure table ID
      Integer freepWeightedTreasureTableId=(Integer)properties.getProperty("PackageItem_Freep_WeightedTreasureTableID");
      if ((freepWeightedTreasureTableId!=null) && (freepWeightedTreasureTableId.intValue()!=0))
      {
        _lootLoader.handleWeightedTreasureTable(freepWeightedTreasureTableId.intValue());
      }
      // Trophy template
      Integer trophyTemplateId=(Integer)properties.getProperty("PackageItem_TrophyListTemplate");
      if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
      {
        _lootLoader.handleTrophyList(trophyTemplateId.intValue());
      }
      // Effects (for scrolls)
      handleEffects(properties);

      /*
PackageItem_FilteredTrophyTableTemplate: 1879303571
PackageItem_Freep_WeightedTreasureTableID: 0
PackageItem_IsPreviewable: 0
PackageItem_TrophyListTemplate: 0
     */
    }
    else
    {
      LOGGER.warn("Could not handle container item ID="+indexDataId);
    }
    return ret;
  }

  private void handleFilteredTrophyTable(int id)
  {
    PropertiesSet properties=_facade.loadProperties(id+0x09000000);
    if (properties!=null)
    {
      Object[] list=(Object[])properties.getProperty("LootGen_FilteredTreasureListArray");
      if (list!=null)
      {
        for(Object listItemObj : list)
        {
          PropertiesSet itemProps=(PropertiesSet)listItemObj;
          //System.out.println(itemProps.dump());
          Object[] filter=(Object[])itemProps.getProperty("EntityFilter_Array");
          loadFilterData(filter);
          Integer lootTableId=(Integer)itemProps.getProperty("LootGen_FilteredTrophyTable_EntryDID");
          if (lootTableId!=null)
          {
            handleLootTable(lootTableId.intValue());
          }
        }
      }
    }
  }

  private void loadFilterData(Object[] filterArray)
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
          System.out.println("\tCharacter clas: "+characterClass);
        }
      }
      // Item #2: ?
      // Item #3: level range
      if (size>=3)
      {
        PropertiesSet levelProps=(PropertiesSet)filterArray[2];
        handleLevelFilter(levelProps);
      }
    }
  }

  private void handleLevelFilter(PropertiesSet levelProps)
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
        int max=((Integer)maxArray[i]).intValue();
        System.out.println("\tLevel range: "+min+"-"+max);
      }
    }
  }

  private void handleLootTable(int lootTableId)
  {
    _lootLoader.handleTrophyList(lootTableId);
  }

  private void handleEffects(PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effects!=null)
    {
      for(Object effectObj : effects)
      {
        PropertiesSet effectItemProps=(PropertiesSet)effectObj;
        Integer effectId=(Integer)effectItemProps.getProperty("EffectGenerator_EffectID");
        if (effectId!=null)
        {
          handleEffect(effectId.intValue());
        }
      }
    }
  }

  private void handleEffect(int effectId)
  {
    PropertiesSet effectProps=_facade.loadProperties(effectId+0x09000000);
    Integer treasureListTemplateId=(Integer)effectProps.getProperty("Effect_Lootgen_DiscoveryTreasureListTemplate");
    if (treasureListTemplateId!=null)
    {
      PropertiesSet treasureListProps=_facade.loadProperties(treasureListTemplateId.intValue()+0x09000000);
      _lootLoader.handleTreasureList(treasureListProps);
    }
  }

  private void doIt()
  {
    // Battle Gift Box
    load(1879303552);
    // Cosmetic Gift Box
    load(1879303553);
    // Ancient Riddermark Scroll Case III
    load(1879265139);
    // Coffer of Adventurer's Armour - Heavy (Incomparable: 1879378494)
    load(1879378494);
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
