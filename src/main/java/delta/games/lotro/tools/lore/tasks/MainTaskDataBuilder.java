package delta.games.lotro.tools.lore.tasks;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.lore.quests.QuestsManager;
import delta.games.lotro.lore.quests.objectives.InventoryItemCondition;
import delta.games.lotro.lore.quests.objectives.Objective;
import delta.games.lotro.lore.quests.objectives.ObjectiveCondition;
import delta.games.lotro.lore.quests.objectives.ObjectivesManager;
import delta.games.lotro.lore.tasks.Task;
import delta.games.lotro.lore.tasks.io.xml.TasksXMLWriter;
import delta.games.lotro.tools.dat.GeneratedFiles;

/**
 * Builder for task related data.
 * @author DAM
 */
public class MainTaskDataBuilder
{
  private List<QuestDescription> getTaskQuests()
  {
    List<QuestDescription> ret=new ArrayList<QuestDescription>();
    QuestsManager qm=QuestsManager.getInstance();
    for(QuestDescription quest : qm.getAll())
    {
      String category=quest.getCategory();
      // TODO l10n. Use code 112 when available
      if ("Task".equals(category))
      {
        ret.add(quest);
      }
    }
    return ret;
  }

  private Task buildTask(QuestDescription quest)
  {
    System.out.println("ID="+quest.getIdentifier()+" - "+quest.getName());
    // Item
    InventoryItemCondition itemCondition=findRequiredItem(quest);
    if (itemCondition==null)
    {
      return null;
    }
    int count=itemCondition.getCount();
    int itemId=itemCondition.getProxy().getId();
    String name=itemCondition.getProxy().getName();
    System.out.println("\t"+name+" x"+count);
    Task ret=new Task(quest);
    Item item=ItemsManager.getInstance().getItem(itemId);
    ret.setRequiredItems(item,count);
    /*
    Rewards rewards=task.getRewards();
    // Reputation rewards
    List<ReputationReward> reputationRewards=rewards.getRewardElementsOfClass(ReputationReward.class);
    if (reputationRewards.size()>0)
    {
      for(ReputationReward reputationReward : reputationRewards)
      {
        Faction faction=reputationReward.getFaction();
        int amount=reputationReward.getAmount();
        System.out.println("\t"+faction.getName()+" - "+amount+" pts");
      }
    }
    */
    return ret;
  }

  private InventoryItemCondition findRequiredItem(QuestDescription task)
  {
    List<InventoryItemCondition> inventoryItemConditions=findInventoryItemConditions(task);
    int nbConditions=inventoryItemConditions.size();
    if ((nbConditions!=1) && (nbConditions!=2))
    {
      System.out.println("Bad conditions count for "+task+". Got: "+nbConditions);
      return null;
    }
    if (nbConditions>1)
    {
      int itemID=inventoryItemConditions.get(0).getProxy().getId();
      int count=inventoryItemConditions.get(0).getCount();
      int itemID2=inventoryItemConditions.get(1).getProxy().getId();
      int count2=inventoryItemConditions.get(1).getCount();
      if (itemID!=itemID2)
      {
        System.out.println("Item ID mismatch: "+itemID+"!="+itemID2);
      }
      if (count!=count2)
      {
        System.out.println("Item count mismatch: "+count+"!="+count2);
      }
    }
    return inventoryItemConditions.get(0);
  }

  private List<InventoryItemCondition> findInventoryItemConditions(QuestDescription task)
  {
    List<InventoryItemCondition> ret=new ArrayList<InventoryItemCondition>();
    ObjectivesManager objectivesMgr=task.getObjectives();
    List<Objective> objectives=objectivesMgr.getObjectives();
    for(Objective objective : objectives)
    {
      List<ObjectiveCondition> conditions=objective.getConditions();
      for(ObjectiveCondition condition : conditions)
      {
        if (condition instanceof InventoryItemCondition)
        {
          InventoryItemCondition itemCondition=(InventoryItemCondition)condition;
          ret.add(itemCondition);
        }
      }
    }
    return ret;
  }

  /**
   * Do it.
   */
  public void doIt()
  {
    List<Task> tasks=loadTasks();
    boolean ok=TasksXMLWriter.write(GeneratedFiles.TASKS,tasks);
    if (ok)
    {
      System.out.println("Wrote tasks file: "+GeneratedFiles.TASKS);
    }
  }

  private List<Task> loadTasks()
  {
    List<Task> tasks=new ArrayList<Task>();
    List<QuestDescription> taskQuests=getTaskQuests();
    for(QuestDescription taskQuest : taskQuests)
    {
      Task task=buildTask(taskQuest);
      if (task!=null)
      {
        tasks.add(task);
      }
    }
    return tasks;
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainTaskDataBuilder().doIt();
  }
}
