package delta.games.lotro.tools.dat.agents.mobs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.io.xml.TreasureXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobLoot;
import delta.games.lotro.lore.agents.mobs.io.xml.MobsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.agents.ClassificationLoader;
import delta.games.lotro.tools.dat.others.LootLoader;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Get mobs definitions from DAT files.
 * @author DAM
 */
public class MainDatMobsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatMobsLoader.class);

  private DataFacade _facade;
  // Classification
  private ClassificationLoader _classificationLoader;
  // Loots
  private LootsManager _loots;
  private LootLoader _lootLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   * @param lootsManager Loots manager.
   */
  public MainDatMobsLoader(DataFacade facade, LootsManager lootsManager)
  {
    _facade=facade;
    _classificationLoader=new ClassificationLoader(facade);
    _loots=lootsManager;
    _lootLoader=new LootLoader(facade,_loots);
  }

  private MobDescription load(int indexDataId)
  {
    MobDescription ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // Name
      String name=DatUtils.getStringProperty(properties,"Name");
      name=StringUtils.fixName(name);
      ret=new MobDescription(indexDataId,name);
      //System.out.println("ID="+indexDataId+", Name: "+name);
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
    }
    else
    {
      LOGGER.warn("Could not handle mob ID="+indexDataId);
    }
    return ret;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
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
      System.out.println("Wrote mobs file: "+GeneratedFiles.MOBS);
    }
    // Write loot data
    TreasureXMLWriter.writeLootsFile(GeneratedFiles.LOOTS,_loots);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    LootsManager lootsManager=new LootsManager();
    new MainDatMobsLoader(facade,lootsManager).doIt();
    facade.dispose();
  }
}
