package delta.games.lotro.tools.dat.npc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.mobs.MobDescription;
import delta.games.lotro.lore.mobs.io.xml.MobsXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.others.LootLoader;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.StringUtils;

/**
 * Get legendary titles definitions from DAT files.
 * @author DAM
 */
public class MainDatMobsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatMobsLoader.class);

  private DataFacade _facade;
  private LootsManager _loots;
  private LootLoader _lootLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatMobsLoader(DataFacade facade)
  {
    _facade=facade;
    _loots=new LootsManager();
    _lootLoader=new LootLoader(facade,_loots);
  }

  private MobDescription load(int indexDataId)
  {
    MobDescription ret=null;
    PropertiesSet properties=_facade.loadProperties(indexDataId+DATConstants.DBPROPERTIES_OFFSET);
    if (properties!=null)
    {
      // ID
      // Name
      String name=DatUtils.getStringProperty(properties,"Name");
      name=StringUtils.fixName(name);
      ret=new MobDescription(indexDataId,name);
      //System.out.println("ID="+indexDataId+", Name: "+name);
      int barterTrophyList=((Integer)properties.getProperty("LootGen_BarterTrophyList")).intValue();
      if (barterTrophyList!=0)
      {
        //System.out.println("\tLootGen_BarterTrophyList="+barterTrophyList);
        TrophyList trophyList=_lootLoader.handleTrophyList(barterTrophyList);
        if (trophyList!=null)
        {
          System.out.println(trophyList.toString());
        }
      }
      /*
      int generatesTrophy=((Integer)properties.getProperty("LootGen_GeneratesTrophies")).intValue();
      if (generatesTrophy!=0)
      {
        System.out.println("\tLootGen_GeneratesTrophies="+generatesTrophy);
      }
      */
      int reputationTrophyList=((Integer)properties.getProperty("LootGen_ReputationTrophyList")).intValue();
      if (reputationTrophyList!=0)
      {
        //System.out.println("\tLootGen_ReputationTrophyList="+reputationTrophyList);
        TrophyList trophyList=_lootLoader.handleTrophyList(reputationTrophyList);
        if (trophyList!=null)
        {
          System.out.println(trophyList.toString());
        }
      }
      int treasureListOverride=((Integer)properties.getProperty("LootGen_TreasureList_Override")).intValue();
      if (treasureListOverride!=0)
      {
        //System.out.println("\tLootGen_TreasureList_Override="+treasureListOverride);
        PropertiesSet treasureListProps=_facade.loadProperties(treasureListOverride+DATConstants.DBPROPERTIES_OFFSET);
        TreasureList treasureList=_lootLoader.handleTreasureList(treasureListOverride,treasureListProps);
        if (treasureList!=null)
        {
          System.out.println(treasureList.toString());
        }
      }
      int trophyListOverride=((Integer)properties.getProperty("LootGen_TrophyList_Override")).intValue();
      if (trophyListOverride!=0)
      {
        //System.out.println("\tLootGen_TrophyList_Override="+trophyListOverride);
        TrophyList trophyList=_lootLoader.handleTrophyList(trophyListOverride);
        if (trophyList!=null)
        {
          System.out.println(trophyList.toString());
        }
      }
      Integer isRemoteLootable=(Integer)properties.getProperty("Loot_IsRemoteLootable");
      if ((isRemoteLootable==null) || (isRemoteLootable.intValue()!=1))
      {
        System.out.println("\tLoot_IsRemoteLootable="+isRemoteLootable);
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
      LOGGER.warn("Could not handle legendary title ID="+indexDataId);
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

  private void doIt()
  {
    List<MobDescription> mobs=new ArrayList<MobDescription>();
    //for(int id=1879079705;id<=1879079705;id++)
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
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatMobsLoader(facade).doIt();
    facade.dispose();
  }
}
