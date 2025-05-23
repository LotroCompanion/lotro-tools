package delta.games.lotro.tools.extraction.agents.mobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.MobType;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.agents.mobs.loot.GenericMobLootEntry;
import delta.games.lotro.lore.agents.mobs.loot.GenericMobLootSpec;
import delta.games.lotro.lore.agents.mobs.loot.SpeciesLoot;
import delta.games.lotro.lore.agents.mobs.loot.SpeciesLootsManager;
import delta.games.lotro.lore.agents.mobs.loot.SubSpeciesLoot;
import delta.games.lotro.lore.agents.mobs.loot.io.xml.GenericMobLootXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.common.worldEvents.WorldEventsLoader;
import delta.games.lotro.tools.extraction.loot.LootLoader;

/**
 * Get mob loot data from DAT files.
 * @author DAM
 */
public class MainDatGenericMobLootLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatGenericMobLootLoader.class);

  private DataFacade _facade;
  // Loots
  private LootLoader _lootLoader;
  // Data
  private SpeciesLootsManager _mgr;
  private String _levelTableProperty;
  private String _lootTableProperty;
  private String _levelProperty;
  private String _treasureProfileProperty;
  private String _trophyListProperty;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootLoader Loot loader.
   */
  public MainDatGenericMobLootLoader(DataFacade facade, LootLoader lootLoader)
  {
    _facade=facade;
    _lootLoader=lootLoader;
    boolean isLive=LotroCoreConfig.isLive();
    String prefix=isLive?"LevelBasedLootTable":"SpeciesLevelLootTable";
    _levelTableProperty=prefix+"_LevelTable";
    _lootTableProperty=prefix+"_LootTable";
    _levelProperty=prefix+"_Level";
    _treasureProfileProperty=prefix+"_TreasureProfile";
    _trophyListProperty=prefix+"_TrophyList";
  }

  private SpeciesLootsManager load()
  {
    // LootGenDirectoryControl
    PropertiesSet properties=_facade.loadProperties(0x70000251+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return null;
    }
    _mgr=new SpeciesLootsManager();
    Object[] array=(Object[])properties.getProperty("LootGenControl_SpeciesLevelLootTableArray");
    for(Object arrayItem : array)
    {
      int tableId=((Integer)arrayItem).intValue();
      handleTable(tableId);
    }
    return _mgr;
  }

  private void handleTable(int tableId)
  {
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return;
    }
    /*
    SpeciesLevelLootTable:
      #1:
        Agent_Species: 7 (Orc)
        Agent_Subspecies: 0 (Undef)
        LevelBasedLootTable_LevelTable: 1879106442
        MonsterLevel_ExaminationModStatType: 0 (Undef)
     */
    Object[] array=(Object[])properties.getProperty("SpeciesLevelLootTable");
    for(Object arrayItem : array)
    {
      PropertiesSet entryProps=(PropertiesSet)arrayItem;
      handleEntry(entryProps);
    }
  }

  private void handleEntry(PropertiesSet entryProps)
  {
    LotroEnumsRegistry registry=LotroEnumsRegistry.getInstance();
    // Species
    LotroEnum<Species> speciesMgr=registry.get(Species.class);
    int speciesCode=((Integer)entryProps.getProperty("Agent_Species")).intValue();
    Species species=speciesMgr.getEntry(speciesCode);
    if (species==null)
    {
      return;
    }
    // Subspecies
    LotroEnum<SubSpecies> subSpeciesMgr=registry.get(SubSpecies.class);
    int subSpeciesCode=((Integer)entryProps.getProperty("Agent_Subspecies")).intValue();
    SubSpecies subspecies=subSpeciesMgr.getEntry(subSpeciesCode);
    // Table ID
    Integer tableId=(Integer)entryProps.getProperty(_levelTableProperty);
    // Mob Type
    LotroEnum<MobType> mobTypeMgr=registry.get(MobType.class);
    int mobTypeCode=((Integer)entryProps.getProperty("MonsterLevel_ExaminationModStatType")).intValue();
    MobType mobType=mobTypeMgr.getEntry(mobTypeCode);

    SpeciesLoot speciesLoot=_mgr.getSpeciesLoot(species);
    if (speciesLoot==null)
    {
      speciesLoot=new SpeciesLoot(species);
      _mgr.addSpeciesLoot(speciesLoot);
    }
    SubSpeciesLoot subSpeciesLoot=speciesLoot.getSubSpeciesLoot(subspecies);
    if (subSpeciesLoot==null)
    {
      subSpeciesLoot=new SubSpeciesLoot(species,subspecies);
      speciesLoot.addSubSpeciesLoot(subSpeciesLoot);
    }
    GenericMobLootSpec mobTypeLoot=subSpeciesLoot.getMobTypeLoot(mobType);
    if (mobTypeLoot==null)
    {
      mobTypeLoot=new GenericMobLootSpec(species,subspecies,mobType);
      subSpeciesLoot.addMobTypeLoot(mobTypeLoot);
    }
    if ((tableId!=null) && (tableId.intValue()!=0))
    {
      handleLootTable(mobTypeLoot,tableId.intValue());
    }
  }

  private void handleLootTable(GenericMobLootSpec mobTypeLoot, int tableId)
  {
    PropertiesSet properties=_facade.loadProperties(tableId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      return;
    }
    /*
******** Properties: 1879106442
LevelBasedLootTable_LootTable:
  #1:
    LevelBasedLootTable_Level: 1
    LevelBasedLootTable_TreasureProfile: 1879064739
    LevelBasedLootTable_TrophyList: 1879064740
     */
    Object[] array=(Object[])properties.getProperty(_lootTableProperty);
    for(Object arrayItem : array)
    {
      PropertiesSet entryProps=(PropertiesSet)arrayItem;
      GenericMobLootEntry entry=handleLevelTableEntry(entryProps);
      mobTypeLoot.addLevelEntry(entry);
    }
  }

  private GenericMobLootEntry handleLevelTableEntry(PropertiesSet entryProps)
  {
    int level=((Integer)entryProps.getProperty(_levelProperty)).intValue();
    TreasureList treasureList=null;
    int treasureProfileId=((Integer)entryProps.getProperty(_treasureProfileProperty)).intValue();
    if (treasureProfileId!=0)
    {
      treasureList=_lootLoader.getTreasureList(treasureProfileId);
      if (treasureList==null)
      {
        LOGGER.warn("Treasure list not found: {}",Integer.valueOf(treasureProfileId));
      }
    }
    TrophyList trophyList=null;
    int trophyListId=((Integer)entryProps.getProperty(_trophyListProperty)).intValue();
    if (trophyListId!=0)
    {
      trophyList=_lootLoader.getTrophyList(trophyListId);
      if (trophyList==null)
      {
        LOGGER.warn("Trophy list not found: {}",Integer.valueOf(trophyListId));
      }
    }
    GenericMobLootEntry entry=new GenericMobLootEntry(level);
    entry.setTreasureList(treasureList);
    entry.setTrophyList(trophyList);
    return entry;
  }

  /**
   * Load data.
   */
  public void doIt()
  {
    SpeciesLootsManager lootsMgr=load();
    // Save mobs
    boolean ok=GenericMobLootXMLWriter.writeMobLootFile(GeneratedFiles.GENERIC_MOB_LOOTS,lootsMgr);
    if (ok)
    {
      LOGGER.info("Wrote generic mobs loot file: {}",GeneratedFiles.GENERIC_MOB_LOOTS);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=new DataFacade();
    LootsManager lootsManager=new LootsManager();
    WorldEventsLoader worldEventsLoader=new WorldEventsLoader(facade);
    LootLoader lootLoader=new LootLoader(facade,worldEventsLoader,lootsManager);
    new MainDatGenericMobLootLoader(facade,lootLoader).doIt();
    facade.dispose();
  }
}
