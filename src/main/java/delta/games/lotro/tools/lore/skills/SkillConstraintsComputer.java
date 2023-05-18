package delta.games.lotro.tools.lore.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.character.classes.ClassDescription;
import delta.games.lotro.character.classes.ClassSkill;
import delta.games.lotro.character.classes.ClassesManager;
import delta.games.lotro.character.races.RaceDescription;
import delta.games.lotro.character.races.RaceTrait;
import delta.games.lotro.character.races.RacesManager;
import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.character.traits.TraitsManager;
import delta.games.lotro.common.Identifiable;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.common.enums.SkillCategory;
import delta.games.lotro.common.requirements.ClassRequirement;
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
  //private static final Logger LOGGER=Logger.getLogger(SkillConstraintsComputer.class);

  private Map<Integer,List<ClassDescription>> _classSkills;
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

  private Map<Integer,List<ClassDescription>> loadClassSkills()
  {
    Map<Integer,List<ClassDescription>> ret=new HashMap<Integer,List<ClassDescription>>();
    for(ClassDescription classDescription : ClassesManager.getInstance().getAllCharacterClasses())
    {
      List<ClassSkill> classSkills=classDescription.getSkills();
      for(ClassSkill classSkill : classSkills)
      {
        SkillDescription skill=classSkill.getSkill();
        Integer key=Integer.valueOf(skill.getIdentifier());
        List<ClassDescription> classes=ret.get(key);
        if (classes==null)
        {
          classes=new ArrayList<ClassDescription>();
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
      List<SkillDescription> traitSkills=trait.getSkills();
      for(SkillDescription traitSkill : traitSkills)
      {
        Integer key=Integer.valueOf(traitSkill.getIdentifier());
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
      List<RaceTrait> raceTraits=race.getTraits();
      for(RaceTrait raceTrait : raceTraits)
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
    System.out.println("Skill: "+skill);
    UsageRequirement req=getSkillRequirement(skill);
    System.out.println("\t"+req);
  }

  private UsageRequirement getSkillRequirement(SkillDescription skill)
  {
    Integer key=Integer.valueOf(skill.getIdentifier());
    UsageRequirement ret=null;
    // Class skills
    List<ClassDescription> classes=_classSkills.get(key);
    if ((classes!=null) && (classes.size()>0))
    {
      ret=new UsageRequirement();
      for(ClassDescription characterClass : classes)
      {
        ret.addAllowedClass(characterClass);
      }
    }
    // Item granted skills
    List<Item> items=_itemGrantedSkills.get(key);
    if (items!=null)
    {
      UsageRequirement itemReq=getRequirementFromItems(items);
      if (itemReq==null)
      {
        return null;
      }
      ret=itemReq; // Merge!
    }
    // Trait granted skills
    List<TraitDescription> traits=_traitSkills.get(key);
    if (traits!=null)
    {
      UsageRequirement traitsReq=getRequirementsFromTraits(traits);
      if (traitsReq==null)
      {
        return null;
      }
      ret=traitsReq; // Merge
    }
    return ret;
  }

  private UsageRequirement getRequirementFromItems(List<Item> items)
  {
    UsageRequirement ret=null;
    for(Item item : items)
    {
      UsageRequirement itemReq=null;
      //System.out.println("\tItem: "+item);
      ItemBinding binding=item.getBinding();
      if (binding==ItemBindings.BIND_ON_ACQUIRE)
      {
        itemReq=analyzeRequirements(item.getUsageRequirements());
      }
      if (itemReq==null)
      {
        return null;
      }
      ret=itemReq; // Merge!
    }
    return ret;
  }

  private UsageRequirement getRequirementsFromTraits(List<TraitDescription> traits)
  {
    UsageRequirement ret=null;
    for(TraitDescription trait : traits)
    {
      //System.out.println("\tTrait: "+trait);
      Integer traitKey=Integer.valueOf(trait.getIdentifier());
      List<RaceDescription> races=_trait2Race.get(traitKey);
      if (races==null)
      {
        return null;
      }
      for(RaceDescription raceDescription : races)
      {
        ret=new UsageRequirement();
        ret.addAllowedRace(raceDescription);
        //System.out.println("\t\tRace: "+raceDescription.getRace());
      }
    }
    return ret;
  }

  private UsageRequirement analyzeRequirements(UsageRequirement requirements)
  {
    ClassRequirement classRequirement=requirements.getClassRequirement();
    if (classRequirement!=null)
    {
      return requirements;
    }
    return null;
  }

  private void doIt()
  {
    _classSkills=loadClassSkills();
    _itemGrantedSkills=loadItemGrantedSkills();
    _traitSkills=loadTraitSkills();
    _trait2Race=loadRacialTraits();
    List<SkillDescription> skills=getTravelSkills();
    //List<SkillDescription> skills=getStandardMountSkills();
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
