package delta.games.lotro.tools.extraction.effects;

import delta.games.lotro.character.skills.SkillEffectGenerator;
import delta.games.lotro.common.effects.Effect;
import delta.games.lotro.common.enums.ImplementUsageType;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.extraction.utils.Utils;

/**
 * Loader for skill effects.
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
    // Effect ID
    Integer effectID=(Integer)effectProps.getProperty("Skill_Toggle_Effect");
    if ((effectID==null) || (effectID.intValue()==0))
    {
      return null;
    }
    // Spellcraft
    Float spellcraft=(Float)effectProps.getProperty("Skill_Toggle_Effect_Spellcraft");
    spellcraft=Utils.normalize(spellcraft);
    // Implement usage
    ImplementUsageType implementUsage=null;
    Integer implementUsageCode=(Integer)effectProps.getProperty("Skill_Toggle_Effect_ImplementUsage");
    if ((implementUsageCode!=null) && (implementUsageCode.intValue()>0))
    {
      implementUsage=_implementUsageEnum.getEntry(implementUsageCode.intValue());
    }
    Effect effect=_loader.getEffect(effectID.intValue());
    SkillEffectGenerator generator=new SkillEffectGenerator(effect,spellcraft,null);
    generator.setImplementUsage(implementUsage);
    return generator;
  }
}
