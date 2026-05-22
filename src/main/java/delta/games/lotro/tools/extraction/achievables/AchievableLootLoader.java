package delta.games.lotro.tools.extraction.achievables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import delta.games.lotro.common.enums.AgentClass;
import delta.games.lotro.common.enums.Alignment;
import delta.games.lotro.common.enums.Genus;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.MobDivision;
import delta.games.lotro.common.enums.Species;
import delta.games.lotro.common.enums.SubSpecies;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.agents.AgentClassification;
import delta.games.lotro.lore.agents.EntityClassification;
import delta.games.lotro.lore.agents.mobs.MobDescription;
import delta.games.lotro.lore.agents.mobs.MobLocation;
import delta.games.lotro.lore.agents.mobs.MobsManager;
import delta.games.lotro.lore.geo.landmarks.LandmarkDescription;
import delta.games.lotro.lore.geo.landmarks.LandmarksManager;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.maps.GeoAreasManager;
import delta.games.lotro.lore.maps.LandDivision;
import delta.games.lotro.lore.quests.Achievable;
import delta.games.lotro.lore.quests.loots.AchievableLoot;
import delta.games.lotro.lore.quests.loots.AchievableLootMonsterSpec;
import delta.games.lotro.lore.quests.loots.AchievableLoots;

/**
 * Loader for achievable loots.
 * @author DAM
 */
public class AchievableLootLoader
{
  private LotroEnum<MobDivision> _mobDivisionEnum;
  private LotroEnum<AgentClass> _agentClassEnum;
  private LotroEnum<Alignment> _alignmentEnum;
  private LotroEnum<Genus> _genusEnum;
  private LotroEnum<Species> _speciesEnum;
  private LotroEnum<SubSpecies> _subSpeciesEnum;

  /**
   * Constructor.
   */
  public AchievableLootLoader()
  {
    _mobDivisionEnum=LotroEnumsRegistry.getInstance().get(MobDivision.class);
    _agentClassEnum=LotroEnumsRegistry.getInstance().get(AgentClass.class);
    _alignmentEnum=LotroEnumsRegistry.getInstance().get(Alignment.class);
    _genusEnum=LotroEnumsRegistry.getInstance().get(Genus.class);
    _speciesEnum=LotroEnumsRegistry.getInstance().get(Species.class);
    _subSpeciesEnum=LotroEnumsRegistry.getInstance().get(SubSpecies.class);
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
        AchievableLootMonsterSpec spec=parseMonsterSpec((PropertiesSet)monsterSpecEntry);
        loot.addMonsterSpec(spec);
      }
    }
    // Monster excluded specs
    Object[] monsterExcludedSpecsArray=(Object[])lootProps.getProperty("Quest_LootMonsterGenus_ExclusionArray");
    if (monsterExcludedSpecsArray!=null)
    {
      for(Object monsterExcludedSpecEntry : monsterExcludedSpecsArray)
      {
        AchievableLootMonsterSpec spec=parseMonsterSpec((PropertiesSet)monsterExcludedSpecEntry);
        loot.addExcludedMonsterSpec(spec);
      }
    }
    return loot;
  }

  private AchievableLootMonsterSpec parseMonsterSpec(PropertiesSet mobSpecProps)
  {
    MobLocation mobLocation=parseMobLocation(mobSpecProps);
    AgentClassification classification=parseClassification(mobSpecProps);
    AchievableLootMonsterSpec ret=new AchievableLootMonsterSpec(mobLocation,classification);
    return ret;
  }

  private MobLocation parseMobLocation(PropertiesSet mobSpecProps)
  {
    // Mob location
    MobDivision mobDivision=null;
    Integer mobDivisionCode=(Integer)mobSpecProps.getProperty("Quest_MonsterDivision");
    if (mobDivisionCode!=null)
    {
      mobDivision=_mobDivisionEnum.getEntry(mobDivisionCode.intValue());
    }
    LandDivision landDivision=null;
    Integer regionDID=(Integer)mobSpecProps.getProperty("Quest_MonsterRegion");
    if (regionDID!=null)
    {
      landDivision=GeoAreasManager.getInstance().getLandById(regionDID.intValue());
    }
    LandmarkDescription landmark=null;
    Integer landmarkDID=(Integer)mobSpecProps.getProperty("QuestEvent_LandmarkDID");
    if (landmarkDID!=null)
    {
      landmark=LandmarksManager.getInstance().getLandmarkById(landmarkDID.intValue());
    }
    MobLocation mobLocation=new MobLocation(mobDivision,landDivision,landmark);
    return mobLocation;
  }

  private AgentClassification parseClassification(PropertiesSet mobSpecProps)
  {
    AgentClassification ret=new AgentClassification();
    // Agent classification
    // - Alignment
    Integer alignmentCode=(Integer)mobSpecProps.getProperty("Quest_MonsterAlignment");
    if (alignmentCode!=null)
    {
      Alignment alignment=_alignmentEnum.getEntry(alignmentCode.intValue());
      ret.setAlignment(alignment);
    }
    // Class
    Integer classCode=(Integer)mobSpecProps.getProperty("Quest_MonsterClass");
    if (classCode!=null)
    {
      AgentClass agentClass=_agentClassEnum.getEntry(classCode.intValue());
      ret.setAgentClass(agentClass);
    }
    // Entity classification
    EntityClassification classification=ret.getEntityClassification();
    // - Genus
    Integer genusCode=(Integer)mobSpecProps.getProperty("Quest_MonsterGenus");
    if (genusCode!=null)
    {
      Genus genus=_genusEnum.getEntry(genusCode.intValue());
      if (genus!=null)
      {
        classification.setGenus(Arrays.asList(genus));
      }
    }
    // - Species
    Integer speciesCode=(Integer)mobSpecProps.getProperty("Quest_MonsterSpecies");
    if (speciesCode!=null)
    {
      Species species=_speciesEnum.getEntry(speciesCode.intValue());
      classification.setSpecies(species);
    }
    // Sub-species
    Integer subSpeciesCode=(Integer)mobSpecProps.getProperty("Quest_MonsterSubspecies");
    if (subSpeciesCode!=null)
    {
      SubSpecies subSpecies=_subSpeciesEnum.getEntry(subSpeciesCode.intValue());
      classification.setSubSpecies(subSpecies);
    }
    return ret;
  }
}
