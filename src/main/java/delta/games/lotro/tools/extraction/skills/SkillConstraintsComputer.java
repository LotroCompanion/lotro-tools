package delta.games.lotro.tools.extraction.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import delta.common.utils.io.Console;
import delta.games.lotro.character.classes.AbstractClassDescription;
import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassSkill;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.SkillAtRank;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.character.utils.TraitAndLevel;
import delta.games.lotro.common.Identifiable;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.requirements.ClassRequirement;
import delta.games.lotro.common.requirements.RaceRequirement;
import delta.games.lotro.common.requirements.UsageRequirement;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemBinding;
import delta.games.lotro.lore.items.ItemBindings;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.details.GrantedElement;
import delta.games.lotro.lore.items.details.ItemDetailsManager;

/**
 * Computes acquisition constraints for skills.
 * @author DAM
 */
public class SkillConstraintsComputer
{
  private static final Logger LOGGER=LoggerFactory.getLogger(SkillConstraintsComputer.class);

  private Map<Integer,Set<AbstractClassDescription>> _classSkills;
  private Map<Integer,List<Item>> _itemGrantedSkills;
  private Map<Integer,List<TraitDescription>> _traitSkills;
  private Map<Integer,List<RaceDescription>> _trait2Race;

  private SkillCategory getSkillCategory(int code)
  {
    LotroEnum<SkillCategory> categories=LotroEnumsRegistry.getInstance().get(SkillCategory.class);
    return categories.getEntry(code);
  }

  List<SkillDescription> getTravelSkills()
  {
    SkillsManager mgr=SkillsManager.getInstance();
    SkillCategory travelSkillsCategory=getSkillCategory(102);
    List<SkillDescription> skills=mgr.getSkillsByCategory(travelSkillsCategory);
    return skills;
  }

  @SuppressWarnings("unused")
  private List<SkillDescription> getStandardMountSkills()
  {
    SkillsManager mgr=SkillsManager.getInstance();
    SkillCategory travelSkillsCategory=getSkillCategory(88);
    List<SkillDescription> skills=mgr.getSkillsByCategory(travelSkillsCategory);
    return skills;
  }

  private Map<Integer,Set<AbstractClassDescription>> loadClassSkills()
  {
    Map<Integer,Set<AbstractClassDescription>> ret=new HashMap<Integer,Set<AbstractClassDescription>>();
    for(ClassDescription classDescription : ClassesManager.getInstance().getAllCharacterClasses())
    {
      List<ClassSkill> classSkills=classDescription.getSkills();
      for(ClassSkill classSkill : classSkills)
      {
        SkillDescription skill=classSkill.getSkill();
        Integer key=Integer.valueOf(skill.getIdentifier());
        Set<AbstractClassDescription> classes=ret.get(key);
        if (classes==null)
        {
          classes=new HashSet<AbstractClassDescription>();
          ret.put(key,classes);
        }
        classes.add(classDescription);
      }
    }
    return ret;
  }

