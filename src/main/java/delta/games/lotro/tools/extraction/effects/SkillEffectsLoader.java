package delta.games.lotro.tools.extraction.effects;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.character.skills.SkillEffectType;
import delta.games.lotro.character.skills.SkillEffectsManager;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.enums.ImplementUsageType;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.Utils;

/**
 * Skill effects loader.
 * @author DAM
 */
public class SkillEffectsLoader
{
  private EffectLoader _loader;
  private LotroEnum<ImplementUsageType> _implementUsageEnum;

  /**
   * Constructor.
   * @param loader Effects loader.
   */
  public SkillEffectsLoader(EffectLoader loader)
  {
    _loader=loader;
    _implementUsageEnum=LotroEnumsRegistry.getInstance().get(ImplementUsageType.class);
  }

  /**
   * Handle a skill.
   * @param skill Skill to use.
   * @param skillProps Skill properties.
   */
  public void handleSkillProps(SkillDescription skill, PropertiesSet skillProps)
  {
    SkillEffectsManager mgr=new SkillEffectsManager();
    handleSkillAttackHookList(mgr,skillProps);
    handleToggleSkillEffects(mgr,skillProps);
    handleUserSkillEffects(mgr,skillProps);
    if (mgr.hasEffects())
    {
      skill.setEffects(mgr);
    }
  }

  private void handleSkillAttackHookList(SkillEffectsManager mgr, PropertiesSet skillProps)
  {
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
          handleSkillEffect(mgr,effectProps,SkillEffectType.ATTACK);
        }
      }
    }
  }

  private void handleToggleSkillEffects(SkillEffectsManager mgr, PropertiesSet skillProps)
  {
    /*
Skill_Toggle_Effect_List:
  #1: Skill_Toggle_Effect_Data
    Skill_Toggle_Effect: 1879383550
    Skill_Toggle_Effect_Spellcraft: -1.0
  #2: Skill_Toggle_Effect_Data
    Skill_Toggle_Effect: 1879320187
    Skill_Toggle_Effect_Spellcraft: -1.0
    */
    Object[] effectList=(Object[])skillProps.getProperty("Skill_Toggle_Effect_List");
    if ((effectList!=null) && (effectList.length>0))
    {
      for(Object effectEntry : effectList)
      {
        PropertiesSet effectProps=(PropertiesSet)effectEntry;
        handleSkillToggleEffect(mgr,effectProps,SkillEffectType.TOGGLE);
      }
    }
    /*
Skill_Toggle_User_Effect_List:
  #1: Skill_Toggle_Effect_Data
    Skill_Toggle_Effect: 1879174081
    Skill_Toggle_Effect_Spellcraft: -1.0
    */
    Object[] userEffectList=(Object[])skillProps.getProperty("Skill_Toggle_User_Effect_List");
    if ((userEffectList!=null) && (userEffectList.length>0))
    {
      for(Object effectEntry : userEffectList)
      {
        PropertiesSet effectProps=(PropertiesSet)effectEntry;
        handleSkillToggleEffect(mgr,effectProps,SkillEffectType.USER_TOGGLE);
      }
    }
  }

  private void handleUserSkillEffects(SkillEffectsManager mgr, PropertiesSet skillProps)
  {
    /*
Skill_UserEffectList:
  #1: Skill_EffectData
    Skill_Effect: 1879102614
    Skill_EffectDuration: -1.0
    Skill_EffectImplementUsage: 0 (Undef)
    Skill_EffectSpellcraft: -1.0
     */
    Object[] userEffectList=(Object[])skillProps.getProperty("Skill_UserEffectList");
    if ((userEffectList!=null) && (userEffectList.length>0))
    {
      for(Object effectEntry : userEffectList)
      {
        PropertiesSet effectProps=(PropertiesSet)effectEntry;
        handleSkillEffect(mgr,effectProps,SkillEffectType.USER);
      }
    }
  }

  private void handleSkillEffect(SkillEffectsManager mgr, PropertiesSet effectProps, SkillEffectType type)
  {
    SkillEffectGenerator generator=loadSkillEffect(effectProps);
    if (generator!=null)
    {
      generator.setType(type);
      mgr.addEffect(generator);
    }
  }

  /**
   * Load a skill effect.
   * @param effectProps Effect properties.
   * @return A generator or <code>null</code>.
   */
  public SkillEffectGenerator loadSkillEffect(PropertiesSet effectProps)
  {
    // Effect
    Integer effectID=(Integer)effectProps.getProperty("Skill_Effect");
    if ((effectID==null) || (effectID.intValue()==0))
    {
      return null;
    }
    Effect effect=_loader.getEffect(effectID.intValue());
    // Duration
    Float duration=(Float)effectProps.getProperty("Skill_EffectDuration");
    duration=Utils.normalize(duration);
    // Spellcraft
    Float spellcraft=(Float)effectProps.getProperty("Skill_EffectSpellcraft");
    spellcraft=Utils.normalize(spellcraft);
    // Implement usage
    ImplementUsageType implementUsage=null;
    Integer implementUsageCode=(Integer)effectProps.getProperty("Skill_EffectImplementUsage");
    if ((implementUsageCode!=null) && (implementUsageCode.intValue()>0))
    {
      implementUsage=_implementUsageEnum.getEntry(implementUsageCode.intValue());
    }
    SkillEffectGenerator generator=new SkillEffectGenerator(effect,spellcraft,duration);
    generator.setImplementUsage(implementUsage);
    return generator;
  }

  /**
   * Load a toggle skill effect.
   * @param effectProps Effect properties.
   * @return the loaded effect generator or <code>null</code>.
   */
  public SkillEffectGenerator loadSkillToggleEffect(PropertiesSet effectProps)
  {
    Integer effectID=(Integer)effectProps.getProperty("Skill_Toggle_Effect");
    if ((effectID==null) || (effectID.intValue()==0))
    {
      return null;
    }
    Float spellcraft=(Float)effectProps.getProperty("Skill_Toggle_Effect_Spellcraft");
    spellcraft=Utils.normalize(spellcraft);
    Effect effect=_loader.getEffect(effectID.intValue());
    SkillEffectGenerator generator=new SkillEffectGenerator(effect,spellcraft,null);
    return generator;
  }

  private void handleSkillToggleEffect(SkillEffectsManager mgr, PropertiesSet effectProps, SkillEffectType type)
  {
    SkillEffectGenerator generator=loadSkillToggleEffect(effectProps);
    if (generator!=null)
    {
      generator.setType(type);
      mgr.addEffect(generator);
    }
  }
}
