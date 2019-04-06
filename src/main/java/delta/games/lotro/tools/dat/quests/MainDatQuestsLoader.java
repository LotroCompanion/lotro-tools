package delta.games.lotro.tools.dat.quests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.Size;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestDescription.FACTION;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;
import delta.games.lotro.utils.Proxy;

/**
 * Get quests definitions from DAT files.
 * @author DAM
 */
public class MainDatQuestsLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatQuestsLoader.class);

  private static final int DEBUG_ID=1879000000;

  private DataFacade _facade;
  private List<QuestDescription> _quests;
  private EnumMapper _category;

  private DatRewardsLoader _rewardsLoader;
  private DatObjectivesLoader _objectivesLoader;
  private DatRolesLoader _rolesLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatQuestsLoader(DataFacade facade)
  {
    _facade=facade;
    _quests=new ArrayList<QuestDescription>();
    _category=_facade.getEnumsManager().getEnumMapper(587202585);
    _rewardsLoader=new DatRewardsLoader(facade);
    _objectivesLoader=new DatObjectivesLoader(facade);
    _rolesLoader=new DatRolesLoader(facade);
  }

  private void handleArc(int arcId)
  {
    PropertiesSet arcProps=_facade.loadProperties(arcId+0x9000000);
    //System.out.println(arcProps.dump());
    String arcName=DatUtils.getStringProperty(arcProps,"QuestArc_Name");
    System.out.println("Arc name: "+arcName);
    Object[] list=(Object[])arcProps.getProperty("QuestArc_Quest_Array");
    for(Object obj : list)
    {
      Integer questId=(Integer)obj;
      load(questId.intValue());
    }
  }

  private QuestDescription load(int indexDataId)
  {
    QuestDescription quest=null;
    int dbPropertiesId=indexDataId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      //System.out.println("************* "+indexDataId+" *****************");
      if (indexDataId==DEBUG_ID)
      {
        FileIO.writeFile(new File(indexDataId+".props"),properties.dump().getBytes());
        System.out.println(properties.dump());
      }
      // Check
      boolean useIt=useIt(properties);
      if (!useIt)
      {
        //System.out.println("Ignored ID="+indexDataId+", name="+name);
        return null;
      }
      quest=new QuestDescription();
      // ID
      quest.setIdentifier(indexDataId);
      // Name
      String name=DatUtils.getStringProperty(properties,"Quest_Name");
      quest.setName(name);
      // Description
      String description=DatUtils.getStringProperty(properties,"Quest_Description");
      quest.setDescription(description);
      // Category
      Integer categoryId=((Integer)properties.getProperty("Quest_Category"));
      if (categoryId!=null)
      {
        String category=_category.getString(categoryId.intValue());
        quest.setCategory(category);
      }
      // Min level
      Integer minLevel=findMinLevel(properties);
      quest.setMinimumLevel(minLevel);
      // Challenge level
      Integer challengeLevel=findChallengeLevel(properties);
      // Max times
      Integer maxTimes=((Integer)properties.getProperty("Quest_MaxTimesCompletable"));
      if (maxTimes!=null)
      {
        System.out.println("Max times: "+maxTimes);
      }
      // Scope
      //handleScope(quest, properties);
      // Monster play?
      Integer isMonsterPlay=((Integer)properties.getProperty("Quest_IsMonsterPlayQuest"));
      if ((isMonsterPlay!=null) && (isMonsterPlay.intValue()!=0))
      {
        quest.setFaction(FACTION.MONSTER_PLAY);
      }
      // Quests to complete (only for 'level up' quests)
      /*
      Object questsToComplete=properties.getProperty("Quest_QuestsToComplete");
      if (questsToComplete!=null)
      {
        System.out.println(questsToComplete);
      }
      */
      // Items given when the quest is bestowed
      /*
      Object givenItems=properties.getProperty("Quest_GiveItemArray");
      if (givenItems!=null)
      {
        System.out.println(givenItems);
      }
      */
      // Chain
      // - previous
      findPreviousQuests(quest,properties);
      // - next
      Integer nextQuestId=((Integer)properties.getProperty("Quest_NextQuest"));
      if (nextQuestId!=null)
      {
        Proxy<QuestDescription> proxy=new Proxy<QuestDescription>();
        proxy.setId(nextQuestId.intValue());
        quest.setNextQuest(proxy);
        //System.out.println("Next quest: "+nextQuestId);
      }
      // Flags
      handleFlags(quest,properties);
      // Requirements
      findRaceRequirements(quest,properties);
      findClassRequirements(quest,properties);

      // Rewards
      Rewards rewards=quest.getQuestRewards();
      _rewardsLoader.fillRewards(properties,rewards);
      _rewardsLoader.handleQuestRewards(rewards,challengeLevel,properties);

      // Quest Loot Table
      // (additional loot tables that are active when the quest is active)
      /*
      Object lootTable=properties.getProperty("Quest_LootTable");
      if (lootTable!=null)
      {
        // [{Quest_LootMonsterGenus_Array=[Ljava.lang.Object;@3d1cfad4, Quest_LootItemDID=1879051728, Quest_LootItemProbability=100}]
        System.out.println("Loot table:" +lootTable);
      }
      */
      _rolesLoader.loadRoles(quest,properties);

      // Objectives
      _objectivesLoader.handleObjectives(properties);

      // Web Store (needed xpack/region): WebStoreAccountItem_DataID

      _quests.add(quest);
    }
    else
    {
      LOGGER.warn("Could not handle quest ID="+indexDataId);
    }
    return quest;
  }

  /*
  private void handleScope(QuestDescription quest, PropertiesSet properties)
  {
    Integer scope=((Integer)properties.getProperty("Quest_Scope"));
    if (scope!=null)
    {
      int value=scope.intValue();
      if (value==1)
      {
        //System.out.println("Scope 1: "+quest.getTitle()); // Task (x417) matches category="Task"
        quest.setRepeatable(true);
      }
      else if (value==2)
      {
        //System.out.println("Scope 2: "+quest.getTitle()); // Crafting (x42)
        quest.setSize(Size.FELLOWSHIP);
      }
      else if (value==3)
      {
        //System.out.println("Scope 3: "+quest.getTitle()); // Epic (x ~1k)
        quest.setSize(Size.SMALL_FELLOWSHIP);
      }
      else if (value==5)
      {
        //System.out.println("Scope 5: "+quest.getTitle()); // Legendary Items (x12) (eg Moria or Eregion instance that require shards ; Defence of Glatrev)?
        // Normal...
        quest.setSize(Size.SOLO);
      }
      else if (value==9)
      {
        //System.out.println("Scope 9: "+quest.getTitle()); // Allegiance quests (x44)
        System.out.println("Instance");
      }
      else
      {
        LOGGER.warn("Unmanaged scope: "+value);
      }
    }
  }
  */

  private void handleFlags(QuestDescription quest, PropertiesSet properties)
  {
    // Size
    // - small fellowship
    Integer smallFellowshipRecommended=((Integer)properties.getProperty("Quest_IsSmallFellowshipRecommended"));
    if ((smallFellowshipRecommended!=null) && (smallFellowshipRecommended.intValue()==1))
    {
      quest.setSize(Size.SMALL_FELLOWSHIP);
      //System.out.println("IS Small Fellowship recommended: "+smallFellowshipRecommended);
    }
    // - fellowship
    Integer fellowshipRecommended=((Integer)properties.getProperty("Quest_IsFellowshipRecommended"));
    if ((fellowshipRecommended!=null) && (fellowshipRecommended.intValue()==1))
    {
      quest.setSize(Size.FELLOWSHIP);
      //System.out.println("IS Fellowship recommended: "+fellowshipRecommended);
    }
    // - raid
    Integer isRaidQuest=((Integer)properties.getProperty("Quest_IsRaidQuest"));
    if ((isRaidQuest!=null) && (isRaidQuest.intValue()==1))
    {
      quest.setSize(Size.RAID);
      //System.out.println("IS Raid quest: "+isRaidQuest);
    }
    // Instance quest? Default is no.
    Integer isInstanceQuest=((Integer)properties.getProperty("Quest_IsInstanceQuest"));
    if ((isInstanceQuest!=null) && (isInstanceQuest.intValue()==1))
    {
      quest.setInstanced(true);
      //System.out.println("IS Instance quest: "+isInstanceQuest);
    }
    // Shareable? Default is yes.
    Integer isShareable=((Integer)properties.getProperty("Quest_IsShareable"));
    if ((isShareable!=null) && (isShareable.intValue()==0))
    {
      quest.setShareable(false);
      //System.out.println("IS shareable quest: "+isShareable);
    }
    // Session play? Default is no.
    Integer isSessionQuest=((Integer)properties.getProperty("Quest_IsSessionQuest"));
    if ((isSessionQuest!=null) && (isSessionQuest.intValue()==1))
    {
      quest.setSessionPlay(true);
      //System.out.println("IS session quest: "+isSessionQuest);
    }
    // Automatic bestowed quest (landscape, in-instance quest). Default is no.
    Integer isPeBestowedQuest=((Integer)properties.getProperty("Quest_IsPEBestowedQuest"));
    if ((isPeBestowedQuest!=null) && (isPeBestowedQuest.intValue()==1))
    {
      quest.setAutoBestowed(true);
      //System.out.println("IS PE bestowed quest: "+isPeBestowedQuest);
    }
    // Unused:
    // Quest_IsSessionAccomplishment
    // Quest_IsHidden
  }

  // Usage_MaxLevel also exists

  private Integer findMinLevel(PropertiesSet properties)
  {
    Integer minLevel=null;
    PropertiesSet permissions=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissions!=null)
    {
      minLevel=(Integer)permissions.getProperty("Usage_MinLevel");
    }
    /*
DefaultPermissionBlobStruct: 
  Usage_MinLevel: 40
    */
    return minLevel;
  }

  private Integer findChallengeLevel(PropertiesSet properties)
  {
    Integer challengeLevel=(Integer)properties.getProperty("Quest_ChallengeLevel");
    Integer challengeLevelOverrideProperty=(Integer)properties.getProperty("Quest_ChallengeLevelOverrideProperty");
    Integer ignoreDefaultChallengeLevel=(Integer)properties.getProperty("Quest_IgnoreDefaultChallengeLevel");
    boolean ignoreChallengeLevel=((ignoreDefaultChallengeLevel!=null) && (ignoreDefaultChallengeLevel.intValue()!=0));
    if ((ignoreChallengeLevel) || (challengeLevel==null))
    {
      if (challengeLevelOverrideProperty!=null)
      {
        if (challengeLevelOverrideProperty.intValue()==268439569)
        {
          System.out.println("Challenge level is character level");
          challengeLevel=Integer.valueOf(120); // TODO tmp
        }
        else if (challengeLevelOverrideProperty.intValue()==268446666)
        {
          System.out.println("Challenge level is skirmish level");
          challengeLevel=Integer.valueOf(120); // TODO tmp
        }
        else
        {
          LOGGER.warn("Unmanaged challenge level property: "+challengeLevelOverrideProperty);
        }
      }
      else
      {
        //LOGGER.warn("No challenge level property!");
      }
    }
    else
    {
      //System.out.println("Challenge level is: "+challengeLevel);
    }
    /*
    Quest_ChallengeLevel: 100
    Quest_ChallengeLevelOverrideProperty: 268439569
    Quest_IgnoreDefaultChallengeLevel: 1
    */
    return challengeLevel;
  }

  private void findRaceRequirements(QuestDescription quest, PropertiesSet properties)
  {
    /*
    DefaultPermissionBlobStruct: 
      Usage_RequiredRaces: 
        #1: 81
        #2: 23
        #3: 114
    */
  }

  private void findClassRequirements(QuestDescription quest, PropertiesSet properties)
  {
    /*
    DefaultPermissionBlobStruct: 
      Usage_RequiredClassList: 
        #1: 162
    */
  }

  private void findPreviousQuests(QuestDescription quest, PropertiesSet properties)
  {
    PropertiesSet permissions=(PropertiesSet)properties.getProperty("DefaultPermissionBlobStruct");
    if (permissions!=null)
    {
      Object[] questRequirements=(Object[])permissions.getProperty("Usage_QuestRequirements");
      if (questRequirements!=null)
      {
        for(Object questRequirementObj : questRequirements)
        {
          PropertiesSet questRequirementProps=(PropertiesSet)questRequirementObj;
          int operator=((Integer)questRequirementProps.getProperty("Usage_Operator")).intValue();
          int questId=((Integer)questRequirementProps.getProperty("Usage_QuestID")).intValue();
          int questStatus=((Integer)questRequirementProps.getProperty("Usage_QuestStatus")).intValue();
          if ((operator==3) && (questStatus==805306368))
          {
            //System.out.println("Requires completed quest: "+questId);
            Proxy<QuestDescription> proxy=new Proxy<QuestDescription>();
            proxy.setId(questId);
            quest.addPrerequisiteQuest(proxy);
          }
          else
          {
            //LOGGER.warn("Unmanaged quest requirement: operator="+operator+", status="+questStatus+", questId="+questId);
          }
        }
      }
    }
    /*
    DefaultPermissionBlobStruct: 
      Usage_QuestRequirements: 
        #1: 
          Usage_Operator: 3
          Usage_QuestID: 1879048439
          Usage_QuestStatus: 805306368
    */
  }

  private boolean useIt(PropertiesSet properties)
  {
    Object isAccomplishment=properties.getProperty("Quest_IsAccomplishment");
    if (isAccomplishment instanceof Integer)
    {
      if (((Integer)isAccomplishment).intValue()==1)
      {
        return false;
      }
    }
    // Ignore 'Test' quests
    Integer categoryId=((Integer)properties.getProperty("Quest_Category"));
    if ((categoryId!=null) && (categoryId.intValue()==16))
    {
      return false;
    }
    // Ignore 'Level Up' quests (Bullroarer specifics?)
    if ((categoryId!=null) && (categoryId.intValue()==15))
    {
      return false;
    }
    // Name
    String name=DatUtils.getStringProperty(properties,"Quest_Name");
    if (name.startsWith("DNT"))
    {
      return false;
    }
    return true;
  }

  private boolean useId(int id)
  {
    byte[] data=_facade.loadData(id);
    if (data!=null)
    {
      //int did=BufferUtils.getDoubleWordAt(data,0);
      int classDefIndex=BufferUtils.getDoubleWordAt(data,4);
      //System.out.println(classDefIndex);
      return (classDefIndex==1398); // TODO: use WStateClass constant
    }
    return false;
  }

  private void doIt()
  {
    List<QuestDescription> quests=new ArrayList<QuestDescription>();

    for(int id=0x70000000;id<=0x77FFFFFF;id++)
    //for(int id=DEBUG_ID;id<=DEBUG_ID;id++)
    {
      boolean useIt=useId(id);
      if (useIt)
      {
        QuestDescription quest=load(id);
        if (quest!=null)
        {
          System.out.println("Quest: "+quest.dump());
          quests.add(quest);
        }
      }
    }
    System.out.println("Nb quests: "+quests.size());
    QuestXMLWriter.writeQuestsFile(GeneratedFiles.QUESTS,quests);
  }

  void loadQuestArcs()
  {
    PropertiesSet questArcsDirectory=_facade.loadProperties(0x7900E36F);
    //System.out.println(questArcsDirectory.dump());
    Object[] list=(Object[])questArcsDirectory.getProperty("QuestArcDirectory_QuestArc_Array");
    for(Object obj : list)
    {
      Integer arcId=(Integer)obj;
      handleArc(arcId.intValue());
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatQuestsLoader(facade).doIt();
    facade.dispose();
  }
}
