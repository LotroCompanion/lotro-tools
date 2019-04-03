package delta.games.lotro.tools.dat.quests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.Rewards;
import delta.games.lotro.common.Size;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.utils.BufferUtils;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.io.xml.QuestXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.utils.DatUtils;

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
  private EnumMapper _questRoleAction;

  private DatRewardsLoader _rewardsLoader;
  private DatObjectivesLoader _objectivesLoader;

  HashSet<String> propNames=new HashSet<String>();
  HashSet<String> propNames2=new HashSet<String>();
  HashSet<String> propNames3=new HashSet<String>();

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatQuestsLoader(DataFacade facade)
  {
    _facade=facade;
    _quests=new ArrayList<QuestDescription>();
    _category=_facade.getEnumsManager().getEnumMapper(587202585);
    _questRoleAction=_facade.getEnumsManager().getEnumMapper(587202589);
    _rewardsLoader=new DatRewardsLoader(facade);
    _objectivesLoader=new DatObjectivesLoader(facade);
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

  private int nb=0;
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
      quest.setTitle(name);
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
      Integer minLevel=((Integer)properties.getProperty("Quest_ChallengeLevel"));
      quest.setMinimumLevel(minLevel);
      // Scope
      handleScope(quest, properties);
      // Monster play?
      Integer isMonsterPlay=((Integer)properties.getProperty("Quest_IsMonsterPlayQuest"));
      if ((isMonsterPlay!=null) && (isMonsterPlay.intValue()!=1))
      {
        System.out.println("Monster play!");
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
      Integer nextQuestId=((Integer)properties.getProperty("Quest_NextQuest"));
      if (nextQuestId!=null)
      {
        System.out.println("Next quest: "+nextQuestId);
      }
      // Flags
      handleFlags(quest,properties);

      // Rewards
      Rewards rewards=quest.getQuestRewards();
      _rewardsLoader.fillRewards(properties,rewards);
      _rewardsLoader.handleQuestRewards(rewards,properties);

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
      //handleRoles(quest,properties);
      //handleRoles2(quest,properties);
      //handleRoles3(quest,properties);

      // Objectives
      _objectivesLoader.handleObjectives(properties);
      // Web Store (needed xpack/region): WebStoreAccountItem_DataID
      nb++;
      _quests.add(quest);
    }
    else
    {
      LOGGER.warn("Could not handle quest ID="+indexDataId);
    }
    return quest;
  }

  private void handleScope(QuestDescription quest, PropertiesSet properties)
  {
    Integer scope=((Integer)properties.getProperty("Quest_Scope"));
    if (scope!=null)
    {
      int value=scope.intValue();
      if (value==1)
      {
        quest.setRepeatable(true);
      }
      else if (value==2)
      {
        quest.setSize(Size.FELLOWSHIP);
      }
      else if (value==3)
      {
        quest.setSize(Size.SMALL_FELLOWSHIP);
      }
      else if (value==5)
      {
        // Normal...
        quest.setSize(Size.SOLO);
      }
      else if (value==9)
      {
        System.out.println("Instance");
      }
      else
      {
        LOGGER.warn("Unmanaged scope: "+value);
      }
    }
  }

  private void handleFlags(QuestDescription quest, PropertiesSet properties)
  {
    Integer smallFellowshipRecommended=((Integer)properties.getProperty("Quest_IsSmallFellowshipRecommended"));
    if (smallFellowshipRecommended!=null) System.out.println("IS Small Fellowship recommended: "+smallFellowshipRecommended);
    Integer fellowshipRecommended=((Integer)properties.getProperty("Quest_IsFellowshipRecommended"));
    if (fellowshipRecommended!=null) System.out.println("IS Fellowship recommended: "+fellowshipRecommended);
    Integer isInstanceQuest=((Integer)properties.getProperty("Quest_IsInstanceQuest"));
    if (isInstanceQuest!=null) System.out.println("IS Instance quest: "+isInstanceQuest);
    Integer isRaidQuest=((Integer)properties.getProperty("Quest_IsRaidQuest"));
    if (isRaidQuest!=null) System.out.println("IS Raid quest: "+isRaidQuest);
    Integer isSessionQuest=((Integer)properties.getProperty("Quest_IsSessionQuest"));
    if (isSessionQuest!=null) System.out.println("IS session quest: "+isSessionQuest);
    Integer isSessionAcc=((Integer)properties.getProperty("Quest_IsSessionAccomplishment"));
    if (isSessionAcc!=null) System.out.println("IS session acc: "+isSessionAcc);
    Integer isShareable=((Integer)properties.getProperty("Quest_IsShareable"));
    if (isShareable!=null) System.out.println("IS shareable quest: "+isShareable);
    Integer isHidden=((Integer)properties.getProperty("Quest_IsHidden"));
    if (isHidden!=null) System.out.println("IS hidden quest: "+isHidden);
    Integer isPeBestowedQuest=((Integer)properties.getProperty("Quest_IsPEBestowedQuest"));
    if (isPeBestowedQuest!=null) System.out.println("IS PE bestowed quest: "+isPeBestowedQuest);
  }

  void handleRoles(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_BestowalRoles
    Object[] roles=(Object[])properties.getProperty("Quest_BestowalRoles");
    if (roles!=null)
    {
      System.out.println("Roles (bestower):");
      int index=0;
      for(Object roleObj : roles)
      {
        System.out.println("Index: "+index);
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        propNames.addAll(roleProps.getPropertyNames());
        //[{QuestDispenser_RoleSuccessText=[Ljava.lang.String;@55536d9e, QuestDispenser_NPC=1879048194}]
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=getNpc(npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String dispenserText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_TextArray",Markers.CHARACTER);
        if (dispenserText!=null)
        {
          System.out.println("\tDispenser text: "+dispenserText);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText",Markers.CHARACTER);
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  void handleRoles2(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_GlobalRoles
    Object[] roles=(Object[])properties.getProperty("Quest_GlobalRoles");
    if (roles!=null)
    {
      System.out.println("Roles (global):");
      int index=0;
      for(Object roleObj : roles)
      {
        System.out.println("Index: "+index);
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        propNames2.addAll(roleProps.getPropertyNames());
        Integer dispenserAction=(Integer)roleProps.getProperty("QuestDispenser_Action");
        if (dispenserAction!=null)
        {
          String action=_questRoleAction.getString(dispenserAction.intValue());
          System.out.println("\tdispenserAction: " +action);
        }
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=getNpc(npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        if (dispenserRoleName!=null)
        {
          System.out.println("\tdispenserRolename: " +dispenserRoleName);
        }
        String dispenserRoleConstraint=(String)roleProps.getProperty("QuestDispenser_RoleConstraint");
        if (dispenserRoleConstraint!=null)
        {
          System.out.println("\tdispenserRole Constraint: " +dispenserRoleConstraint);
        }
        String dispenserText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_TextArray",Markers.CHARACTER);
        if (dispenserText!=null)
        {
          System.out.println("\tDispenser text: "+dispenserText);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText","{***}");
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  void handleRoles3(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_RoleArray
    Object[] roles=(Object[])properties.getProperty("Quest_RoleArray");
    if (roles!=null)
    {
      System.out.println("Role3:");
      int index=0;
      for(Object roleObj : roles)
      {
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        System.out.println("Index: "+index);
        propNames3.addAll(roleProps.getPropertyNames());
        // [QuestDispenser_Action, QuestDispenser_RoleSuccessText, Quest_ObjectiveIndex, QuestDispenser_NPC, QuestDispenser_RoleName]
        Integer dispenserAction=(Integer)roleProps.getProperty("QuestDispenser_Action");
        if (dispenserAction!=null)
        {
          String action=_questRoleAction.getString(dispenserAction.intValue());
          System.out.println("\tdispenserAction: " +action);
        }
        Integer objectiveIndex=(Integer)roleProps.getProperty("Quest_ObjectiveIndex");
        if (objectiveIndex!=null)
        {
          System.out.println("\tobjectiveIndex: " +objectiveIndex);
        }
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        if (dispenserRoleName!=null)
        {
          System.out.println("\tdispenserRolename: " +dispenserRoleName);
        }
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=getNpc(npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText",Markers.CHARACTER);
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  private String getNpc(int npcId)
  {
    int dbPropertiesId=npcId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      String npcName=DatUtils.getStringProperty(properties,"Name");
      return npcName;
    }
    return null;
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
    System.out.println("Nb quests: "+nb);
    QuestXMLWriter.writeQuestsFile(GeneratedFiles.QUESTS,quests);
    System.out.println(_objectivesLoader.eventIds);
    System.out.println("Role props: "+propNames);
    System.out.println("Role2 props: "+propNames2);
    System.out.println("Role3 props:" +propNames3);
    //System.out.println("Places: "+PlaceLoader._names);
  }

  void doIt2()
  {
    PropertiesSet questArcsDirectory=_facade.loadProperties(0x7900E36F);
    //System.out.println(questArcsDirectory.dump());
    Object[] list=(Object[])questArcsDirectory.getProperty("QuestArcDirectory_QuestArc_Array");
    for(Object obj : list)
    {
      Integer arcId=(Integer)obj;
      handleArc(arcId.intValue());
    }
    System.out.println("Nb quests: "+nb);
    //DeedsWriter.writeSortedDeeds(_deeds,new File("deeds_dat.xml").getAbsoluteFile());
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
