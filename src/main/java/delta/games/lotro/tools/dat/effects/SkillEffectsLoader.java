package delta.games.lotro.tools.dat.effects;

import delta.games.lotro.character.skills.SkillDescription;
import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.common.effects.Effect2;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.dat.utils.Utils;

/**
 * Skill effects loader.
 * @author DAM
 */
public class SkillEffectsLoader
{
  private EffectLoader _loader;

  /**
   * Constructor.
   * @param loader Effects loader.
   */
  public SkillEffectsLoader(EffectLoader loader)
  {
    _loader=loader;
  }

  /**
   * Handle a skill.
   * @param skill Skill to use.
   * @param skillProps Skill properties.
   */
  public void handleSkillProps(SkillDescription skill, PropertiesSet skillProps)
  {
    // TODO Avoid duplicate effects for skills:
    // For instance, Item 1879069473 (a hope token), uses a skill that
    // triggers 2 effects: a fellowship effect that triggers effect X, and effect X a second time!
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
    duration=Utils.normalize(duration);
    Float spellcraft=(Float)effectProps.getProperty("Skill_EffectSpellcraft");
    spellcraft=Utils.normalize(spellcraft);
    Effect2 effect=_loader.getEffect(effectID);
    return new SkillEffectGenerator(effect,spellcraft,duration);
  }
}
