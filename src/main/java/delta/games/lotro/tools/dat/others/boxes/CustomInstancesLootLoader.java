package delta.games.lotro.tools.dat.others.boxes;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.tools.dat.others.LootLoader;

/**
 * Get the contents of custom skirmish/instances chests from DAT files.
 * @author DAM
 */
public class CustomInstancesLootLoader
{
  private static final Logger LOGGER=Logger.getLogger(CustomInstancesLootLoader.class);

  private DataFacade _facade;
  private LootLoader _lootLoader;
  private EnumMapper _difficultyTiers;
  private EnumMapper _groupSize;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootLoader Loot loader.
   */
  public CustomInstancesLootLoader(DataFacade facade, LootLoader lootLoader)
  {
    _facade=facade;
    _lootLoader=lootLoader;
    _difficultyTiers=facade.getEnumsManager().getEnumMapper(0x230002DC);
    _groupSize=facade.getEnumsManager().getEnumMapper(0x230002DA);
  }

  /**
   * Handle a custom skirmish loot lookup table.
   * @param tableId Table identifier.
   */
  public void handleCustomSkirmishLootLookupTable(int tableId)
  {
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return;
    }
    Object[] tableArray=(Object[])properties.getProperty("SkirmishCustomLootLookupTable_Array");
    for(Object tableItemObj : tableArray)
    {
      int lookupEntryId=((Integer)tableItemObj).intValue();
      handleEntry(lookupEntryId);
    }
  }

  @SuppressWarnings("unused")
  private void handleEntry(int lookupEntryId)
  {
    PropertiesSet properties=_facade.loadProperties(lookupEntryId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return;
    }
    /*
      SkirmishTreasureLookup_BarterTrophyListDID: 0
      SkirmishTreasureLookup_EncounterDID: 1879162688
      SkirmishTreasureLookup_ReputationTrophyListDID: 0
      SkirmishTreasureLookup_SkirmishMatchParametersDID: 1879193916
      SkirmishTreasureLookup_TreasureListTemplateDID: 0
      SkirmishTreasureLookup_TrophyListTemplateDID: 1879194188
    */
    int encounterId=((Integer)properties.getProperty("SkirmishTreasureLookup_EncounterDID")).intValue();
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    PrivateEncounter privateEncounter=peMgr.getPrivateEncounterById(encounterId);
    //System.out.println("Encounter: "+privateEncounter.getName());
    int parametersDid=((Integer)properties.getProperty("SkirmishTreasureLookup_SkirmishMatchParametersDID")).intValue();
    handleParameters(parametersDid);
    handleLootTables(properties);
  }

  @SuppressWarnings("unused")
  private void handleLootTables(PropertiesSet properties)
  {
    TrophyList barterTrophyList=null;
    TreasureList treasureList=null;
    TrophyList trophyList=null;
    // Barter trophy list
    Integer barterTrophyListId=(Integer)properties.getProperty("SkirmishTreasureLookup_BarterTrophyListDID");
    if ((barterTrophyListId!=null) && (barterTrophyListId.intValue()!=0))
    {
      barterTrophyList=_lootLoader.handleTrophyList(barterTrophyListId.intValue());
    }
    // Reputation trophy
    Integer reputationTrophyListId=(Integer)properties.getProperty("SkirmishTreasureLookup_ReputationTrophyListDID");
    if ((reputationTrophyListId!=null) && (reputationTrophyListId.intValue()!=0))
    {
      // Never happens
      LOGGER.warn("Reputation trophy - should never happen");
      //reputationTrophyList=_lootLoader.handleReputationTrophyList(reputationTrophyListId.intValue());
      //System.out.println(reputationTrophyList);
    }
    // Treasure list template
    Integer treasureListTemplateId=(Integer)properties.getProperty("SkirmishTreasureLookup_TreasureListTemplateDID");
    if ((treasureListTemplateId!=null) && (treasureListTemplateId.intValue()!=0))
    {
      PropertiesSet treasureListProps=_facade.loadProperties(treasureListTemplateId.intValue()+DATConstants.DBPROPERTIES_OFFSET);
      treasureList=_lootLoader.handleTreasureList(treasureListTemplateId.intValue(),treasureListProps);
    }
    // Trophy list template
    Integer trophyTemplateId=(Integer)properties.getProperty("SkirmishTreasureLookup_TrophyListTemplateDID");
    if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
    {
      trophyList=_lootLoader.handleTrophyList(trophyTemplateId.intValue());
    }
  }

  @SuppressWarnings("unused")
  private void handleParameters(int parametersDid)
  {
    PropertiesSet properties=_facade.loadProperties(parametersDid+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return;
    }
    /*
      SkirmishMatchParams_DifficultyTier_Max: 1 (Tier I)
      SkirmishMatchParams_DifficultyTier_Min: 1 (Tier I)
      SkirmishMatchParams_GroupSize_Max: 3 (Small Fellowship)
      SkirmishMatchParams_GroupSize_Min: 1 (Solo)
      SkirmishMatchParams_Level_Max: 29
      SkirmishMatchParams_Level_Min: 1
     */
    int minLevel=((Integer)properties.getProperty("SkirmishMatchParams_Level_Min")).intValue();
    int maxLevel=((Integer)properties.getProperty("SkirmishMatchParams_Level_Max")).intValue();
    int minSizeCode=((Integer)properties.getProperty("SkirmishMatchParams_GroupSize_Min")).intValue();
    int maxSizeCode=((Integer)properties.getProperty("SkirmishMatchParams_GroupSize_Max")).intValue();
    String minGroupSize=_groupSize.getString(minSizeCode);
    String maxGroupSize=_groupSize.getString(maxSizeCode);
    int minDifficultyCode=((Integer)properties.getProperty("SkirmishMatchParams_DifficultyTier_Min")).intValue();
    int maxDifficultyCode=((Integer)properties.getProperty("SkirmishMatchParams_DifficultyTier_Max")).intValue();
    String minDifficultyTier=_difficultyTiers.getString(minDifficultyCode);
    String maxDifficultyTier=_difficultyTiers.getString(maxDifficultyCode);
    //System.out.println("Level "+minLevel+"-"+maxLevel+", size="+minGroupSize+"/"+maxGroupSize+", difficulty="+minDifficultyTier+"/"+maxDifficultyTier);
  }

  /**
   * Load containers.
   */
  public void doIt()
  {
    handleCustomSkirmishLootLookupTable(1879174480);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    LootsManager lootsMgr=new LootsManager();
    LootLoader lootLoader=new LootLoader(facade,lootsMgr);
    new CustomInstancesLootLoader(facade,lootLoader).doIt();
    facade.dispose();
  }
}
