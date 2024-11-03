package delta.games.lotro.tools.extraction.skills;

import delta.games.lotro.character.skills.SkillEffectType;

/**
 * Properties to use when loading skill effects.
 * @author DAM
 */
public class SkillEffectsProperties
{
  private SkillEffectType _type;
  private String _effectsListProperty;
  private String _additiveModsProperty;
  private String _overrideProperty;
  private String _implementUsageProperty;

  /**
   * User effects.
   */
  public static final SkillEffectsProperties USER=buildUserEffects();
  /**
   * Critical effects.
   */
  public static final SkillEffectsProperties CRITICAL=buildCriticalEffects();
  /**
   * Toggle effects.
   */
  public static final SkillEffectsProperties TOGGLE=buildToggleEffects();
  /**
   * Toglle user effects.
   */
  public static final SkillEffectsProperties TOGGLE_USER=buildToggleUserEffects();
  /**
   * Attack (default).
   */
  public static final SkillEffectsProperties ATTACK=buildAttackEffects(SkillEffectType.ATTACK,"Skill_AttackHook_Target");
  /**
   * Attack (critical result).
   */
  public static final SkillEffectsProperties ATTACK_CRITICAL=buildAttackEffects(SkillEffectType.ATTACK_CRITICAL,"Skill_AttackHook_CriticalTarget");
  /**
   * Attack (with positional bonus).
   */
  public static final SkillEffectsProperties ATTACK_POSITIONAL=buildAttackEffects(SkillEffectType.ATTACK_POSITIONAL,"Skill_AttackHook_PositionalTarget");
  /**
   * Attack (devastate result).
   */
  public static final SkillEffectsProperties ATTACK_SUPERCRITICAL=buildAttackEffects(SkillEffectType.ATTACK_SUPERCRITICAL,"Skill_AttackHook_SuperCriticalTarget");

  /**
   * Get the skill effect type.
   * @return A skill effect type.
   */
  public SkillEffectType getType()
  {
    return _type;
  }

  /**
   * Get the property name for the effects list.
   * @return A property name.
   */
  public String getEffectsListProperty()
  {
    return _effectsListProperty;
  }

  /**
   * Get the property name for the additive modifiers.
   * @return A property name.
   */
  public String getAdditiveModsProperty()
  {
    return _additiveModsProperty;
  }

  /**
   * Get the property name for the effects list override.
   * @return A property name.
   */
  public String getOverrideProperty()
  {
    return _overrideProperty;
  }

  /**
   * Get the property name for the implement usage.
   * @return A property name.
   */
  public String getImplementUsageProperty()
  {
    return _implementUsageProperty;
  }

  private static SkillEffectsProperties buildUserEffects()
  {
    SkillEffectsProperties ret=new SkillEffectsProperties();
    ret._type=SkillEffectType.USER;
    ret._effectsListProperty="Skill_UserEffectList";
    ret._additiveModsProperty="Skill_UserEffectList_AdditiveArray";
    ret._overrideProperty="Skill_UserEffectList_Override";
    ret._implementUsageProperty=null;
    return ret;
  }

  private static SkillEffectsProperties buildCriticalEffects()
  {
    SkillEffectsProperties ret=new SkillEffectsProperties();
    ret._type=SkillEffectType.SELF_CRITICAL;
    ret._effectsListProperty="Skill_CriticalEffectList";
    ret._additiveModsProperty="Skill_CriticalEffectList_AdditiveArray";
    ret._overrideProperty=null;
    ret._implementUsageProperty=null;
    return ret;
  }

  private static SkillEffectsProperties buildToggleEffects()
  {
    SkillEffectsProperties ret=new SkillEffectsProperties();
    ret._type=SkillEffectType.TOGGLE;
    ret._effectsListProperty="Skill_Toggle_Effect_List";
    ret._additiveModsProperty="Skill_Toggle_EffectList_AdditiveArray";
    ret._overrideProperty="Skill_Toggle_EffectList_Override";
    ret._implementUsageProperty="Skill_Toggle_Effect_ImplementUsage";
    return ret;
  }

  private static SkillEffectsProperties buildToggleUserEffects()
  {
    SkillEffectsProperties ret=new SkillEffectsProperties();
    ret._type=SkillEffectType.USER_TOGGLE;
    ret._effectsListProperty="Skill_Toggle_User_Effect_List";
    ret._additiveModsProperty="Skill_Toggle_UserEffectList_AdditiveArray";
    ret._overrideProperty="Skill_Toggle_UserEffectList_Override";
    ret._implementUsageProperty="Skill_Toggle_User_Effect_ImplementUsage";
    return ret;
  }

  private static SkillEffectsProperties buildAttackEffects(SkillEffectType type, String seed)
  {
    SkillEffectsProperties ret=new SkillEffectsProperties();
    ret._type=type;
    ret._effectsListProperty=seed+"EffectList";
    ret._additiveModsProperty=seed+"EffectList_AdditiveArray";
    ret._overrideProperty=seed+"EffectList_Override";
    ret._implementUsageProperty=null;
    return ret;
  }
}
