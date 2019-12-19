package delta.games.lotro.tools.dat.others.boxes;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.dat.DATConstants;
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

  /**
   * Load a container.
   * @param indexDataId Container item identifier.
   * @return the loaded container.
   */
  public Object load(int indexDataId)
  {
    Object ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      boolean used=false;
      // Filtered trophy table
      Integer filteredLootTableId=(Integer)properties.getProperty("PackageItem_FilteredTrophyTableTemplate");
      if (filteredLootTableId!=null)
      {
        _lootLoader.handleFilteredTreasureList(filteredLootTableId.intValue());
        used=true;
      }
      // Free-people weighted treasure table ID
      Integer freepWeightedTreasureTableId=(Integer)properties.getProperty("PackageItem_Freep_WeightedTreasureTableID");
      if ((freepWeightedTreasureTableId!=null) && (freepWeightedTreasureTableId.intValue()!=0))
      {
        _lootLoader.handleWeightedTreasureTable(freepWeightedTreasureTableId.intValue());
        used=true;
      }
      // Trophy template
      Integer trophyTemplateId=(Integer)properties.getProperty("PackageItem_TrophyListTemplate");
      if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
      {
        _lootLoader.handleTrophyList(trophyTemplateId.intValue());
        used=true;
      }
      // Effects (for scrolls)
      boolean effectsUsed=handleEffects(properties);
      if (effectsUsed)
      {
        used=true;
      }

      if (used)
      {
        // Name
        String name=DatUtils.getStringProperty(properties,"Name");
        System.out.println("Container: "+name);
        //System.out.println(properties.dump());
      }
     /*
PackageItem_Freep_WeightedTreasureTableID: 0
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

  private boolean handleEffects(PropertiesSet properties)
  {
    boolean used=false;
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effects!=null)
    {
      for(Object effectObj : effects)
      {
        PropertiesSet effectItemProps=(PropertiesSet)effectObj;
        Integer effectId=(Integer)effectItemProps.getProperty("EffectGenerator_EffectID");
        if (effectId!=null)
        {
          boolean effectUsed=handleEffect(effectId.intValue());
          if (effectUsed)
          {
            used=true;
          }
        }
      }
    }
    return used;
  }

  private boolean handleEffect(int effectId)
  {
    boolean used=false;
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    Integer treasureListTemplateId=(Integer)effectProps.getProperty("Effect_Lootgen_DiscoveryTreasureListTemplate");
    if (treasureListTemplateId!=null)
    {
      PropertiesSet treasureListProps=_facade.loadProperties(treasureListTemplateId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      _lootLoader.handleTreasureList(treasureListTemplateId.intValue(),treasureListProps);
      used=true;
    }
    return used;
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
    // Coffer of Adventurer's Jewellery - Might (Incomparable: 1879378473)
    load(1879378473);
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
