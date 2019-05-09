package delta.games.lotro.tools.dat.npc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.WStateClass;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.tools.dat.others.LootLoader;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Get legendary titles definitions from DAT files.
 * @author DAM
 */
public class MainDatMobsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatMobsLoader.class);

  private DataFacade _facade;
  private LootLoader _lootLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatMobsLoader(DataFacade facade)
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
      //ret=new LegendaryTitle();
      // ID
      //ret.setIdentifier(indexDataId);
      // Name
      String name=DatUtils.getStringProperty(properties,"Name");
      //ret.setName(name);
      System.out.println("ID="+indexDataId+", Name: "+name);
      int barterTrophyList=((Integer)properties.getProperty("LootGen_BarterTrophyList")).intValue();
      if (barterTrophyList!=0)
      {
        System.out.println("\tLootGen_BarterTrophyList="+barterTrophyList);
        _lootLoader.handleTrophyList(barterTrophyList);
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
        System.out.println("\tLootGen_ReputationTrophyList="+reputationTrophyList);
        _lootLoader.handleTrophyList(reputationTrophyList);
      }
      int treasureListOverride=((Integer)properties.getProperty("LootGen_TreasureList_Override")).intValue();
      if (treasureListOverride!=0)
      {
        System.out.println("\tLootGen_TreasureList_Override="+treasureListOverride);
      }
      int trophyListOverride=((Integer)properties.getProperty("LootGen_TrophyList_Override")).intValue();
      if (trophyListOverride!=0)
      {
        System.out.println("\tLootGen_TrophyList_Override="+trophyListOverride);
        _lootLoader.handleTrophyList(trophyListOverride);
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
    List<Object> titles=new ArrayList<Object>();
    //for(int id=1879079705;id<=1879079705;id++)
    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        Object title=load(id);
        if (title!=null)
        {
          titles.add(title);
        }
      }
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
