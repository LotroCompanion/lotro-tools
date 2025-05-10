package delta.games.lotro.tools.extraction.agents.mobs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.games.lotro.common.action.ActionTables;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.MobDivision;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobLoot;
import delta.games.lotro.lore.agents.mobs.io.xml.MobsXMLWriter;
import delta.games.lotro.tools.extraction.GeneratedFiles;
import delta.games.lotro.tools.extraction.agents.AgentLoader;
import delta.games.lotro.tools.extraction.agents.ClassificationLoader;
import delta.games.lotro.tools.extraction.common.PlacesLoader;
import delta.games.lotro.tools.extraction.common.worldEvents.WorldEventsLoader;
import delta.games.lotro.tools.extraction.effects.EffectLoader;
import delta.games.lotro.tools.extraction.loot.LootLoader;
import delta.games.lotro.tools.extraction.misc.actions.ActionTablesLoader;
import delta.games.lotro.tools.extraction.utils.i18n.I18nUtils;
import delta.games.lotro.tools.utils.DataFacadeBuilder;

/**
 * Get mobs definitions from DAT files.
 * @author DAM
 */
public class MainDatMobsLoader
{
  private static final Logger LOGGER=LoggerFactory.getLogger(MainDatMobsLoader.class);

  private DataFacade _facade;
  private I18nUtils _i18n;
  // Classification
  private ClassificationLoader _classificationLoader;
  private LotroEnum<MobDivision> _mobDivision;
  // Loots
  private EffectLoader _effectLoader;
  private LootLoader _lootLoader;
  // Action tables
  private ActionTablesLoader _actionTablesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootLoader Loot loader.
   * @param effectsLoader Effects loader.
   */
  public MainDatMobsLoader(DataFacade facade, LootLoader lootLoader, EffectLoader effectsLoader)
  {
    _facade=facade;
    _i18n=new I18nUtils("mobs",facade.getGlobalStringsManager());
    _classificationLoader=new ClassificationLoader(facade);
    _effectLoader=effectsLoader;
    _mobDivision=LotroEnumsRegistry.getInstance().get(MobDivision.class);
    _lootLoader=lootLoader;
    _actionTablesLoader=new ActionTablesLoader(facade);
  }

  private MobDescription load(int mobId)
  {
    PropertiesSet properties=_facade.loadProperties(mobId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not handle mob ID={}",Integer.valueOf(mobId));
      return null;
    }
    // Name
    String name=_i18n.getNameStringProperty(properties,"Name",mobId,I18nUtils.OPTION_REMOVE_TRAILING_MARK);
    MobDescription ret=new MobDescription(mobId,name);
    // Classification
    _classificationLoader.loadClassification(properties,ret.getClassification());
    // Loot
    TrophyList barterTrophyList=null;
    Integer barterTrophyListId=(Integer)properties.getProperty("LootGen_BarterTrophyList");
    if ((barterTrophyListId!=null) && (barterTrophyListId.intValue()!=0))
    {
      barterTrophyList=_lootLoader.getTrophyList(barterTrophyListId.intValue());
    }
    boolean generatesTrophy=(((Integer)properties.getProperty("LootGen_GeneratesTrophies")).intValue()!=0);
    TrophyList reputationTrophyList=null;
    Integer reputationTrophyListId=(Integer)properties.getProperty("LootGen_ReputationTrophyList");
    if ((reputationTrophyListId!=null) && (reputationTrophyListId.intValue()!=0))
    {
      reputationTrophyList=_lootLoader.getTrophyList(reputationTrophyListId.intValue());
    }
    TreasureList treasureList=null;
    int treasureListOverrideId=((Integer)properties.getProperty("LootGen_TreasureList_Override")).intValue();
    if (treasureListOverrideId!=0)
    {
      treasureList=_lootLoader.getTreasureList(treasureListOverrideId);
    }
    TrophyList trophyListOverride=null;
    int trophyListOverrideId=((Integer)properties.getProperty("LootGen_TrophyList_Override")).intValue();
    if (trophyListOverrideId!=0)
    {
      trophyListOverride=_lootLoader.getTrophyList(trophyListOverrideId);
    }
    Integer isRemoteLootableInt=(Integer)properties.getProperty("Loot_IsRemoteLootable");
    boolean remoteLootable=((isRemoteLootableInt!=null) && (isRemoteLootableInt.intValue()>0));
    if ((barterTrophyList!=null) || (reputationTrophyList!=null) || (treasureList!=null) || (trophyListOverride!=null))
    {
      MobLoot loot=new MobLoot();
      loot.setBarterTrophy(barterTrophyList);
      loot.setReputationTrophy(reputationTrophyList);
      loot.setTreasureListOverride(treasureList);
      loot.setTrophyListOverride(trophyListOverride);
      loot.setGeneratesTrophy(generatesTrophy);
      loot.setRemoteLootable(remoteLootable);
      ret.setMobLoot(loot);
    }
    /*
Agent_Alignment: 3
Agent_Class: 51
Agent_Classification: 1879049378
       ******** Properties: 1879049378
  Classification_Alignment: 3
  Classification_Genus: 8192
  Classification_Species: 66
Agent_ClassificationFilter: 2
  Enum: ClassificationFilterType, (id=587202575) => 2=Monster
Agent_Genus: 8192
Agent_ShowSubspecies: 0
Agent_Species: 66
Agent_Subspecies: 132
Quest_MonsterDivision: 245 => HallOfMirror
     */
    MobDivision mobDivision=null;
    Integer mobDivisionCode=(Integer)properties.getProperty("Quest_MonsterDivision");
    if (mobDivisionCode!=null)
    {
      mobDivision=_mobDivision.getEntry(mobDivisionCode.intValue());
      ret.setDivision(mobDivision);
    }
    // Actions tables
    ActionTables table=_actionTablesLoader.loadActionTables(properties);
    ret.setActionTables(table);
    // Effects
    AgentLoader.loadEffects(_effectLoader,properties,ret);
    return ret;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      return (classDefIndex==WStateClass.MOB);
    }
    return false;
  }

  /**
   * Load mobs.
   */
  public void doIt()
  {
    List<MobDescription> mobs=new ArrayList<MobDescription>();
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        MobDescription mob=load(id);
        if (mob!=null)
        {
          mobs.add(mob);
        }
      }
    }
    // Save mobs
    boolean ok=MobsXMLWriter.writeMobsFile(GeneratedFiles.MOBS,mobs);
    if (ok)
    {
      LOGGER.info("Wrote mobs file: {}",GeneratedFiles.MOBS);
    }
    // Save
    // -labels
    _i18n.save();
    // - action tables
    _actionTablesLoader.save();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    PlacesLoader placesLoader=new PlacesLoader(facade);
    EffectLoader effectsLoader=new EffectLoader(facade,placesLoader);
    LootsManager lootsManager=new LootsManager();
    WorldEventsLoader worldEventsLoader=new WorldEventsLoader(facade);
    LootLoader lootLoader=new LootLoader(facade,worldEventsLoader,lootsManager);
    new MainDatMobsLoader(facade,lootLoader,effectsLoader).doIt();
    facade.dispose();
  }
}
