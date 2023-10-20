package delta.games.lotro.tools.dat.effects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.character.skills.SkillsManager;
import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.common.effects.EffectGenerator;
import delta.games.lotro.config.LotroCoreConfig;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.misc.Context;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.details.SkillToExecute;
import delta.games.lotro.lore.items.effects.ItemEffectsDisplay;
import delta.games.lotro.lore.items.effects.ItemEffectsManager;
import delta.games.lotro.lore.items.sets.ItemSetEffectsManager;
import delta.games.lotro.lore.items.sets.ItemsSet;
import delta.games.lotro.lore.items.sets.ItemsSetsManager;
import delta.games.lotro.lore.items.sets.SetBonus;
import delta.games.lotro.lore.items.sets.SetEffectsDisplay;
import delta.games.lotro.tools.dat.utils.DataFacadeBuilder;

/**
 * Get effects from DAT files.
 * @author DAM
 */
public class MainDatEffectsLoader
{
  private DataFacade _facade;
  private EffectLoader _loader;
  private Set<Integer> _handledSkills;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatEffectsLoader(DataFacade facade)
  {
    _facade=facade;
    _loader=new EffectLoader(facade);
    _handledSkills=new HashSet<Integer>();
  }

  private void doIt()
  {
    // Items
    for(Item item : ItemsManager.getInstance().getAllItems())
    {
      PropertiesSet props=_facade.loadProperties(item.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
      handleItem(item,props);
    }
    // Sets
    //for(int sedId : TEST_SET_IDS)
    for(ItemsSet set : ItemsSetsManager.getInstance().getAll())
    {
      PropertiesSet props=_facade.loadProperties(set.getIdentifier()+DATConstants.DBPROPERTIES_OFFSET);
      handleSetEffects(set,props);
    }
    _loader.save();
  }

  private void handleItem(Item item, PropertiesSet properties)
  {
    // On equip
    handleOnEquipEffects(item,properties);
    // On use
    handleOnUseEffects(item,properties);
    // Skills
    handleSkillEffects(item,properties);
    showItem(item);
  }

  private void showItem(Item item)
  {
    ItemEffectsDisplay display=new ItemEffectsDisplay();
    System.out.println("Item: "+item);
    String text=display.buildItemEffectsDisplay(item);
    System.out.println(text);
  }

  private void showItemsSet(ItemsSet set)
  {
    boolean hasEffects=hasEffects(set);
    if (hasEffects)
    {
      System.out.println("Set: "+set);
      for(SetBonus bonus : set.getBonuses())
      {
        SetEffectsDisplay display=new SetEffectsDisplay();
        String text=display.buildSetEffectsDisplay(set,bonus).trim();
        if (text.length()>0)
        {
          System.out.println("Nb pieces: "+bonus.getPiecesCount());
          System.out.println(text);
        }
      }
    }
  }

  private boolean hasEffects(ItemsSet set)
  {
    boolean doIt=false;
    List<SetBonus> bonuses=set.getBonuses();
    for(SetBonus bonus : bonuses)
    {
      ItemSetEffectsManager effectsMgr=bonus.getEffects();
      if (effectsMgr!=null)
      {
        doIt=true;
        break;
      }
    }
    return doIt;
  }

  private void handleSetBonusEffects(SetBonus bonus, Object[] effects)
  {
    for(Object effectObj : effects)
    {
      PropertiesSet effectProps=(PropertiesSet)effectObj;
      int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect2 effect=_loader.getEffect(effectId);
      Float spellcraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
      spellcraft=normalize(spellcraft);
      EffectGenerator generator=new EffectGenerator(effect,spellcraft);
      bonus.addEffect(generator);
      // EffectGenerator_EffectDataList
    }
  }

  private void handleOnEquipEffects(Item item, PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_EquipperEffectList");
    handleItemEffects(item,effects,ItemEffectsManager.Type.ON_EQUIP);
  }

  private void handleOnUseEffects(Item item, PropertiesSet properties)
  {
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    handleItemEffects(item,effects,ItemEffectsManager.Type.ON_USE);
  }

  private void handleItemEffects(Item item, Object[] effects, ItemEffectsManager.Type type)
  {
    if (effects==null)
    {
      return;
    }
    for(Object effectObj : effects)
    {
      PropertiesSet effectProps=(PropertiesSet)effectObj;
      int effectId=((Integer)effectProps.getProperty("EffectGenerator_EffectID")).intValue();
      Effect2 effect=_loader.getEffect(effectId);
      Float spellcraft=(Float)effectProps.getProperty("EffectGenerator_EffectSpellcraft");
      spellcraft=normalize(spellcraft);
      EffectGenerator generator=new EffectGenerator(effect,spellcraft);
      Item.addEffect(item,type,generator);
    }
  }

  /**
   * Handle skill effects.
   * @param item Parent item.
   * @param properties Item properties.
   */
  private void handleSkillEffects(Item item, PropertiesSet properties)
  {
    // TODO Avoid duplicate effects for skills:
    // For instance, Item 1879069473 (a hope token), uses a skill that
    // triggers 2 effects: a fellowship effect that triggers effect X, and effect X a second time!
    Integer skillID=(Integer)properties.getProperty("Usage_SkillToExecute");
    if (skillID==null)
    {
      return;
    }
    Integer skillLevel=(Integer)properties.getProperty("Usage_SkillLevel");
    SkillDescription skill=SkillsManager.getInstance().getSkill(skillID.intValue());
    if (!_handledSkills.contains(skillID))
    {
      handleSkillProps(skill);
      _handledSkills.add(skillID);
    }
    SkillToExecute detail=new SkillToExecute(skill,skillLevel);
    Item.addDetail(item,detail);
    //System.out.println("Set skill for item: "+item+" = "+skill+", level="+skillLevel);
  }

  private void handleSkillProps(SkillDescription skill)
  {
    int skillID=skill.getIdentifier();
    PropertiesSet skillProps=_facade.loadProperties(skillID+DATConstants.DBPROPERTIES_OFFSET);
    /*
Skill_AttackHookList: 
  #1: Skill_AttackHookInfo 
    Skill_AttackHook_ActionDurationContributionMultiplier: 0.0
    Skill_AttackHook_TargetEffectList: 
     */
    Object[] attackHookList=(Object[])skillProps.getProperty("Skill_AttackHookList");
    if ((attackHookList==null) || (attackHookList.length==0))
    {
      return;
    }
    for(Object attackHookInfoObj : attackHookList)
    {
      PropertiesSet attackHookInfoProps=(PropertiesSet)attackHookInfoObj;
      Object[] effectList=(Object[])attackHookInfoProps.getProperty("Skill_AttackHook_TargetEffectList");
      if ((effectList!=null) && (effectList.length>0))
      {
        for(Object effectEntry : effectList)
        {
          PropertiesSet effectProps=(PropertiesSet)effectEntry;
          //System.out.println(effectProps.dump());
          Integer effectID=(Integer)effectProps.getProperty("Skill_Effect");
          if ((effectID!=null) && (effectID.intValue()!=0))
          {
            SkillEffectGenerator generator=handleSkillEffect(effectID.intValue(),effectProps);
            SkillDescription.addEffect(skill,generator);
          }
        }
      }
    }
  }

  private SkillEffectGenerator handleSkillEffect(int effectID, PropertiesSet effectProps)
  {
    Float duration=(Float)effectProps.getProperty("Skill_EffectDuration");
    duration=normalize(duration);
    Float spellcraft=(Float)effectProps.getProperty("Skill_EffectSpellcraft");
    spellcraft=normalize(spellcraft);
    Effect2 effect=_loader.getEffect(effectID);
    return new SkillEffectGenerator(effect,spellcraft,duration);
  }

  private Float normalize(Float value)
  {
    if (value==null)
    {
      return null;
    }
    if (value.floatValue()<0)
    {
      return null;
    }
    return value;
  }

  private void handleSetEffects(ItemsSet set,PropertiesSet properties)
  {
    //System.out.println(properties.dump());
    /*
    Set_ActiveCountDataList: 
      #1: Set_ActiveCountData 
        Set_ActiveCount: 2
        Set_EffectDataList: 
          #1: EffectGenerator_EffectStruct 
            EffectGenerator_EffectID: 1879098038
            EffectGenerator_EffectSpellcraft: -1.0
    */
    Object[] activeCountDataList=(Object[])properties.getProperty("Set_ActiveCountDataList");
    if (activeCountDataList==null)
    {
      return;
    }
    for(Object activeCountDataEntry : activeCountDataList)
    {
      PropertiesSet entryProps=(PropertiesSet)activeCountDataEntry;
      //int count=((Integer)entryProps.getProperty("Set_ActiveCount")).intValue();
      Object[] effectsList=(Object[])entryProps.getProperty("Set_EffectDataList");
      if (effectsList==null)
      {
        continue;
      }
      int count=((Integer)entryProps.getProperty("Set_ActiveCount")).intValue();
      SetBonus bonus=set.getBonus(count);
      handleSetBonusEffects(bonus,effectsList);
    }
    showItemsSet(set);
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    Context.init(LotroCoreConfig.getMode());
    DataFacade facade=DataFacadeBuilder.buildFacadeForTools();
    new MainDatEffectsLoader(facade).doIt();
    facade.dispose();
  }
}
