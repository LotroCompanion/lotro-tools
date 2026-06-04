package delta.games.lotro.tools.extraction.achievables;

import java.util.ArrayList;
import java.util.List;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.agents.AgentClassification;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobLocation;
import delta.games.lotro.lore.agents.mobs.MobSelection;
import delta.games.lotro.lore.agents.mobs.MobsManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.loots.AchievableLoot;
import delta.games.lotro.lore.quests.loots.AchievableLoots;
import delta.games.lotro.tools.extraction.utils.MobUtils;

/**
 * Loader for achievable loots.
 * @author DAM
 */
public class AchievableLootLoader
{
  private MobUtils _mobUtils;

  /**
   * Constructor.
   */
  public AchievableLootLoader()
  {
    _mobUtils=new MobUtils();
  }

  /**
   * Handle loot data for the given achievable.
   * @param achievable Achievable.
   * @param properties Raw properties.
   */
  public void handleLoot(Achievable achievable, PropertiesSet properties)
  {
    Object[] lootPropsArray=(Object[])properties.getProperty("Quest_LootTable");
    if (lootPropsArray==null)
    {
      return;
    }
    AchievableLoots loots=new AchievableLoots();
    for(Object lootProps : lootPropsArray)
    {
      AchievableLoot loot=handleElementaryLoot((PropertiesSet)lootProps);
      if (loot!=null)
      {
        loots.addLoot(loot);
      }
    }
    achievable.setLoots(loots);
  }

  private AchievableLoot handleElementaryLoot(PropertiesSet lootProps)
  {
    int itemID=((Integer)lootProps.getProperty("Quest_LootItemDID")).intValue();
    Item item=ItemsManager.getInstance().getItem(itemID);
    if (item==null)
    {
      return null;
    }
    AchievableLoot loot=new AchievableLoot(item);
    // Probabilities
    Integer probability=(Integer)lootProps.getProperty("Quest_LootItemProbability");
    if (probability!=null)
    {
      loot.setProbabilities(new int[] {probability.intValue()});
    }
    Object[] probabilities=(Object[])lootProps.getProperty("Quest_LootItemProbabilityArray");
    if (probabilities!=null)
    {
       List<Integer> intProbabilities=new ArrayList<Integer>();
       for(Object probabilitiyEntry : probabilities)
       {
         Integer intProbability=(Integer)probabilitiyEntry;
         intProbabilities.add(intProbability);
       }
       int[] probabilitiesToSet=new int[intProbabilities.size()];
       for(int i=0;i<probabilitiesToSet.length;i++)
       {
         probabilitiesToSet[i]=intProbabilities.get(i).intValue();
       }
       loot.setProbabilities(probabilitiesToSet);
    }
    // Monster DID
    Integer mobDID=(Integer)lootProps.getProperty("Quest_LootMonsterDID");
    if (mobDID!=null)
    {
      MobDescription mob=MobsManager.getInstance().getMobById(mobDID.intValue());
      loot.setMob(mob);
    }
    // Monster specs
    Object[] monsterSpecsArray=(Object[])lootProps.getProperty("Quest_LootMonsterGenus_Array");
    if (monsterSpecsArray!=null)
    {
      for(Object monsterSpecEntry : monsterSpecsArray)
      {
        MobSelection spec=parseMonsterSpec((PropertiesSet)monsterSpecEntry);
        loot.addMonsterSpec(spec);
      }
    }
    // Monster excluded specs
    Object[] monsterExcludedSpecsArray=(Object[])lootProps.getProperty("Quest_LootMonsterGenus_ExclusionArray");
    if (monsterExcludedSpecsArray!=null)
    {
      for(Object monsterExcludedSpecEntry : monsterExcludedSpecsArray)
      {
        MobSelection spec=parseMonsterSpec((PropertiesSet)monsterExcludedSpecEntry);
        loot.addExcludedMonsterSpec(spec);
      }
    }
    return loot;
  }

  private MobSelection parseMonsterSpec(PropertiesSet mobSpecProps)
  {
    MobLocation mobLocation=_mobUtils.parseMobLocation(mobSpecProps);
    AgentClassification classification=_mobUtils.parseClassification(mobSpecProps);
    MobSelection ret=new MobSelection(mobLocation,classification);
    return ret;
  }
}
