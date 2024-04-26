package delta.games.lotro.tools.dat.others.boxes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import delta.common.utils.math.Range;
import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.instances.PrivateEncounter;
import delta.games.lotro.lore.instances.PrivateEncountersManager;
import delta.games.lotro.lore.instances.loot.InstanceLootEntry;
import delta.games.lotro.lore.instances.loot.InstanceLootParameters;
import delta.games.lotro.lore.instances.loot.InstanceLoots;
import delta.games.lotro.lore.instances.loot.InstanceLootsTable;
import delta.games.lotro.lore.instances.loot.io.xml.InstanceLootXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
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
  private Map<Integer,InstanceLootsTable> _tables;
  private Map<Integer,InstanceLoots> _loots;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootLoader Loot loader.
   */
  public CustomInstancesLootLoader(DataFacade facade, LootLoader lootLoader)
  {
    _facade=facade;
    _lootLoader=lootLoader;
    _tables=new HashMap<Integer,InstanceLootsTable>();
    _loots=new HashMap<Integer,InstanceLoots>();
  }

  /**
   * Handle a custom skirmish loot lookup table.
   * @param tableId Table identifier.
   * @return the table.
   */
  public InstanceLootsTable handleCustomSkirmishLootLookupTable(int tableId)
  {
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    Integer key=Integer.valueOf(tableId);
    if (_tables.containsKey(key))
    {
      return _tables.get(key);
    }
    _loots.clear();
    LOGGER.info("Doing custom skirmish loot table: "+tableId);
    InstanceLootsTable table=new InstanceLootsTable(tableId);
    Object[] tableArray=(Object[])properties.getProperty("SkirmishCustomLootLookupTable_Array");
    for(Object tableItemObj : tableArray)
    {
      int lookupEntryId=((Integer)tableItemObj).intValue();
      handleEntry(lookupEntryId);
    }
    for(InstanceLoots instanceLoots : _loots.values())
    {
      table.addInstanceLoots(instanceLoots);
    }
    _loots.clear();
    _tables.put(key,table);
    return table;
  }

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
    Integer key=Integer.valueOf(encounterId);
    InstanceLoots instanceLoots=_loots.get(key);
    PrivateEncountersManager peMgr=PrivateEncountersManager.getInstance();
    PrivateEncounter privateEncounter=peMgr.getPrivateEncounterById(encounterId);
    if (instanceLoots==null)
    {
      instanceLoots=new InstanceLoots(privateEncounter);
      _loots.put(key,instanceLoots);
    }
    int parametersDid=((Integer)properties.getProperty("SkirmishTreasureLookup_SkirmishMatchParametersDID")).intValue();
    InstanceLootParameters parameters=handleParameters(parametersDid);
    InstanceLootEntry entry=new InstanceLootEntry(parameters);
    instanceLoots.addEntry(entry);
    handleLootTables(properties,entry);
  }

  private void handleLootTables(PropertiesSet properties, InstanceLootEntry entry)
  {
    TrophyList trophyList=null;
    // Barter trophy list
    Integer barterTrophyListId=(Integer)properties.getProperty("SkirmishTreasureLookup_BarterTrophyListDID");
    if ((barterTrophyListId!=null) && (barterTrophyListId.intValue()!=0))
    {
      LOGGER.warn("Barter trophy list - should never happen");
    }
    // Reputation trophy
    Integer reputationTrophyListId=(Integer)properties.getProperty("SkirmishTreasureLookup_ReputationTrophyListDID");
    if ((reputationTrophyListId!=null) && (reputationTrophyListId.intValue()!=0))
    {
      // Never happens
      LOGGER.warn("Reputation trophy - should never happen");
    }
    // Treasure list template
    Integer treasureListTemplateId=(Integer)properties.getProperty("SkirmishTreasureLookup_TreasureListTemplateDID");
    if ((treasureListTemplateId!=null) && (treasureListTemplateId.intValue()!=0))
    {
      LOGGER.warn("Treasure list - should never happen");
    }
    // Trophy list template
    Integer trophyTemplateId=(Integer)properties.getProperty("SkirmishTreasureLookup_TrophyListTemplateDID");
    if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
    {
      trophyList=_lootLoader.getTrophyList(trophyTemplateId.intValue());
      entry.setTrophyList(trophyList);
    }
  }

  private InstanceLootParameters handleParameters(int parametersDid)
  {
    PropertiesSet properties=_facade.loadProperties(parametersDid+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
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
    Range level=new Range(minLevel,maxLevel);
    int minSizeCode=((Integer)properties.getProperty("SkirmishMatchParams_GroupSize_Min")).intValue();
    int maxSizeCode=((Integer)properties.getProperty("SkirmishMatchParams_GroupSize_Max")).intValue();
    Range size=new Range(minSizeCode,maxSizeCode);
    int minDifficultyCode=((Integer)properties.getProperty("SkirmishMatchParams_DifficultyTier_Min")).intValue();
    int maxDifficultyCode=((Integer)properties.getProperty("SkirmishMatchParams_DifficultyTier_Max")).intValue();
    Range difficulty=new Range(minDifficultyCode,maxDifficultyCode);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Level "+minLevel+"-"+maxLevel+", size="+minSizeCode+"/"+maxSizeCode+", difficulty="+minDifficultyCode+"/"+maxDifficultyCode);
    }
    InstanceLootParameters params=new InstanceLootParameters(difficulty,size,level);
    return params;
  }

  /**
   * Write the managed data.
   */
  public void writeData()
  {
    List<InstanceLootsTable> loots=new ArrayList<InstanceLootsTable>(_tables.values());
    Collections.sort(loots,new IdentifiableComparator<InstanceLootsTable>());
    InstanceLootXMLWriter.writeInstanceLootsFile(GeneratedFiles.INSTANCES_LOOTS,loots);
  }
}