  private Map<Integer,List<Item>> loadItemGrantedSkills()
  {
    Map<Integer,List<Item>> ret=new HashMap<Integer,List<Item>>();
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      ItemDetailsManager detailsMgr=item.getDetails();
      if (detailsMgr==null)
      {
        continue;
      }
      for(GrantedElement<?> element : detailsMgr.getItemDetails(GrantedElement.class))
      {
        Identifiable identifiable=element.getGrantedElement();
        if (identifiable instanceof SkillDescription)
        {
          Integer key=Integer.valueOf(identifiable.getIdentifier());
          List<Item> items=ret.get(key);
          if (items==null)
          {
            items=new ArrayList<Item>();
            ret.put(key,items);
          }
          items.add(item);
        }
      }
    }
    return ret;
  }

  private Map<Integer,List<TraitDescription>> loadTraitSkills()
  {
    Map<Integer,List<TraitDescription>> ret=new HashMap<Integer,List<TraitDescription>>();
    for(TraitDescription trait : TraitsManager.getInstance().getAll())
    {
      List<SkillAtRank> traitSkills=trait.getSkills();
      for(SkillAtRank traitSkill : traitSkills)
      {
        SkillDescription skill=traitSkill.getSkill();
        Integer key=Integer.valueOf(skill.getIdentifier());
        List<TraitDescription> traits=ret.get(key);
        if (traits==null)
        {
          traits=new ArrayList<TraitDescription>();
          ret.put(key,traits);
        }
        traits.add(trait);
      }
    }
    return ret;
  }

  private Map<Integer,List<RaceDescription>> loadRacialTraits()
  {
    Map<Integer,List<RaceDescription>> ret=new HashMap<Integer,List<RaceDescription>>();
    RacesManager mgr=RacesManager.getInstance();
    for(RaceDescription race : mgr.getAll())
    {
      List<TraitAndLevel> raceTraits=race.getTraits();
      for(TraitAndLevel raceTrait : raceTraits)
      {
        TraitDescription trait=raceTrait.getTrait();
        Integer key=Integer.valueOf(trait.getIdentifier());
        List<RaceDescription> races=ret.get(key);
        if (races==null)
        {
          races=new ArrayList<RaceDescription>();
          ret.put(key,races);
        }
        races.add(race);
      }
      for(TraitDescription trait : race.getEarnableTraits())
      {
        Integer key=Integer.valueOf(trait.getIdentifier());
        List<RaceDescription> races=ret.get(key);
        if (races==null)
        {
          races=new ArrayList<RaceDescription>();
          ret.put(key,races);
        }
        races.add(race);
      }
    }
    return ret;
  }

  private void handleSkill(SkillDescription skill)
  {
    UsageRequirement req=getSkillRequirement(skill);
    if (!req.isEmpty())
    {
      Console.println("Skill: "+skill);
      Console.println("\t"+req);
    }
  }

  private UsageRequirement getSkillRequirement(SkillDescription skill)
  {
    Integer key=Integer.valueOf(skill.getIdentifier());
    UsageRequirement ret=new UsageRequirement();
    // Class skills
    Set<AbstractClassDescription> allClasses=new HashSet<AbstractClassDescription>();
    Set<AbstractClassDescription> classes=_classSkills.get(key);
    if (classes!=null)
    {
      allClasses.addAll(classes);
    }
    // Item granted skills
    List<Item> items=_itemGrantedSkills.get(key);
    if (items!=null)
    {
      Set<AbstractClassDescription> classesFromItems=getAllowedClassesFromItems(items);
      if (classesFromItems!=null)
      {
        allClasses.addAll(classesFromItems);
      }
    }
    if (!allClasses.isEmpty())
    {
      List<AbstractClassDescription> allowedClasses=new ArrayList<AbstractClassDescription>(allClasses);
      ClassRequirement classRequirement=new ClassRequirement(allowedClasses);
      ret.setClassRequirement(classRequirement);
    }
    // Trait granted skills
    List<TraitDescription> traits=_traitSkills.get(key);
    if (traits!=null)
    {
      RaceRequirement raceRequirement=getRaceRequirementFromTraits(traits);
      ret.setRaceRequirement(raceRequirement);
    }
    return ret;
  }

  private Set<AbstractClassDescription> getAllowedClassesFromItems(List<Item> items)
  {
    Set<AbstractClassDescription> ret=null;
    for(Item item : items)
    {
      LOGGER.debug("\tItem: {}",item);
      ItemBinding binding=item.getBinding();
      if (binding==ItemBindings.BIND_ON_ACQUIRE)
      {
        AbstractClassDescription characterClass=item.getRequiredClass();
        if (characterClass!=null)
        {
          if (ret==null)
          {
            ret=new HashSet<AbstractClassDescription>();
          }
          ret.add(characterClass);
        }
      }
    }
    return ret;
  }

  private RaceRequirement getRaceRequirementFromTraits(List<TraitDescription> traits)
  {
    RaceRequirement ret=null;
    for(TraitDescription trait : traits)
    {
      LOGGER.debug("\tTrait: {}",trait);
      Integer traitKey=Integer.valueOf(trait.getIdentifier());
      List<RaceDescription> races=_trait2Race.get(traitKey);
      if ((races!=null) && (!races.isEmpty()))
      {
        ret=new RaceRequirement(races);
      }
    }
    return ret;
  }

  private void doIt()
  {
    _classSkills=loadClassSkills();
    _itemGrantedSkills=loadItemGrantedSkills();
    _traitSkills=loadTraitSkills();
    _trait2Race=loadRacialTraits();
    List<SkillDescription> skills=getTravelSkills(); // or getStandardMountSkills()
    for(SkillDescription skill : skills)
    {
      handleSkill(skill);
    }
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new SkillConstraintsComputer().doIt();
  }
}
